package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.DuplicateRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDuplicadoBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager  // ← IMPORTAR
import org.json.JSONObject

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
  setupRequestManager()  // ← NUEVO
  configurarListeners()
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Duplicado JSON" else "Duplicado"

  binding.header.setup(
   title = titulo,
   showBackButton = true,
   showRequestButton = true,
   onBackClick = { finish() }
   // onRequestClick lo maneja RequestManager
  )
 }

 // 🎯 NUEVO: Setup de RequestManager
 private fun setupRequestManager() {
  // 1. Vincular el header con RequestManager
  RequestManager.bind(binding.header)

  // 2. Función que genera el JSON con los valores actuales
  val buildRequestJson = {
   val operationIdString = binding.etOperationId.text.toString()
   val operationId = operationIdString.toIntOrNull() ?: 0
   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0

   if (isCommandsMode) {
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
  }

  // 3. Vincular los campos automáticamente
  val titulo = if (isCommandsMode) "Duplicado JSON" else "Duplicado (Librería)"

  // Bind EditText
  RequestManager.bindEditText(
   binding.etOperationId,
   updateFunction = buildRequestJson,
   title = titulo
  )

  // Bind RadioGroup
  RequestManager.bindRadioGroup(
   binding.rgPrintOnPos,
   updateFunction = buildRequestJson,
   title = titulo
  )

  // 4. Inicializar con valores por defecto
  RequestManager.initWithDefault(buildRequestJson, titulo)
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
    startActivityForResult(intent, REQUEST_CODE_DUPLICADO)
  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  if (requestCode == REQUEST_CODE_DUPLICADO) {
   val requestData = RequestManager.getCurrentRequest()

   when (resultCode) {
    RESULT_OK -> {
     JsonParser.showDuplicadoResult(this, data, requestData = requestData)
    }

    RESULT_CANCELED -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DUPLICADO CANCELADO",
      onRetry = { solicitarDuplicado() }
     )
    }

    else -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DUPLICADO RECHAZADO",
      onRetry = { solicitarDuplicado() }
     )
    }
   }
  }
 }
}