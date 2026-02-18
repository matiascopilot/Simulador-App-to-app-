package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.CloseRequest
import cl.ione.simuladorapptoapp.databinding.ActivityCierreBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class CierreActivity : AppCompatActivity() {

 private lateinit var binding: ActivityCierreBinding
 private val REQUEST_CODE_CIERRE = 3448
 private val TAG = "CierreActivity"
 private var isCommandsMode: Boolean = false
 private var currentRequestJson: String = "" // Para guardar el request actual

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityCierreBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  configurarHeader()
  configurarListeners()
  actualizarRequestJson() // Generar request inicial
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Cierre JSON" else "Cierre de Caja"

  binding.header.setup(
   title = titulo,
   showBackButton = true,
   showRequestButton = true, // Mostrar botón de request
   onBackClick = { finish() },
   onRequestClick = {
    // Mostrar el request actual
    if (currentRequestJson.isNotEmpty()) {
     binding.header.showRequestJson(currentRequestJson, "REQUEST CIERRE")
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
   onSecondaryClick = { solicitarCierre() }
  )
 }

 // Actualizar el JSON del request con los valores actuales
 private fun actualizarRequestJson() {
  try {
   val typeApp: Byte = 0
   val printOnPos: Boolean = true

   val jsonObject = if (isCommandsMode) {
    JSONObject().apply {
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
     put("PaymentResultTimeout", 2)
    }
   } else {
    JSONObject().apply {
     put("TypeApp", typeApp)
     put("PrintOnPos", printOnPos)
    }
   }

   currentRequestJson = jsonObject.toString(4)

  } catch (e: Exception) {
   currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
  }
 }

 private fun solicitarCierre() {
  try {
   val typeApp: Byte = 0
   val printOnPos: Boolean = true
   val intent = Intent("cl.getnet.payment.action.CLOSE")

   if (isCommandsMode) {
    val closeRequestJson = """
                {
                    "PrintOnPos": $printOnPos,
                    "TypeApp": $typeApp,
                    "PaymentResultTimeout": 2
                }
                """.trimIndent()
    intent.putExtra("params", closeRequestJson)
    Log.d(TAG, "JSON Request: $closeRequestJson")
   } else {
    val request = CloseRequest(typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "CloseRequest: $typeApp")
   }

   val actividades = packageManager.queryIntentActivities(intent, 0)
   if (actividades.isNotEmpty()) {
    startActivityForResult(intent, REQUEST_CODE_CIERRE)
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

  if (requestCode == REQUEST_CODE_CIERRE) {
   procesarRespuestaCierre(resultCode, data)
  }
 }

 private fun procesarRespuestaCierre(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showCierreResult(this, data)
   }
   RESULT_CANCELED -> {
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    mostrarResultado("CIERRE CANCELADO\n\n$error")
   }
   else -> {
    val errorMsg = data?.getStringExtra("error") ?:
    data?.getStringExtra("message") ?:
    "Error desconocido"
    mostrarResultado("CIERRE RECHAZADO\n\nMotivo: $errorMsg")
   }
  }
 }

 private fun mostrarResultado(mensaje: String) {
  android.app.AlertDialog.Builder(this)
   .setTitle("Resultado de Cierre")
   .setMessage(mensaje)
   .setPositiveButton("ACEPTAR") { _, _ -> finish() }
   .setCancelable(false)
   .show()
 }
}