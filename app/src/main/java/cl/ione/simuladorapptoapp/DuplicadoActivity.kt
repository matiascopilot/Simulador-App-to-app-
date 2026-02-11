package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityDuplicadoBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.getnet.payment.interop.parcels.DuplicateRequest

class DuplicadoActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDuplicadoBinding
 private val REQUEST_CODE_DUPLICADO = 3443
 private val TAG = "DuplicadoActivity"

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDuplicadoBinding.inflate(layoutInflater)
  setContentView(binding.root)

  configurarHeader()
  configurarListeners()
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Duplicado de Voucher",
   showBackButton = true,
   onBackClick = { finish() }
  )
 }

 private fun configurarListeners() {
  binding.footerButtons.setButtons(
   primaryText = "VOLVER",
   secondaryText = "CONFIRMAR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { solicitarDuplicado() }
  )
 }

 private fun solicitarDuplicado() {
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
   val request = DuplicateRequest(operationId, printOnPos, typeApp)

   // 6. Crear el intent
   val intent = Intent("cl.getnet.payment.action.DUPLICATE")
   intent.putExtra("params", request)

   // 7. Iniciar actividad
   startActivityForResult(intent, REQUEST_CODE_DUPLICADO)
   Log.d(TAG, "DuplicateRequest - operationId: $operationId, printOnPos: $printOnPos, typeApp: $typeApp")

  } catch (e: Exception) {
   Log.e(TAG, "Error al solicitar duplicado: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
   e.printStackTrace()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  when (requestCode) {
   REQUEST_CODE_DUPLICADO -> {
    procesarRespuestaDuplicado(resultCode, data)
   }
  }
 }

 private fun procesarRespuestaDuplicado(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showDuplicadoResult(this, data)
    binding.etOperationId.isEnabled = false
    binding.rgPrintOnPos.isEnabled = false
   }

   RESULT_CANCELED -> {
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    Log.e(TAG, "Duplicado fallido: $error")
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