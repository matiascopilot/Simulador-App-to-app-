package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.SaleRequest
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.*
import org.json.JSONObject

class VentaActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3443
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = ""

    private lateinit var header: Header
    private lateinit var etAmount: EditText
    private lateinit var etTicketNumber: EditText
    private lateinit var rgPrintOnPos: RadioGroup
    private lateinit var spinnerSaleType: Spinner
    private lateinit var etEmployeeId: EditText
    private lateinit var footerButtons: FooterButtons

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        initViews()
        setupHeader()
        setupSpinners()
        setupFooterButtons()
        setDefaultValues()
        setupRequestManager()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        etAmount = findViewById(R.id.etAmount)
        etTicketNumber = findViewById(R.id.etTicketNumber)
        rgPrintOnPos = findViewById(R.id.rgPrintOnPos)
        spinnerSaleType = findViewById(R.id.spinnerSaleType)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        footerButtons = findViewById(R.id.footerButtons)
        etAmount.setupMoneyFormat()
    }

    private fun setupHeader() {
        val titulo = if (isCommandsMode) "Venta JSON" else "Venta"
        header.setup(
            title = titulo,
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    private fun setupRequestManager() {
        RequestManager.bind(header)
        val buildRequestJson = {
            val amount = etAmount.getCleanMoneyValue()
            val ticketNumber = etTicketNumber.text.toString()
            val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
            val saleType = spinnerSaleType.selectedItemPosition + 1
            val employeeId = etEmployeeId.text.toString().toIntOrNull() ?: 1

            if (isCommandsMode) {
                JSONObject().apply {
                    put("Amount", amount)
                    put("TicketNumber", ticketNumber)
                    put("PrintOnPos", printOnPos)
                    put("SaleType", saleType)
                    put("EmployeeId", employeeId)
                    put("TypeApp", 0)
                    put("RestrictedCards", listOf("12345678", "12345679", "12345670"))
                    put("AllowedCards", listOf("87654321", "87654322", "87654323"))
                    put("PlaceCardToPayTimeout", 20)
                    put("PaymentResultTimeout", 2)
                }
            } else {
                JSONObject().apply {
                    put("Amount", amount)
                    put("TicketNumber", ticketNumber)
                    put("PrintOnPos", printOnPos)
                    put("SaleType", saleType)
                    put("EmployeeId", employeeId)
                    put("TypeApp", 0)
                    put("tcBsan", true)
                    put("tdBsan", true)
                }
            }
        }
        val titulo = if (isCommandsMode) "Venta JSON" else "Venta (Librería)"

        // Bind EditTexts
        RequestManager.bindEditText(
            etAmount, etTicketNumber, etEmployeeId,
            updateFunction = buildRequestJson,
            title = titulo
        )

        // Bind RadioGroup
        RequestManager.bindRadioGroup(
            rgPrintOnPos,
            updateFunction = buildRequestJson,
            title = titulo
        )

        // Bind Spinner
        RequestManager.bindSpinner(
            spinnerSaleType,
            updateFunction = buildRequestJson,
            title = titulo
        )

        // Inicializar con valores por defecto
        RequestManager.initWithDefault(buildRequestJson, titulo)
    }

    private fun setDefaultValues() {
        val monto = generarMontoPsicologico()
        etAmount.setMoneyValue(monto.toLong())
        etTicketNumber.setText("${System.currentTimeMillis() % 100000}")
        etEmployeeId.setText("1")
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

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            saleTypeList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSaleType.adapter = adapter
        spinnerSaleType.setSelection(0)
    }

    private fun setupFooterButtons() {
        footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "PAGAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { enviarVentaGetnet() }
        )
    }

    private fun enviarVentaGetnet() {
        if (!validarCampos()) return

        try {
            val amount = etAmount.getCleanMoneyValue()
            val ticketNumber = etTicketNumber.text.toString()
            val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
            val saleType = spinnerSaleType.selectedItemPosition + 1
            val employeeId = etEmployeeId.text.toString().toInt()

            val intent = Intent("cl.getnet.payment.action.SALE")

            if (isCommandsMode) {
                val saleRequestJson = """
            {
                "Amount": $amount,
                "TicketNumber": "$ticketNumber",
                "PrintOnPos": $printOnPos,
                "SaleType": $saleType,
                "EmployeeId": $employeeId,
                "TypeApp": 0,
                "RestrictedCards": ["12345678", "12345679", "12345670"],
                "AllowedCards": ["87654321", "87654322", "87654323"],
                "PlaceCardToPayTimeout": 20,
                "PaymentResultTimeout": 2
            }
            """.trimIndent()
                intent.putExtra("params", saleRequestJson)
                currentRequestJson = saleRequestJson // Guardar el request
                Log.d("VENTA_REQUEST", "JSON: $saleRequestJson")
            } else {
                val request = SaleRequest(
                    amount = amount,
                    ticketNumber = ticketNumber,
                    printOnPos = printOnPos,
                    saleType = saleType,
                    employeeId = employeeId,
                    typeApp = 0,
                    tcBsan = true,
                    tdBsan = true
                )
                intent.putExtra("params", request)
                currentRequestJson = "Modo Librería - SaleRequest object" // Para referencia
                Log.d(
                    "VENTA_REQUEST",
                    "SaleRequest: $amount, $ticketNumber, $printOnPos, $saleType"
                )
            }

            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("VENTA_REQUEST", "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
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

        val ticketNumber = etTicketNumber.text.toString()
        if (ticketNumber.isEmpty()) {
            Toast.makeText(this, "Ingrese número de ticket", Toast.LENGTH_SHORT).show()
            etTicketNumber.requestFocus()
            return false
        }
        if (ticketNumber.length > 24) {
            Toast.makeText(this, "Ticket Number máximo 24 caracteres", Toast.LENGTH_SHORT).show()
            etTicketNumber.requestFocus()
            return false
        }

        val employeeId = etEmployeeId.text.toString()
        if (employeeId.isEmpty()) {
            Toast.makeText(this, "Ingrese Employee ID", Toast.LENGTH_SHORT).show()
            etEmployeeId.requestFocus()
            return false
        }
        if (employeeId.length > 4) {
            Toast.makeText(this, "Employee ID máximo 4 dígitos", Toast.LENGTH_SHORT).show()
            etEmployeeId.requestFocus()
            return false
        }
        if (employeeId.toIntOrNull() == null) {
            Toast.makeText(this, "Employee ID inválido", Toast.LENGTH_SHORT).show()
            etEmployeeId.requestFocus()
            return false
        }

        return true
    }

    // Crear un archivo separado o agregar en el companion object de la actividad
    companion object {
        fun extraerErrorDelIntent(data: Intent?): String {
            if (data == null) return "No se recibió información de error"

            // Posibles keys donde puede venir el error
            val posiblesKeys = listOf(
                "error", "message", "errorMessage",
                "error_description", "detail", "error_detail",
                "error_code", "status", "response"
            )

            // Buscar el primer key no nulo
            for (key in posiblesKeys) {
                data.getStringExtra(key)?.let {
                    if (it.isNotBlank()) return "$key: $it"
                }
            }

            // Si no encuentra, mostrar todos los extras
            val extras = data.extras
            if (extras != null && extras.size() > 0) {
                val extrasInfo = buildString {
                    append("Extras recibidos:\n")
                    for (key in extras.keySet()) {
                        append("• $key: ${extras.get(key)}\n")
                    }
                }
                return extrasInfo
            }

            return "Error desconocido sin detalles adicionales"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            val requestData = RequestManager.getCurrentRequest()
            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showVentaResult(
                        this,
                        data,
                        isCommandsMode,
                        requestData = requestData
                    )
                }

                RESULT_CANCELED -> {
                    // Usar el nuevo método del JsonParser
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "VENTA CANCELADA",
                        onRetry = { enviarVentaGetnet() }
                    )
                }

                else -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "VENTA RECHAZADA",
                        onRetry = { enviarVentaGetnet() }
                    )
                }
            }
        }
    }
}