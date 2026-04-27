package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.CancellationRequest
import cl.ione.simuladorapptoapp.databinding.ActivityAnulacionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import org.json.JSONObject

class AnulacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnulacionBinding
    private val REQUEST_CODE_ANULACION = 3444
    private val TAG = "AnulacionActivity"
    private var isCommandsMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnulacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        configurarHeader()
        setupRequestManager()
        configurarListeners()
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
            showRequestButton = true,
            onBackClick = { finish() }
            // El onRequestClick lo maneja RequestManager
        )
    }

    private fun setupRequestManager() {
        // 1. Vincular el header con RequestManager
        RequestManager.bind(binding.header)

        // 2. Función que genera el JSON con los valores actuales
        val buildRequestJson = {
            val operationIdString = binding.etOperationId.text.toString()
            val operationId = operationIdString.toIntOrNull() ?: 0
            val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
            val typeApp: Byte = 0

            if (isCommandsMode) {
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
        }

        // 3. Vincular los campos automáticamente
        val titulo = if (isCommandsMode) "Anulación JSON" else "Anulación (Librería)"

        // Bind EditText
        RequestManager.bindEditText(binding.etOperationId,
            updateFunction = buildRequestJson,
            title = titulo
        )

        // Bind RadioGroup
        RequestManager.bindRadioGroup(binding.rgPrintOnPos,
            updateFunction = buildRequestJson,
            title = titulo
        )

        // 4. Inicializar con valores por defecto
        RequestManager.initWithDefault(buildRequestJson, titulo)
    }

    private fun configurarListeners() {
        binding.footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "ANULAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { solicitarAnulacion() }
        )
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
            val typeApp: Byte = 0

            // 5. Crear el intent
            val intent = Intent("cl.getnet.payment.action.CANCELLATION")
            intent.setPackage("cl.getnet.payment.devgetnet")

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
                startActivityForResult(intent, REQUEST_CODE_ANULACION)
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
        val requestData = RequestManager.getCurrentRequest()

        when (resultCode) {
            RESULT_OK -> {
                // Transacción exitosa
                JsonParser.showAnulacionResult(this, data, requestData = requestData)
            }

            RESULT_CANCELED -> {
                // Transacción cancelada - mostrar error con opción de reintentar
                JsonParser.showErrorWithRetry(
                    activity = this,
                    data = data,
                    title = "ANULACIÓN CANCELADA",
                    onRetry = { solicitarAnulacion() },  // Reintentar la misma operación
                    onCancel = { /* Opcional: qué hacer si cancela */ }
                )
            }

            else -> {
                // Otro tipo de error
                JsonParser.showErrorWithRetry(
                    activity = this,
                    data = data,
                    title = "ANULACIÓN RECHAZADA",
                    onRetry = { solicitarAnulacion() },
                    onCancel = null
                )
            }
        }
    }
}