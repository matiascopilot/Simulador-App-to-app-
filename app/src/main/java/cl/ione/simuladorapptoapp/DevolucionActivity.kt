package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityDevolucionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.getnet.payment.interop.parcels.RefundRequest

class DevolucionActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDevolucionBinding
 private val REQUEST_CODE_DEVOLUCION = 3443
 private val TAG = "DevolucionActivity"

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDevolucionBinding.inflate(layoutInflater)
  setContentView(binding.root)

  configurarHeader()
  configurarListeners()
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Devolución",
   showBackButton = true,
   onBackClick = { finish() }
  )
 }

 private fun configurarListeners() {
  binding.footerButtons.setButtons(
   primaryText = "VOLVER",
   secondaryText = "CONFIRMAR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { solicitarDevolucion() }
  )
 }

 private fun solicitarDevolucion() {
  try {
   // 1. Validar Authorization Code
   val authorizationCode = binding.etAuthorizationCode.text.toString()
   if (authorizationCode.isEmpty()) {
    Toast.makeText(this, "Ingrese código de autorización", Toast.LENGTH_SHORT).show()
    binding.etAuthorizationCode.requestFocus()
    return
   }

   // 2. Validar Monto - CONVERTIR A LONG
   val amountString = binding.etAmount.text.toString()
   if (amountString.isEmpty()) {
    Toast.makeText(this, "Ingrese monto a devolver", Toast.LENGTH_SHORT).show()
    binding.etAmount.requestFocus()
    return
   }

   val amount = amountString.toLongOrNull()  // Cambiado de toIntOrNull() a toLongOrNull()
   if (amount == null || amount <= 0) {
    Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
    binding.etAmount.requestFocus()
    return
   }

   // 3. Obtener valor de PrintOnPos desde RadioGroup
   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id

   // 4. typeApp: 0 = Producción
   val typeApp: Byte = 0

   // 5. Crear el request - amount es Long, no Int
   val request = RefundRequest(authorizationCode, amount, printOnPos, typeApp)

   // 6. Crear el intent
   val intent = Intent("cl.getnet.payment.action.REFUND")
   intent.putExtra("params", request)

   // 7. Iniciar actividad
   startActivityForResult(intent, REQUEST_CODE_DEVOLUCION)
   Log.d(TAG, "RefundRequest - authorizationCode: $authorizationCode, amount: $amount, printOnPos: $printOnPos, typeApp: $typeApp")

  } catch (e: Exception) {
   Log.e(TAG, "Error al solicitar devolución: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
   e.printStackTrace()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  when (requestCode) {
   REQUEST_CODE_DEVOLUCION -> {
    procesarRespuestaDevolucion(resultCode, data)
   }
  }
 }

 private fun procesarRespuestaDevolucion(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showDevolucionResult(this, data)
    binding.etAuthorizationCode.isEnabled = false
    binding.etAmount.isEnabled = false
    binding.rgPrintOnPos.isEnabled = false
   }

   RESULT_CANCELED -> {
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    Log.e(TAG, "Devolución fallida: $error")
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