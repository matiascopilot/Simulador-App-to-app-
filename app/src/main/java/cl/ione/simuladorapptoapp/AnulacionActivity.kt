package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.CancellationRequest
import cl.ione.simuladorapptoapp.databinding.ActivityAnulacionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class AnulacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnulacionBinding
    private val REQUEST_CODE_ANULACION = 3444
    private val TAG = "AnulacionActivity"
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = "" // Para guardar el request actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnulacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        configurarHeader()
        configurarListeners()
        configurarActualizacionRequest() // Configurar actualización automática
        actualizarRequestJson() // Generar request inicial
    }

    private fun configurarHeader() {
        val titulo = if (isCommandsMode) {
            "Anulación JSON"
        } else {
            "Anulación"
        }

        binding.header.setup(
            title = titulo,
            showBackButton = true,
            showRequestButton = true, // Mostrar botón de request
            onBackClick = { finish() },
            onRequestClick = {
                // Mostrar el request actual
                if (currentRequestJson.isNotEmpty()) {
                    binding.header.showRequestJson(currentRequestJson, "REQUEST ANULACIÓN")
                } else {
                    Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun configurarListeners() {
        binding.footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "ANULAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { solicitarAnulacion() }
        )
    }

    // Configurar actualización automática cuando cambian los campos
    private fun configurarActualizacionRequest() {
        // TextWatcher para campos de texto
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                actualizarRequestJson()
            }
        }

        binding.etOperationId.addTextChangedListener(textWatcher)

        // Listener para RadioGroup
        binding.rgPrintOnPos.setOnCheckedChangeListener { _, _ ->
            actualizarRequestJson()
        }
    }

    // Actualizar el JSON del request con los valores actuales
    private fun actualizarRequestJson() {
        try {
            val operationIdString = binding.etOperationId.text.toString()
            val operationId = operationIdString.toIntOrNull() ?: 0
            val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
            val typeApp: Byte = 0

            val jsonObject = if (isCommandsMode) {
                JSONObject().apply {
                    put("OperationId", operationId)
                    put("PrintOnPos", printOnPos)
                    put("TypeApp", typeApp)
                    put("PlaceCardToPayTimeout", 20)
                    put("PaymentResultTimeout", 2)
                }
            } else {
                JSONObject().apply {
                    put("OperationId", operationId)
                    put("PrintOnPos", printOnPos)
                    put("TypeApp", typeApp)
                }
            }

            currentRequestJson = jsonObject.toString(4)

        } catch (e: Exception) {
            currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
        }
    }

    private fun solicitarAnulacion() {
        try {
            // 1. Validar Operation ID
            val operationIdString = binding.etOperationId.text.toString()
            if (operationIdString.isEmpty()) {
                Toast.makeText(this, "Ingrese número de comprobante", Toast.LENGTH_SHORT).show()
                binding.etOperationId.requestFocus()
                return
            }

            // 2. Convertir String a Int
            val operationId = operationIdString.toIntOrNull()
            if (operationId == null) {
                Toast.makeText(this, "El número de comprobante debe ser válido", Toast.LENGTH_SHORT).show()
                binding.etOperationId.requestFocus()
                return
            }

            // 3. Validar que sea positivo
            if (operationId <= 0) {
                Toast.makeText(this, "El número de comprobante debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                binding.etOperationId.requestFocus()
                return
            }

            // 4. Obtener valor de PrintOnPos desde RadioGroup
            val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id

            // 5. typeApp: 0 = Producción
            val typeApp: Byte = 0

            // 6. Crear el intent
            val intent = Intent("cl.getnet.payment.action.CANCELLATION")
            intent.setPackage("cl.getnet.payment.devgetnet")  // ✅ AGREGAR ESTA LÍNEA

            if (isCommandsMode) {
                val cancellationRequestJson = """
            {
                "OperationId": $operationId,
                "PrintOnPos": $printOnPos,
                "TypeApp": $typeApp,
                "PlaceCardToPayTimeout": 20,
                "PaymentResultTimeout": 2
            }
            """.trimIndent()
                intent.putExtra("params", cancellationRequestJson)
                Log.d(TAG, "JSON: $cancellationRequestJson")
            } else {
                val request = CancellationRequest(operationId, printOnPos, typeApp)
                intent.putExtra("params", request)
                Log.d(TAG, "CancellationRequest: $operationId, $printOnPos, $typeApp")
            }

            // 7. Verificar disponibilidad de Getnet
            val actividades = packageManager.queryIntentActivities(intent, 0)
            if (actividades.isNotEmpty()) {
                Log.d(TAG, "✅ Actividades encontradas: ${actividades.size}")
                startActivityForResult(intent, REQUEST_CODE_ANULACION)
                // Actualizar request después de enviar
                actualizarRequestJson()
            } else {
                Log.e(TAG, "❌ ERROR: Getnet no está disponible")
                Toast.makeText(this,
                    "Getnet no está disponible en este dispositivo",
                    Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ANULACION -> {
                procesarRespuestaAnulacion(resultCode, data)
            }
        }
    }

    private fun procesarRespuestaAnulacion(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                JsonParser.showAnulacionResult(this, data)
            }

            RESULT_CANCELED -> {
                val error = data?.getStringExtra("error") ?: "Operación cancelada"
                mostrarResultado("ANULACIÓN CANCELADA\n\n$error")
            }

            else -> {
                val errorMsg = data?.getStringExtra("error") ?:
                data?.getStringExtra("message") ?:
                "Error desconocido (Código: ${data?.getIntExtra("ResponseCode", -1)})"
                mostrarResultado("ANULACIÓN RECHAZADA\n\nMotivo: $errorMsg")
            }
        }
    }

    private fun mostrarResultado(mensaje: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Resultado de Anulación")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}