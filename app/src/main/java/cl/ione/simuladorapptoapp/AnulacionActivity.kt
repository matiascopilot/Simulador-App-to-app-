package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityAnulacionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.getnet.payment.interop.parcels.CancellationRequest

class AnulacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnulacionBinding
    private val REQUEST_CODE_ANULACION = 3443
    private val TAG = "AnulacionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnulacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarHeader()
        configurarListeners()
    }

    private fun configurarHeader() {
        binding.header.setup(
            title = "Anulación",
            showBackButton = true,
            onBackClick = { finish() }
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

    private fun solicitarAnulacion() {
        try {
            // 1. Validar Operation ID
            val operationIdString = binding.etOperationId.text.toString()
            if (operationIdString.isEmpty()) {
                Toast.makeText(this, "Ingrese número de operación", Toast.LENGTH_SHORT).show()
                binding.etOperationId.requestFocus()
                return
            }

            // 2. Convertir String a Int
            val operationId = operationIdString.toIntOrNull()
            if (operationId == null) {
                Toast.makeText(this, "El número de operación debe ser válido", Toast.LENGTH_SHORT).show()
                binding.etOperationId.requestFocus()
                return
            }

            // 3. Obtener valor de PrintOnPos desde RadioGroup
            val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id

            // 4. typeApp: 0 = Producción
            val typeApp: Byte = 0

            // 5. Crear el request
            val request = CancellationRequest(operationId, printOnPos, typeApp)

            // 6. Crear el intent
            val intent = Intent("cl.getnet.payment.action.CANCELLATION")
            intent.putExtra("params", request)

            // 7. Iniciar actividad
            startActivityForResult(intent, REQUEST_CODE_ANULACION)
            Log.d(TAG, "CancellationRequest - operationId: $operationId, printOnPos: $printOnPos, typeApp: $typeApp")

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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
                binding.etOperationId.isEnabled = false
                binding.rgPrintOnPos.isEnabled = false
            }

            RESULT_CANCELED -> {
                val error = data?.getStringExtra("error") ?: "Operación cancelada"
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Anulación fallida: $error")
            }

            else -> {
                Toast.makeText(this, "Error inesperado", Toast.LENGTH_LONG).show()
                Log.e(TAG, "ResultCode inesperado: $resultCode")
            }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}