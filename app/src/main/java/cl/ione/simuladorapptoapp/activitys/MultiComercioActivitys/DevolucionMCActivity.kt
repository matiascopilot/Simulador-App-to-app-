package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.*
import org.json.JSONObject

class DevolucionMCActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3444 // Diferente al de anulación
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = ""

    private lateinit var header: Header
    private lateinit var etAuthorizationCode: EditText
    private lateinit var etRutCommerceSon: RutEditText
    private lateinit var etAmount: EditText
    private lateinit var rgPrintOnPos: RadioGroup
    private lateinit var footerButtons: FooterButtons

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devolucion_mc)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        initViews()
        setupHeader()
        setupFooterButtons()
        setDefaultValues()
        setupAutoUpdate()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        etAuthorizationCode = findViewById(R.id.etAuthorizationCode)
        etRutCommerceSon = findViewById(R.id.etRutCommerceSon)
        etAmount = findViewById(R.id.etAmount)
        rgPrintOnPos = findViewById(R.id.rgPrintOnPos)
        footerButtons = findViewById(R.id.footerButtons)

        etAmount.setupMoneyFormat()
    }

    private fun setupHeader() {
        RequestManager.bind(header)

        header.setup(
            title = "Devolución Multicomercio",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() },
            onRequestClick = {
                val requestJson = RequestManager.getCurrentRequest()
                if (requestJson.isNotEmpty()) {
                    header.showRequestJson(requestJson, "REQUEST DEVOLUCIÓN MC")
                } else {
                    Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupAutoUpdate() {
        val updateFunction = {
            crearJsonRequest()
        }

        RequestManager.bindEditText(
            etAuthorizationCode, etRutCommerceSon, etAmount,
            updateFunction = updateFunction,
            title = "REQUEST DEVOLUCIÓN MC"
        )

        RequestManager.bindRadioGroup(
            rgPrintOnPos,
            updateFunction = updateFunction,
            title = "REQUEST DEVOLUCIÓN MC"
        )

        RequestManager.initWithDefault(
            updateFunction = updateFunction,
            title = "REQUEST DEVOLUCIÓN MC"
        )
    }

    private fun crearJsonRequest(): JSONObject {
        val authorizationCode = etAuthorizationCode.text.toString()
        val rutCommerceSon = etRutCommerceSon.getCleanRut()
        val amount = etAmount.getCleanMoneyValue()
        val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
        val typeApp = 0

        return JSONObject().apply {
            put("AuthorizationCode", authorizationCode)
            put("RutCommerceSon", rutCommerceSon)
            put("Amount", amount)
            put("PrintOnPos", printOnPos)
            put("TypeApp", typeApp)
            put("PlaceCardToPayTimeout", 20)
            put("PaymentResultTimeout", 2)
        }
    }

    private fun setDefaultValues() {
        // NO generar monto automático - dejar vacío
        etAuthorizationCode.setText("ABCD1234")
        etRutCommerceSon.setRut("09091125-2")
        // etAmount se deja vacío intencionalmente
    }

    private fun setupFooterButtons() {
        footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "CONFIRMAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { enviarDevolucionMC() }
        )
    }

    private fun validarCampos(): Boolean {
        val authorizationCode = etAuthorizationCode.text.toString()
        if (authorizationCode.isEmpty()) {
            Toast.makeText(this, "Ingrese código de autorización", Toast.LENGTH_SHORT).show()
            etAuthorizationCode.requestFocus()
            return false
        }

        if (!etRutCommerceSon.isValidRut()) {
            Toast.makeText(this, "RUT del comercio hijo no válido", Toast.LENGTH_SHORT).show()
            etRutCommerceSon.requestFocus()
            return false
        }

        val amount = etAmount.getCleanMoneyValue()
        if (amount <= 0) {
            Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            etAmount.requestFocus()
            return false
        }

        return true
    }

    private fun enviarDevolucionMC() {
        if (!validarCampos()) {
            return
        }

        try {
            val jsonRequest = crearJsonRequest().toString()

            RequestManager.updateRequest(jsonRequest, "REQUEST DEVOLUCIÓN MC ENVIADO")
            currentRequestJson = jsonRequest

            Log.d("DEVOLUCION_MC_DEBUG", "════════════════════════════════")
            Log.d("DEVOLUCION_MC_DEBUG", "ENVIANDO DEVOLUCIÓN MC")
            Log.d("DEVOLUCION_MC_DEBUG", "JSON Enviado:")
            Log.d("DEVOLUCION_MC_DEBUG", jsonRequest)
            Log.d("DEVOLUCION_MC_DEBUG", "════════════════════════════════")

            val intent = Intent("cl.getnet.payment.action.REFUND_MC")
            intent.putExtra("params", jsonRequest)
            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("DEVOLUCION_MC_DEBUG", "❌ Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // Transacción exitosa
                    JsonParser.showDevolucionMCResult(
                        activity = this,
                        data = data,
                        requestData = currentRequestJson
                    )
                }

                RESULT_CANCELED -> {
                    // Transacción cancelada - mostrar error con opción de reintentar
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "DEVOLUCIÓN MC CANCELADA",
                        onRetry = { enviarDevolucionMC() },  // Reintentar la misma operación
                        onCancel = { /* Opcional: acción al cancelar */ }
                    )
                }

                else -> {
                    // Otro tipo de error - mostrar error con opción de reintentar
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "DEVOLUCIÓN MC RECHAZADA",
                        onRetry = { enviarDevolucionMC() }
                    )
                }
            }
        }
    }
}