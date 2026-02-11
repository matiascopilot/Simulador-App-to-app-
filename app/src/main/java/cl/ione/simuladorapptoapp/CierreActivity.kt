package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityCierreBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.getnet.payment.interop.parcels.CloseRequest

class CierreActivity : AppCompatActivity() {

 private lateinit var binding: ActivityCierreBinding
 private val REQUEST_CODE_CIERRE = 3443
 private val TAG = "CierreActivity"

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityCierreBinding.inflate(layoutInflater)
  setContentView(binding.root)

  configurarHeader()
  configurarListeners()
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Cierre de Caja",
   showBackButton = true,
   onBackClick = { finish() }
  )
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
   val request = CloseRequest(typeApp)
   val intent = Intent("cl.getnet.payment.action.CLOSE")
   intent.putExtra("params", request)
   startActivityForResult(intent, REQUEST_CODE_CIERRE)
   Log.d(TAG, "CloseRequest enviado - typeApp: $typeApp")

  } catch (e: Exception) {
   Log.e(TAG, "Error al solicitar cierre: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
   e.printStackTrace()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  when (requestCode) {
   REQUEST_CODE_CIERRE -> {
    procesarRespuestaCierre(resultCode, data)
   }
  }
 }

 private fun procesarRespuestaCierre(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showCierreResult(this, data)
    Log.d(TAG, "Cierre exitoso - Response: ${data?.getSerializableExtra("response")}")
   }

   RESULT_CANCELED -> {
    // Cierre cancelado o error
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    Log.e(TAG, "Cierre fallido: $error")
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