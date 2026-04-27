package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.*
import cl.ione.simuladorapptoapp.utils.RutUtils
import org.json.JSONObject

class AnulacionMCActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3443
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = ""

    private lateinit var header: Header
    private lateinit var etRutCommerceSon: RutEditText
    private lateinit var etAmount: EditText
    private lateinit var etOperationId: EditText
    private lateinit var etHostRRN: EditText
    private lateinit var rgPrintOnPos: RadioGroup
    private lateinit var spinnerSaleType: Spinner
    private lateinit var etMti: EditText
    private lateinit var etDe11: EditText
    private lateinit var etDe12: EditText
    private lateinit var etDe13: EditText
    private lateinit var footerButtons: FooterButtons

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anulacion_mc)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        initViews()
        setupHeader()
        setupSpinners()
        setupFooterButtons()
        setupAutoUpdate()
        setDefaultValues()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        etRutCommerceSon = findViewById(R.id.etRutCommerceSon)
        etAmount = findViewById(R.id.etAmount)
        etOperationId = findViewById(R.id.etOperationId)
        etHostRRN = findViewById(R.id.etHostRRN)
        rgPrintOnPos = findViewById(R.id.rgPrintOnPos)
        spinnerSaleType = findViewById(R.id.spinnerSaleType)
        etMti = findViewById(R.id.etMti)
        etDe11 = findViewById(R.id.etDe11)
        etDe12 = findViewById(R.id.etDe12)
        etDe13 = findViewById(R.id.etDe13)
        footerButtons = findViewById(R.id.footerButtons)

        etAmount.setupMoneyFormat()
    }

    private fun setupHeader() {
        RequestManager.bind(header)

        header.setup(
            title = "Anulación Multicomercio",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() },
            onRequestClick = {
                val requestJson = RequestManager.getCurrentRequest()
                if (requestJson.isNotEmpty()) {
                    header.showRequestJson(requestJson, "REQUEST ANULACIÓN MC")
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
            etRutCommerceSon, etAmount, etOperationId, etHostRRN, etMti, etDe11, etDe12, etDe13,
            updateFunction = updateFunction,
            title = "REQUEST ANULACIÓN MC"
        )

        RequestManager.bindRadioGroup(
            rgPrintOnPos,
            updateFunction = updateFunction,
            title = "REQUEST ANULACIÓN MC"
        )

        RequestManager.bindSpinner(
            spinnerSaleType,
            updateFunction = updateFunction,
            title = "REQUEST ANULACIÓN MC"
        )

        RequestManager.initWithDefault(
            updateFunction = updateFunction,
            title = "REQUEST ANULACIÓN MC"
        )
    }

    private fun crearJsonRequest(): JSONObject {
        val amount = etAmount.getCleanMoneyValue()
        val rutCommerceSon = etRutCommerceSon.getCleanRut()
        val operationId = etOperationId.text.toString().toIntOrNull() ?: 0
        val hostRRN = etHostRRN.text.toString()
        val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
        val saleType = spinnerSaleType.selectedItemPosition + 1
        val typeApp = 0

        val mti = etMti.text.toString()
        val de11 = etDe11.text.toString()
        val de12 = etDe12.text.toString()
        val de13 = etDe13.text.toString()

        return JSONObject().apply {
            put("Amount", amount)
            put("RutCommerceSon", rutCommerceSon)
            put("OperationId", operationId)
            put("PrintOnPos", printOnPos)
            put("TypeApp", typeApp)
            put("HostRRN", hostRRN)
            put("SaleType", saleType)

            // OriginalData como objeto anidado
            val originalData = JSONObject().apply {
                put("Mti", mti)
                put("De11", de11)
                put("De12", de12)
                put("De13", de13)
            }
            put("OriginalData", originalData)

            put("PlaceCardToPayTimeout", 20)
            put("PaymentResultTimeout", 2)
        }
    }

    private fun setDefaultValues() {
        // Rellenar con los valores por defecto
        etAmount.setMoneyValue(0)
        etOperationId.setText("")
        etHostRRN.setText("433413000062")
        etMti.setText("0200")
        etDe11.setText("123456")
        etDe12.setText("123456")
        etDe13.setText("123456")
        etRutCommerceSon.setRut("09091125-2") // RUT de ejemplo
        spinnerSaleType.setSelection(0) // SaleType 1 (posición 0)

        // Por defecto PrintOnPos = true (seleccionar Sí)
        rgPrintOnPos.check(R.id.rbPrintYes)
    }

    private fun setupSpinners() {
        val saleTypes = mapOf(
            1 to "Compra Afecta",
            2 to "Factura Afecta",
            3 to "Compra Exenta",
            4 to "Factura Exenta",
            5 to "Recaudación Afecta",
            6 to "Recaudación Exenta"
        )

        val saleTypeList = saleTypes.entries.map { "${it.key} - ${it.value}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, saleTypeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSaleType.adapter = adapter
        spinnerSaleType.setSelection(0)
    }

    private fun setupFooterButtons() {
        footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "CONFIRMAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { enviarAnulacionMC() }
        )
    }

    private fun validarCampos(): Boolean {
        val amount = etAmount.getCleanMoneyValue()
        if (amount <= 0) {
            Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            etAmount.requestFocus()
            return false
        }

        if (!etRutCommerceSon.isValidRut()) {
            Toast.makeText(this, "RUT del comercio hijo no válido", Toast.LENGTH_SHORT).show()
            etRutCommerceSon.requestFocus()
            return false
        }

        val operationId = etOperationId.text.toString()
        if (operationId.isEmpty()) {
            Toast.makeText(this, "Ingrese Operation ID", Toast.LENGTH_SHORT).show()
            etOperationId.requestFocus()
            return false
        }

        val hostRRN = etHostRRN.text.toString()
        if (hostRRN.isEmpty()) {
            Toast.makeText(this, "Ingrese Host RRN", Toast.LENGTH_SHORT).show()
            etHostRRN.requestFocus()
            return false
        }

        return true
    }

    // ✅ Eliminé completamente el método verificarAppGetnet()

    private fun enviarAnulacionMC() {
        if (!validarCampos()) {
            return
        }

        // ✅ Eliminé la verificación de PackageManager

        try {
            val jsonRequest = crearJsonRequest().toString()

            RequestManager.updateRequest(jsonRequest, "REQUEST ANULACIÓN MC ENVIADO")
            currentRequestJson = jsonRequest

            Log.d("ANULACION_MC_DEBUG", "════════════════════════════════")
            Log.d("ANULACION_MC_DEBUG", "📤 ENVIANDO ANULACIÓN MC")
            Log.d("ANULACION_MC_DEBUG", "JSON Enviado:")
            Log.d("ANULACION_MC_DEBUG", jsonRequest)
            Log.d("ANULACION_MC_DEBUG", "════════════════════════════════")

            // Intent limpio, sin verificación previa
            val intent = Intent("cl.getnet.payment.action.CANCELLATION_MC")
            intent.putExtra("params", jsonRequest)
            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("ANULACION_MC_DEBUG", "❌ Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // Transacción exitosa
                    JsonParser.showAnulacionMCResult(
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
                        title = "ANULACIÓN MC CANCELADA",
                        onRetry = { enviarAnulacionMC() },  // Reintentar la misma operación
                        onCancel = { /* Opcional: acción al cancelar */ }
                    )
                }

                else -> {
                    // Otro tipo de error - mostrar error con opción de reintentar
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "ANULACIÓN MC RECHAZADA",
                        onRetry = { enviarAnulacionMC() }
                    )
                }
            }
        }
    }
}