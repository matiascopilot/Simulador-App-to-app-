package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.SalesdetailRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDetalleVentaBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class DetalleVentaActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDetalleVentaBinding
 private val REQUEST_CODE_DETALLE = 3447
 private val TAG = "DetalleVentaActivity"
 private var isCommandsMode: Boolean = false
 private var printOnPos: Boolean = true
 private var typeApp: Byte = 0
 private var currentRequestJson: String = "" // Para guardar el request actual

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDetalleVentaBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  obtenerDatosIntent()
  configurarHeader()
  configurarListeners()
  configurarActualizacionRequest() // Configurar actualización automática
  actualizarRequestJson() // Generar request inicial
 }

 private fun obtenerDatosIntent() {
  intent?.let {
   printOnPos = it.getBooleanExtra("printOnPos", true)
   typeApp = it.getByteExtra("typeApp", 0)
  }
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Detalle JSON" else "Detalle de Venta"

  binding.header.setup(
   title = titulo,
   showBackButton = true,
   showRequestButton = true, // Mostrar botón de request
   onBackClick = { finish() },
   onRequestClick = {
    // Mostrar el request actual
    if (currentRequestJson.isNotEmpty()) {
     binding.header.showRequestJson(currentRequestJson, "REQUEST DETALLE")
    } else {
     Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
    }
   }
  )
 }

 private fun configurarListeners() {
  binding.footerButtons.setButtons(
   primaryText = "CANCELAR",
   secondaryText = "IMPRIMIR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { iniciarDetalleVenta() }
  )

  binding.btnIniciarVenta.setOnClickListener {
   iniciarDetalleVenta()
  }
 }

 // Configurar actualización automática (aunque no hay campos editables, igual generamos el JSON)
 private fun configurarActualizacionRequest() {
  // Como no hay campos editables, solo necesitamos actualizar cuando cambien las variables
  // pero como vienen del intent, no cambian
 }

 // Actualizar el JSON del request con los valores actuales
 private fun actualizarRequestJson() {
  try {
   val jsonObject = if (isCommandsMode) {
    JSONObject().apply {
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   } else {
    JSONObject().apply {
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
     put("originRequestApp", 1)
    }
   }

   currentRequestJson = jsonObject.toString(4)

  } catch (e: Exception) {
   currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
  }
 }

 private fun iniciarDetalleVenta() {
  try {
   val intent = Intent("cl.getnet.payment.action.SALES_DETAIL")

   if (isCommandsMode) {
    val detalleRequestJson = """
                {
                    "PrintOnPos": $printOnPos,
                    "TypeApp": $typeApp
                }
                """.trimIndent()
    intent.putExtra("params", detalleRequestJson)
    Log.d(TAG, "JSON Request: $detalleRequestJson")
   } else {
    val request = SalesdetailRequest(printOnPos, typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "SalesdetailRequest: $printOnPos, $typeApp")
   }

   // Opcional: originRequestApp = 1
   intent.putExtra("originRequestApp", 1)

   val actividades = packageManager.queryIntentActivities(intent, 0)
   if (actividades.isNotEmpty()) {
    startActivityForResult(intent, REQUEST_CODE_DETALLE)
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

  if (requestCode == REQUEST_CODE_DETALLE) {
   JsonParser.showDetalleVentaResult(this, data)
  }
 }
}