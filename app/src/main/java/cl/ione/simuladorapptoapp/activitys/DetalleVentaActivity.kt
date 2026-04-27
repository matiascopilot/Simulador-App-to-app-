package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.SalesdetailRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDetalleVentaBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager  // ← IMPORTAR
import org.json.JSONObject

class DetalleVentaActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDetalleVentaBinding
 private val REQUEST_CODE_DETALLE = 3447
 private val TAG = "DetalleVentaActivity"
 private var isCommandsMode: Boolean = false
 private var printOnPos: Boolean = true
 private var typeApp: Byte = 0

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDetalleVentaBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  obtenerDatosIntent()
  configurarHeader()
  setupRequestManager()  // ← NUEVO
  configurarListeners()
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
   showRequestButton = true,
   onBackClick = { finish() }
   // onRequestClick lo maneja RequestManager
  )
 }

 private fun setupRequestManager() {
  // 1. Vincular el header con RequestManager
  RequestManager.bind(binding.header)

  // 2. Función que genera el JSON con los valores actuales
  val buildRequestJson = {
   if (isCommandsMode) {
    JSONObject().apply {
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   } else {
    JSONObject().apply {
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   }
  }

  // 3. Inicializar con valores por defecto
  val titulo = if (isCommandsMode) "Detalle JSON" else "Detalle de Venta"
  RequestManager.initWithDefault(buildRequestJson, titulo)
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
    startActivityForResult(intent, REQUEST_CODE_DETALLE)
  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  if (requestCode == REQUEST_CODE_DETALLE) {
   val requestData = RequestManager.getCurrentRequest()

   when (resultCode) {
    RESULT_OK -> {
     JsonParser.showDetalleVentaResult(this, data, requestData = requestData)
    }

    RESULT_CANCELED -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DETALLE CANCELADO",
      onRetry = { iniciarDetalleVenta() }
     )
    }

    else -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DETALLE RECHAZADO",
      onRetry = { iniciarDetalleVenta() }
     )
    }
   }
  }
 }
}