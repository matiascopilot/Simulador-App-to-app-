package cl.ione.simuladorapptoapp.activitys.MultiComercioActivitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.*
import cl.ione.simuladorapptoapp.models.CommerceData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject

class VentaMCActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3450
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = ""
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private lateinit var header: Header
    private lateinit var etAmount: EditText
    private lateinit var rgPrintOnPos: RadioGroup
    private lateinit var spinnerSaleType: Spinner
    private lateinit var etRutCommerceSon: RutEditText
    private lateinit var footerButtons: FooterButtons
    private var commerceData: CommerceData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_mc)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        val commerceDataJson = intent.getStringExtra("commerceDataJson")
        commerceData = parseCommerceData(commerceDataJson)

        Log.d("VENTA_MC_DEBUG", "════════════════════════════════")
        Log.d("VENTA_MC_DEBUG", "📱 VentaMCActivity - onCreate")
        Log.d("VENTA_MC_DEBUG", "isCommandsMode: $isCommandsMode")

        if (commerceData != null) {
            Log.d("VENTA_MC_DEBUG", "commerceData RECIBIDO:")
            Log.d("VENTA_MC_DEBUG", "   LegalName: ${commerceData?.LegalName}")
            Log.d("VENTA_MC_DEBUG", "   CommerceNumber: ${commerceData?.CommerceNumber}")
            Log.d("VENTA_MC_DEBUG", "   CommerceRut: ${commerceData?.CommerceRut}")
        } else {
            Log.e("VENTA_MC_DEBUG", "commerceData es NULL - usando valores por defecto")
        }
        Log.d("VENTA_MC_DEBUG", "════════════════════════════════")

        initViews()
        setupHeader()
        setupSpinners()
        setupFooterButtons()
        setDefaultValues()
        setupAutoUpdate()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        etAmount = findViewById(R.id.etAmount)
        rgPrintOnPos = findViewById(R.id.rgPrintOnPos)
        spinnerSaleType = findViewById(R.id.spinnerSaleType)
        etRutCommerceSon = findViewById(R.id.etRutCommerceSon)
        footerButtons = findViewById(R.id.footerButtons)

        etAmount.setupMoneyFormat()

        etRutCommerceSon.setOnRutChangeListener { cleanRut, isValid ->
            Log.d("RUT", "RUT: $cleanRut, válido: $isValid")
        }
    }

    private fun setupHeader() {
        RequestManager.bind(header)

        header.setup(
            title = "Venta Multicomercio",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() },
            onRequestClick = {
                val requestJson = RequestManager.getCurrentRequest()
                if (requestJson.isNotEmpty()) {
                    header.showRequestJson(requestJson, "REQUEST VENTA MC")
                } else {
                    Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupAutoUpdate() {
        val updateFunction = {
            val amount = etAmount.getCleanMoneyValue()
            val rutCommerceSon = etRutCommerceSon.getCleanRut()
            val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
            val saleType = spinnerSaleType.selectedItemPosition + 1
            val typeApp = 0

            JSONObject().apply {
                put("Amount", amount)
                put("RutCommerceSon", rutCommerceSon)
                put("PrintOnPos", printOnPos)
                put("SaleType", saleType)
                put("TypeApp", typeApp)

//                put("TicketNumber", "813")
                put("EmployeeId", "1")

                val restrictedArray = JSONArray()
                restrictedArray.put("12345678")
                restrictedArray.put("12345679")
                restrictedArray.put("12345670")
                put("RestrictedCards", restrictedArray)

                val allowedArray = JSONArray()
                allowedArray.put("87654321")
                allowedArray.put("87654322")
                allowedArray.put("87654323")
                put("AllowedCards", allowedArray)

                put("PlaceCardToPayTimeout", 20)
                put("PaymentResultTimeout", 2)
            }
        }


        RequestManager.bindEditText(
            etAmount, etRutCommerceSon,  // Solo los EditText que existen
            updateFunction = updateFunction,
            title = "REQUEST VENTA MC"
        )

        RequestManager.bindRadioGroup(
            rgPrintOnPos,
            updateFunction = updateFunction,
            title = "REQUEST VENTA MC"
        )

        RequestManager.bindSpinner(
            spinnerSaleType,
            updateFunction = updateFunction,
            title = "REQUEST VENTA MC"
        )

        RequestManager.initWithDefault(
            updateFunction = updateFunction,
            title = "REQUEST VENTA MC"
        )
    }

    private fun crearRequestJson(): String {
        val amount = etAmount.getCleanMoneyValue()
        val rutCommerceSon = etRutCommerceSon.getCleanRut()
        val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
        val saleType = spinnerSaleType.selectedItemPosition + 1

        return JSONObject().apply {
            put("Amount", amount)
            put("RutCommerceSon", rutCommerceSon)
            put("PrintOnPos", printOnPos)
            put("SaleType", saleType)
            put("TypeApp", 0)

            // Campos hardcodeados
//            put("TicketNumber", "813")
            put("EmployeeId", "1")

            val restrictedArray = JSONArray()
            restrictedArray.put("12345678")
            restrictedArray.put("12345679")
            restrictedArray.put("12345670")
            put("RestrictedCards", restrictedArray)

            val allowedArray = JSONArray()
            allowedArray.put("87654321")
            allowedArray.put("87654322")
            allowedArray.put("87654323")
            put("AllowedCards", allowedArray)

            put("PlaceCardToPayTimeout", 20)
            put("PaymentResultTimeout", 2)
        }.toString()
    }

    private fun setDefaultValues() {
        val monto = generarMontoPsicologico()
        etAmount.setMoneyValue(monto.toLong())

        if (commerceData != null && commerceData?.CommerceRut?.isNotEmpty() == true) {
            etRutCommerceSon.setRut(commerceData?.CommerceRut ?: "")
        } else {
            etRutCommerceSon.setRut("09091125-2")
        }
    }

    private fun parseCommerceData(jsonString: String?): CommerceData? {
        if (jsonString.isNullOrEmpty()) return null
        return try {
            gson.fromJson(jsonString, CommerceData::class.java)
        } catch (e: Exception) {
            Log.e("VENTA_MC_DEBUG", "Error parsing JSON: ${e.message}")
            null
        }
    }

    private fun generarMontoPsicologico(): Int {
        val montos = listOf(
            1500, 1990, 2990, 3990, 4990, 5990, 6990, 7990, 8990, 9990,
            10990, 12990, 14990, 15990, 17990, 19990,
            22990, 24990, 27990, 29990,
            32990, 34990, 39990,
            44990, 49990,
            59990, 69990, 79990, 89990, 99990
        )
        return montos.random()
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
            secondaryText = "PAGAR MC",
            onPrimaryClick = { finish() },
            onSecondaryClick = { enviarVentaMC() }
        )
    }

    private fun validarCampos(): Boolean {
        val amount = etAmount.getCleanMoneyValue()
        if (amount <= 0) {
            Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            etAmount.requestFocus()
            return false
        }
        if (amount > 999999999) {
            Toast.makeText(this, "Monto máximo excedido", Toast.LENGTH_SHORT).show()
            etAmount.requestFocus()
            return false
        }

        if (!etRutCommerceSon.isValidRut()) {
            Toast.makeText(this, "RUT del comercio hijo no válido", Toast.LENGTH_SHORT).show()
            etRutCommerceSon.requestFocus()
            return false
        }

        return true
    }

    private fun enviarVentaMC() {
        if (!validarCampos()) {
            return
        }

        try {
            val jsonRequest = crearRequestJson()

            RequestManager.updateRequest(jsonRequest, "REQUEST VENTA MC ENVIADO")
            currentRequestJson = jsonRequest

            Log.d("VENTA_MC_DEBUG", "════════════════════════════════")
            Log.d("VENTA_MC_DEBUG", "ENVIANDO VENTA MC")
            Log.d("VENTA_MC_DEBUG", "JSON Enviado:")
            Log.d("VENTA_MC_DEBUG", jsonRequest)
            Log.d("VENTA_MC_DEBUG", "════════════════════════════════")

            val intent = Intent("cl.getnet.payment.action.SALE_MC")
            intent.putExtra("params", jsonRequest)
            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("VENTA_MC_DEBUG", "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showVentaMCResult(
                        activity = this,
                        data = data,
                        requestData = currentRequestJson
                    )
                }
                RESULT_CANCELED -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "VENTA MC CANCELADA",
                        onRetry = { enviarVentaMC() },
                        onCancel = { /* Opcional */ }
                    )
                }
                else -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "VENTA MC RECHAZADA",
                        onRetry = { enviarVentaMC() }
                    )
                }
            }
        }
    }
}