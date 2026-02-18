package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.*
import cl.ione.simuladorapptoapp.components.FooterButtons
import cl.ione.simuladorapptoapp.components.Header
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.setupMoneyFormat
import cl.ione.simuladorapptoapp.components.getCleanMoneyValue
import cl.ione.simuladorapptoapp.components.setMoneyValue

class VentaMCActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3450
    private var isCommandsMode: Boolean = false

    private lateinit var header: Header
    private lateinit var etAmount: EditText
    private lateinit var etTicketNumber: EditText
    private lateinit var rgPrintOnPos: RadioGroup
    private lateinit var spinnerSaleType: Spinner
    private lateinit var etEmployeeId: EditText
    private lateinit var etIdSucursal: EditText
    private lateinit var etIdTerminal: EditText
    private lateinit var etSerialNumber: EditText
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
            Log.d("VENTA_MC_DEBUG", "✅ commerceData RECIBIDO:")
            Log.d("VENTA_MC_DEBUG", "   rut: ${commerceData?.rut}")
            Log.d("VENTA_MC_DEBUG", "   direccion: ${commerceData?.direccion}")
            Log.d("VENTA_MC_DEBUG", "   ciudad: ${commerceData?.ciudad}")
            Log.d("VENTA_MC_DEBUG", "   razonSocial: ${commerceData?.razonSocial}")
            Log.d("VENTA_MC_DEBUG", "   nombreDeFantasia: ${commerceData?.nombreDeFantasia}")
        } else {
            Log.e("VENTA_MC_DEBUG", "❌ commerceData es NULL - usando valores por defecto")
        }
        Log.d("VENTA_MC_DEBUG", "════════════════════════════════")

        initViews()
        setupHeader()
        setupSpinners()
        setupFooterButtons()
        setDefaultValues()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        etAmount = findViewById(R.id.etAmount)
        etTicketNumber = findViewById(R.id.etTicketNumber)
        rgPrintOnPos = findViewById(R.id.rgPrintOnPos)
        spinnerSaleType = findViewById(R.id.spinnerSaleType)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        etIdSucursal = findViewById(R.id.etIdSucursal)
        etIdTerminal = findViewById(R.id.etIdTerminal)
        etSerialNumber = findViewById(R.id.etSerialNumber)
        footerButtons = findViewById(R.id.footerButtons)

        etAmount.setupMoneyFormat()
    }

    private fun setupHeader() {
        val titulo = if (isCommandsMode) "Venta MC JSON" else "Venta Multicomercio"
        header.setTitle(titulo)
        header.setOnBackClickListener { finish() }
    }

    private fun setDefaultValues() {
        val monto = generarMontoPsicologico()
        etAmount.setMoneyValue(monto.toLong())
        etTicketNumber.setText("${System.currentTimeMillis() % 100000}")
        etEmployeeId.setText("1")
        // Valores por defecto para MC
        etIdSucursal.setText("10606")
        etIdTerminal.setText("80000039")
        etSerialNumber.setText("123456789012")
    }

    private fun parseCommerceData(jsonString: String?): CommerceData? {
        if (jsonString.isNullOrEmpty()) return null

        return try {
            // Usar la clase CommerceData directamente
            CommerceData(
                rut = extractJsonValue(jsonString, "rut"),
                direccion = extractJsonValue(jsonString, "direccion"),
                ciudad = extractJsonValue(jsonString, "ciudad"),
                razonSocial = extractJsonValue(jsonString, "razonSocial"),
                nombreDeFantasia = extractJsonValue(jsonString, "nombreDeFantasia")
            )
        } catch (e: Exception) {
            Log.e("VENTA_MC_DEBUG", "Error parsing JSON: ${e.message}")
            null
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        val pattern = "\"$key\":\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1) ?: ""
    }

    private fun generarMontoPsicologico(): Int {
        val montos = listOf(
            1990, 2990, 3990, 4990, 5990, 6990, 7990, 8990, 9990,
            10990, 12990, 14990, 15990, 17990, 19990,
            22990, 24990, 27990, 29990,
            32990, 34990, 39990,
            44990, 49990,
            59990, 69990, 79990, 89990, 99990,
            149990, 199990, 249990, 299990, 349990
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

        spinnerSaleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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

        if (etTicketNumber.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingrese número de ticket", Toast.LENGTH_SHORT).show()
            etTicketNumber.requestFocus()
            return false
        }

        if (etEmployeeId.text.toString().isEmpty()) {
            Toast.makeText(this, "Ingrese Employee ID", Toast.LENGTH_SHORT).show()
            etEmployeeId.requestFocus()
            return false
        }

        return true
    }

    private fun enviarVentaMC() {
        if (!validarCampos()) return

        try {
            val amount = etAmount.getCleanMoneyValue()
            val ticketNumber = etTicketNumber.text.toString()
            val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
            val saleType = spinnerSaleType.selectedItemPosition + 1
            val employeeId = etEmployeeId.text.toString().toInt()
            val idSucursal = etIdSucursal.text.toString().toInt()
            val idTerminal = etIdTerminal.text.toString().toInt()
            val serialNumber = etSerialNumber.text.toString()

            val intent = Intent("cl.getnet.payment.action.SALEMC")
            intent.setPackage("cl.getnet.payment.devgetnet")

            if (isCommandsMode) {
                val saleMcRequestJson = """
        {
            "Amount": $amount,
            "PrintOnPos": $printOnPos,
            "SaleType": $saleType,
            "TypeApp": 0,
            "RestrictedCards": ["12345678", "12345679", "12345670"],
            "AllowedCards": ["87654321", "87654322", "87654323"],
            "CommerceData": {
                "LegalName": "InteligenciadeNegociosLimitada",
                "CommerceNumber": "12345",
                "CommerceRut": "123456789-5",
                "BranchNumber": "$idSucursal",
                "BranchName": "LasCondes",
                "LittleBranchName": "LC",
                "BranchAddress": "Apoquindo1234",
                "BranchDistrict": "LasCondes",
                "TerminaId": "$idTerminal",
                "SerialNumber": "$serialNumber"
            },
            "CommerceParams": {
                "IndicadorBimoneda": 0,
                "IndicadorBoleta": 0,
                "IndicadorNoVendedor": 0,
                "IndicadorComprobanteComoBoleta": 1,
                "IndicadorPropina": 0,
                "IndicadorVuelto": 0,
                "IndicadorCuotasEmisor": 1,
                "MinimoCuotasEmisor": 2,
                "MaximoCuotasEmisor": 60,
                "IndicadorCuotasComercio": 1,
                "MinimoCuotasComercio": 1,
                "MaximoCuotasComercio": 3,
                "IndicadorCuotasTasaCero": 0,
                "MinimoCuotasTasaCero": 0,
                "MaximoCuotasTasaCero": 0,
                "IndicadorCuotasTasaInteresConocida": 0,
                "MinimoCuotasTasaInteresConocida": 0,
                "MaximoCuotasTasaInteresConocida": 0,
                "TipoProductoCreditoVisa": 1,
                "TipoProductoDebitoVisa": 1,
                "TipoProductoDebitoVisaElectron": 1,
                "TipoProductoPrepagoVisa": 1,
                "TipoProductoCreditoMastercard": 1,
                "TipoProductoDebitoMastercard": 1,
                "TipoProductoDebitoMaestro": 1,
                "TipoProductoPrepagoMastercard": 1,
                "TipoProductoCreditoAmex": 1,
                "TipoProductoDebitoAmex": 1,
                "TipoProductoPrepagoAmex": 1,
                "TipoProductoMagna": 1,
                "NumeroDeFolio": 0,
                "PosAvance": 0
            },
            "PlaceCardToPayTimeout": 20,
            "PaymentResultTimeout": 2
        }
        """.trimIndent()

                intent.putExtra("params", saleMcRequestJson)
                Log.d("VENTA_MC", "JSON: $saleMcRequestJson")

            } else {
                val datosComercio = commerceData ?: CommerceData(
                    rut = "12345678-5",
                    direccion = "Apoquindo 1234",
                    ciudad = "Santiago",
                    razonSocial = "SIM A2A",
                    nombreDeFantasia = "Simulador A2A"
                )
                Log.d("VENTA_MC_DEBUG", "════════════════════════════════")
                Log.d("VENTA_MC_DEBUG", "Enviando Venta MC - Modo COMANDOS")
                Log.d("VENTA_MC_DEBUG", "amount: $amount")
                Log.d("VENTA_MC_DEBUG", "ticketNumber: $ticketNumber")
                Log.d("VENTA_MC_DEBUG", "idSucursal: $idSucursal")
                Log.d("VENTA_MC_DEBUG", "idTerminal: $idTerminal")
                Log.d("VENTA_MC_DEBUG", "serialNumber: $serialNumber")
                Log.d("VENTA_MC_DEBUG", "CommerceData usado:")
                Log.d("VENTA_MC_DEBUG", "   rut: ${datosComercio.rut}")
                Log.d("VENTA_MC_DEBUG", "   direccion: ${datosComercio.direccion}")
                Log.d("VENTA_MC_DEBUG", "   ciudad: ${datosComercio.ciudad}")
                Log.d("VENTA_MC_DEBUG", "   razonSocial: ${datosComercio.razonSocial}")
                Log.d("VENTA_MC_DEBUG", "   nombreFantasia: ${datosComercio.nombreDeFantasia}")
                Log.d("VENTA_MC_DEBUG", "════════════════════════════════")

                val request = SalemcRequest(
                    amount = amount,
                    ticketNumber = ticketNumber,
                    printOnPos = printOnPos,
                    saleType = saleType,
                    employeeId = employeeId,
                    typeApp = 0,
                    tcBsan = true,
                    tdBsan = true,
                    idSucursal = idSucursal,
                    idTerminal = idTerminal,
                    serialNumber = serialNumber,
                    datosComercio = datosComercio
                )

                intent.putExtra("params", request)
                Log.d("VENTA_MC", "SalemcRequest enviado")
            }

            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("VENTA_MC", "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showVentaResult(this, data)
                }
                RESULT_CANCELED -> {
                    mostrarResultado("VENTA MC CANCELADA\n\nEl usuario canceló la transacción")
                }
                else -> {
                    val errorMsg = data?.getStringExtra("error") ?:
                    data?.getStringExtra("message") ?:
                    "Error desconocido"
                    mostrarResultado("VENTA MC RECHAZADA\n\nMotivo: $errorMsg")
                }
            }
        }
    }
    private fun mostrarResultado(mensaje: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Resultado Venta Multicomercio")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}