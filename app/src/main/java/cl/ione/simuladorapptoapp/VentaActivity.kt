package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.SaleRequest
import cl.ione.simuladorapptoapp.components.FooterButtons
import cl.ione.simuladorapptoapp.components.Header
import cl.ione.simuladorapptoapp.components.JsonParser

class VentaActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3443

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
        footerButtons = findViewById(R.id.footerButtons)
    }

    private fun setupHeader() {
        header.setOnBackClickListener {
            finish()
        }
    }

    private fun setDefaultValues() {
        fun generarMontoPsicologico(): Int {
            val base = when ((1..4).random()) {
                1 -> (1..9).random() * 1000  // 1.000 a 9.000
                2 -> (1..9).random() * 5000  // 5.000 a 45.000
                3 -> (1..9).random() * 10000 // 10.000 a 90.000
                else -> (1..5).random() * 20000 // 20.000 a 100.000
            }
            return if ((1..2).random() == 1) base + 990 else base + 990
        }

        val monto = generarMontoPsicologico()
        etAmount.setText(monto.toString())
        etTicketNumber.setText("${System.currentTimeMillis() % 100000}")
        etEmployeeId.setText("1")
    }

    private fun setupSpinners() {
        val saleTypes = mapOf(
            0 to "Compra",
            1 to "Compra Afecta",
            2 to "Factura Afecta",
            3 to "Compra Exenta",
            4 to "Factura Exenta",
            5 to "Recaudación Afecta",
            6 to "Recaudación Exenta"
        )
        val saleTypeList = saleTypes.entries.map { "${it.key} - ${it.value}" }
        val saleTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, saleTypeList)
        saleTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSaleType.adapter = saleTypeAdapter
        spinnerSaleType.setSelection(1)
    }

    private fun setupFooterButtons() {
        footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "CONFIRMAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { enviarVentaGetnet() }
        )
    }

    private fun enviarVentaGetnet() {
        if (!validarCampos()) {
            return
        }

        try {
            val amount = etAmount.text.toString().toLong()
            val ticketNumber = etTicketNumber.text.toString()
            val printOnPos = rgPrintOnPos.checkedRadioButtonId == R.id.rbPrintYes
            val saleType = spinnerSaleType.selectedItemPosition
            val employeeId = etEmployeeId.text.toString().toInt()

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

            val intent = Intent("cl.getnet.payment.action.SALE")
            intent.putExtra("params", request)

            val actividades = packageManager.queryIntentActivities(intent, 0)
            if (actividades.isNotEmpty()) {
                startActivityForResult(intent, REQUEST_CODE)

            } else {
                Toast.makeText(this,
                    "Getnet no está disponible en este dispositivo\n" +
                            "Contacte al administrador del POS",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validarCampos(): Boolean {
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el monto", Toast.LENGTH_SHORT).show()
            return false
        }

        val amount = amountStr.toLongOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar TicketNumber (máximo 10 caracteres)
        val ticketNumber = etTicketNumber.text.toString()
        if (ticketNumber.isEmpty()) {
            Toast.makeText(this, "Ingrese número de ticket", Toast.LENGTH_SHORT).show()
            return false
        }
        if (ticketNumber.length > 10) {
            Toast.makeText(this, "Ticket máximo 10 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        val employeeId = etEmployeeId.text.toString()
        if (employeeId.isEmpty()) {
            Toast.makeText(this, "Ingrese Employee ID", Toast.LENGTH_SHORT).show()
            return false
        }
        if (employeeId.length > 4) {
            Toast.makeText(this, "Employee ID máximo 4 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }
        if (employeeId.toIntOrNull() == null) {
            Toast.makeText(this, "Employee ID inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // Procesar respuesta exitosa de Getnet
                    procesarRespuestaExitosa(data)
                }
                RESULT_CANCELED -> {
                    mostrarResultado("VENTA CANCELADA\n\nEl usuario canceló la transacción")
                }
                else -> {
                    procesarRespuestaError(data)
                }
            }
        }
    }

    private fun procesarRespuestaExitosa(data: Intent?) {
        JsonParser.showVentaResult(this, data)
    }

    private fun procesarRespuestaError(data: Intent?) {
        val errorMsg = data?.getStringExtra("error") ?:
        data?.getStringExtra("message") ?:
        "Error desconocido (Código: ${data?.getIntExtra("ResponseCode", -1)})"

        mostrarResultado("VENTA RECHAZADA\n\nMotivo: $errorMsg")
    }

    private fun mostrarResultado(mensaje: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Resultado de Venta")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR", null)
            .setCancelable(false)
            .show()
    }
}