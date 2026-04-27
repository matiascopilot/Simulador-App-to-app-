package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.CloseRequest
import cl.ione.simuladorapptoapp.databinding.ActivityCierreBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import org.json.JSONObject

class CierreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCierreBinding
    private val REQUEST_CODE_CIERRE = 3448
    private val TAG = "CierreActivity"
    private var isCommandsMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCierreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
        configurarHeader()
        setupPrintOptionsVisibility() // Nueva función
        setupRequestManager()
        configurarListeners()
    }

    private fun configurarHeader() {
        val titulo = if (isCommandsMode) "Cierre JSON" else "Cierre de Caja"

        binding.header.setup(
            title = titulo,
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    // Mostrar opciones de impresión solo en modo JSON
    private fun setupPrintOptionsVisibility() {
        if (isCommandsMode) {
            binding.layoutPrintOptions.visibility = View.VISIBLE
        } else {
            binding.layoutPrintOptions.visibility = View.GONE
        }
    }

    private fun setupRequestManager() {
        RequestManager.bind(binding.header)

        val buildRequestJson = {
            val typeApp: Byte = 0
            val printOnPos = if (isCommandsMode) {
                binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
            } else {
                true // Valor por defecto, no se usa
            }

            if (isCommandsMode) {
                JSONObject().apply {
                    put("PrintOnPos", printOnPos)
                    put("TypeApp", typeApp)
                    put("PaymentResultTimeout", 2)
                }
            } else {
                JSONObject().apply {
                    put("TypeApp", typeApp)
                    // No incluimos PrintOnPos en modo librería
                }
            }
        }

        val titulo = if (isCommandsMode) "Cierre JSON" else "Cierre de Caja"

        // Solo vincular el RadioGroup en modo JSON
        if (isCommandsMode) {
            RequestManager.bindRadioGroup(
                binding.rgPrintOnPos,
                updateFunction = buildRequestJson,
                title = titulo
            )
        }

        RequestManager.initWithDefault(buildRequestJson, titulo)
    }

    private fun configurarListeners() {
        binding.footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "CONFIRMAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { solicitarCierre() }
        )
    }

    private fun solicitarCierre() {
        try {
            val typeApp: Byte = 0
            val printOnPos = if (isCommandsMode) {
                binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
            } else {
                false
            }

            val intent = Intent("cl.getnet.payment.action.CLOSE")

            if (isCommandsMode) {
                val closeRequestJson = """
                {
                    "PrintOnPos": $printOnPos,
                    "TypeApp": $typeApp,
                    "PaymentResultTimeout": 2
                }
                """.trimIndent()
                intent.putExtra("params", closeRequestJson)
                Log.d(TAG, "JSON Request: $closeRequestJson")
            } else {
                // En modo librería, CloseRequest solo acepta typeApp
                val request = CloseRequest(typeApp)
                intent.putExtra("params", request)
                Log.d(TAG, "CloseRequest: typeApp=$typeApp")
            }

                startActivityForResult(intent, REQUEST_CODE_CIERRE)


        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CIERRE) {
            val requestData = RequestManager.getCurrentRequest()

            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showCierreResult(this, data, requestData = requestData)
                }

                RESULT_CANCELED -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "CIERRE CANCELADO",
                        onRetry = { solicitarCierre() }
                    )
                }

                else -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "CIERRE RECHAZADO",
                        onRetry = { solicitarCierre() }
                    )
                }
            }
        }
    }
}