package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.SalesdetailRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDetalleVentaBinding

class DetalleVentaActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDetalleVentaBinding
 private var printOnPos: Boolean = true
 private var typeApp: Byte = 0  // Cambiado a Byte
 private var monto: String = ""
 private var ticketNumber: String = ""
 private var employeeId: String = ""

 companion object {
  private const val REQUEST_CODE = 3443

  const val EXTRA_MONTO = "monto"
  const val EXTRA_TICKET_NUMBER = "ticketNumber"
  const val EXTRA_EMPLOYEE_ID = "employeeId"
  const val EXTRA_PRINT_ON_POS = "printOnPos"
  const val EXTRA_TYPE_APP = "typeApp"
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDetalleVentaBinding.inflate(layoutInflater)
  setContentView(binding.root)

  obtenerDatosIntent()
  configurarHeader()
  configurarListeners()
 }

 private fun obtenerDatosIntent() {
  intent?.let {
   monto = it.getStringExtra(EXTRA_MONTO) ?: "0"
   ticketNumber = it.getStringExtra(EXTRA_TICKET_NUMBER) ?: "000000"
   employeeId = it.getStringExtra(EXTRA_EMPLOYEE_ID) ?: "0001"
   printOnPos = it.getBooleanExtra(EXTRA_PRINT_ON_POS, true)
   typeApp = it.getByteExtra(EXTRA_TYPE_APP, 0)  // Cambiado a getByteExtra
  }
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Detalle de Venta",
   showBackButton = true,
   onBackClick = { finish() }
  )

  binding.footerButtons.setButtons(
   primaryText = "CANCELAR",
   secondaryText = "IMPRIMIR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { iniciarDetalleVenta() }
  )
 }

 private fun configurarListeners() {
  // Icono circular
  binding.btnIniciarVenta.setOnClickListener {
   iniciarDetalleVenta()
  }
 }

 private fun iniciarDetalleVenta() {
  try {
   // Crear request con printOnPos y typeApp (Byte)
   val request = SalesdetailRequest(printOnPos, typeApp)

   // Crear intent para la actividad de Getnet
   val intent = Intent("cl.getnet.payment.action.SALES_DETAIL")

   // Agregar el request como extra - IMPORTANTE
   intent.putExtra("params", request)

   // Agregar datos adicionales si es necesario
   intent.putExtra("monto", monto)
   intent.putExtra("ticketNumber", ticketNumber)
   intent.putExtra("employeeId", employeeId)

   // Iniciar actividad para resultado
   startActivityForResult(intent, REQUEST_CODE)

  } catch (e: Exception) {
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
   e.printStackTrace()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  if (requestCode == REQUEST_CODE) {
   val mensaje = when (resultCode) {
    RESULT_OK -> {
     // Procesar resultado exitoso
     val transactionId = data?.getStringExtra("transactionId")
     val authCode = data?.getStringExtra("authCode")
     "Detalle de venta Exitoso!"
    }
    RESULT_CANCELED -> {
     "Detalle de Venta cancelado por el usuario"
    }
    else -> {
     val errorMessage = data?.getStringExtra("error") ?: "Error desconocido"
     "Error en generar detalle de venta: $errorMessage"
    }
   }
  }
 }
}