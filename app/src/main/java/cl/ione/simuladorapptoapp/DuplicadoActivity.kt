package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.DuplicateRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDuplicadoBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class DuplicadoActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDuplicadoBinding
 private val REQUEST_CODE_DUPLICADO = 3446
 private val TAG = "DuplicadoActivity"
 private var isCommandsMode: Boolean = false
 private var currentRequestJson: String = "" // Para guardar el request actual

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDuplicadoBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  configurarHeader()
  configurarListeners()
  configurarActualizacionRequest() // Configurar actualización automática
  actualizarRequestJson() // Generar request inicial
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Duplicado JSON" else "Duplicado"

  binding.header.setup(
   title = titulo,
   showBackButton = true,
   showRequestButton = true, // Mostrar botón de request
   onBackClick = { finish() },
   onRequestClick = {
    // Mostrar el request actual
    if (currentRequestJson.isNotEmpty()) {
     binding.header.showRequestJson(currentRequestJson, "REQUEST DUPLICADO")
    } else {
     Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
    }
   }
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

 // Configurar actualización automática cuando cambian los campos
 private fun configurarActualizacionRequest() {
  // TextWatcher para campos de texto
  val textWatcher = object : android.text.TextWatcher {
   override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
   override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
   override fun afterTextChanged(s: android.text.Editable?) {
    actualizarRequestJson()
   }
  }

  binding.etOperationId.addTextChangedListener(textWatcher)

  // Listener para RadioGroup
  binding.rgPrintOnPos.setOnCheckedChangeListener { _, _ ->
   actualizarRequestJson()
  }
 }

 // Actualizar el JSON del request con los valores actuales
 private fun actualizarRequestJson() {
  try {
   val operationIdString = binding.etOperationId.text.toString()
   val operationId = operationIdString.toIntOrNull() ?: 0
   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0

   val jsonObject = if (isCommandsMode) {
    JSONObject().apply {
     put("OperationId", operationId)
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   } else {
    JSONObject().apply {
     put("OperationId", operationId)
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   }

   currentRequestJson = jsonObject.toString(4)

  } catch (e: Exception) {
   currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
  }
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
    // Actualizar request después de enviar
    actualizarRequestJson()
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