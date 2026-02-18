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
 private val REQUEST_CODE_DUPLICADO = 3446
 private val TAG = "DuplicadoActivity"
 private var isCommandsMode: Boolean = false

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDuplicadoBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  configurarHeader()
  configurarListeners()
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Duplicado JSON" else "Duplicado"
  binding.header.setup(
   title = titulo,
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
   val operationIdString = binding.etOperationId.text.toString()
   if (operationIdString.isEmpty()) {
    Toast.makeText(this, "Ingrese número de operación", Toast.LENGTH_SHORT).show()
    binding.etOperationId.requestFocus()
    return
   }

   val operationId = operationIdString.toIntOrNull()
   if (operationId == null) {
    Toast.makeText(this, "El número de operación debe ser válido", Toast.LENGTH_SHORT).show()
    binding.etOperationId.requestFocus()
    return
   }

   if (operationId <= 0) {
    Toast.makeText(this, "El número de operación debe ser mayor a 0", Toast.LENGTH_SHORT).show()
    binding.etOperationId.requestFocus()
    return
   }

   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0
   val intent = Intent("cl.getnet.payment.action.DUPLICATE")

   if (isCommandsMode) {
    val duplicateRequestJson = """
                {
                    "OperationId": $operationId,
                    "PrintOnPos": $printOnPos,
                    "TypeApp": $typeApp
                }
                """.trimIndent()
    intent.putExtra("params", duplicateRequestJson)
    Log.d(TAG, "JSON Request: $duplicateRequestJson")
   } else {
    val request = DuplicateRequest(operationId, printOnPos, typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "DuplicateRequest: $operationId, $printOnPos, $typeApp")
   }

   val actividades = packageManager.queryIntentActivities(intent, 0)
   if (actividades.isNotEmpty()) {
    startActivityForResult(intent, REQUEST_CODE_DUPLICADO)
   } else {
    Toast.makeText(this,
     "Getnet no está disponible en este dispositivo",
     Toast.LENGTH_LONG).show()
   }

  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)
  if (requestCode == REQUEST_CODE_DUPLICADO) {
   procesarRespuestaDuplicado(resultCode, data)
  }
 }

 private fun procesarRespuestaDuplicado(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showDuplicadoResult(this, data)
   }
   RESULT_CANCELED -> {
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    mostrarResultado("DUPLICADO CANCELADO\n\n$error")
   }
   else -> {
    val errorMsg = data?.getStringExtra("error") ?:
    data?.getStringExtra("message") ?:
    "Error desconocido"
    mostrarResultado("DUPLICADO RECHAZADO\n\nMotivo: $errorMsg")
   }
  }
 }

 private fun mostrarResultado(mensaje: String) {
  android.app.AlertDialog.Builder(this)
   .setTitle("Resultado de Duplicado")
   .setMessage(mensaje)
   .setPositiveButton("ACEPTAR") { _, _ -> finish() }
   .setCancelable(false)
   .show()
 }
}