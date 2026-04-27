package cl.ione.simuladorapptoapp.activitys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.PrintServiceRequest
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ActivityPrintServicesBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import cl.ione.simuladorapptoapp.components.ImageProcessor
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PrintServicesActivity : AppCompatActivity() {

 private lateinit var binding: ActivityPrintServicesBinding
 private var isCommandsMode: Boolean = false
 private var typeApp: Byte = 0
 private var imagenBase64Cached: String? = null
 private var isImageReady = false
 private val mainHandler = Handler(Looper.getMainLooper())

 private val mBroadcastReceiver = object : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
   if ("cl.getnet.c2cservice.action.PRINT_SERVICE_RESPONSE" == intent.action) {
    procesarRespuestaImpresion(intent)
   }
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityPrintServicesBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  typeApp = intent.getByteExtra("typeApp", 0)

  configurarHeader()
  setupRequestManager()
  configurarListeners()
  precargarImagen()

  registerReceiver(
   mBroadcastReceiver,
   IntentFilter("cl.getnet.c2cservice.action.PRINT_SERVICE_RESPONSE"),
   Context.RECEIVER_NOT_EXPORTED
  )
 }

 override fun onDestroy() {
  super.onDestroy()
  try {
   unregisterReceiver(mBroadcastReceiver)
  } catch (_: Exception) { }
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Servicio de Impresión",
   showBackButton = true,
   showRequestButton = true,
   onBackClick = { finish() }
  )
 }

 private fun setupRequestManager() {
  RequestManager.bind(binding.header)

  val buildRequestJson = {
   JSONObject().apply {
    put("printData", generarPrintDataJson())
    put("typeApp", typeApp)
   }
  }

  RequestManager.initWithDefault(buildRequestJson, "Servicio de Impresión")
 }

 private fun generarPrintDataJson(): JSONArray {
  var espera = 0
  while (!isImageReady && espera < 20) {
   try {
    Thread.sleep(100)
    espera++
   } catch (_: InterruptedException) {
    Thread.currentThread().interrupt()
    break
   }
  }

  val imagenBase64 = if (isImageReady && !imagenBase64Cached.isNullOrEmpty()) {
   imagenBase64Cached
  } else {
   ImageProcessor.getImagenSimuladorBase64()
  }

  return JSONArray("""
            [
                {
                    "printSeq": 1,
                    "type": "text",
                    "encode": "",
                    "data": "SIMULADOR APP TO APP",
                    "align": "center",
                    "font": "bold"
                },
                {
                    "printSeq": 2,
                    "type": "text",
                    "encode": "",
                    "data": "${getCurrentDateTime()}",
                    "align": "center"
                },
                {
                    "printSeq": 3,
                    "type": "text",
                    "encode": "",
                    "data": "----------------------------------------",
                    "align": "center"
                },
                {
                    "printSeq": 4,
                    "type": "image",
                    "encode": "",
                    "data": "$imagenBase64",
                    "align": "center"
                },
                {
                    "printSeq": 5,
                    "type": "text",
                    "encode": "",
                    "data": "----------------------------------------",
                    "align": "center"
                },
                {
                    "printSeq": 6,
                    "type": "text",
                    "encode": "",
                    "data": "IMPRESION APP TO APP EXITOSA",
                    "align": "center",
                    "font": "bold"
                }
            ]
        """.trimIndent())
 }

 private fun precargarImagen() {
  Thread {
   try {
    val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.icono)
    if (originalBitmap != null) {
     imagenBase64Cached = ImageProcessor.procesarImagenDesdeRecurso(originalBitmap)
     originalBitmap.recycle()
     isImageReady = true
    } else {
     isImageReady = false
    }
   } catch (_: Exception) {
    isImageReady = false
   }
  }.start()
 }

 private fun configurarListeners() {
  binding.footerButtons.setButtons(
   primaryText = "CANCELAR",
   secondaryText = "IMPRIMIR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { enviarImpresion() }
  )

  binding.btnIniciarImpresion.setOnClickListener {
   enviarImpresion()
  }
 }

 private fun enviarImpresion() {
  try {
   val intent = Intent("cl.getnet.payment.action.PRINT_SERVICE")
   val jsonTest = generarPrintDataJson().toString()
   val request = PrintServiceRequest(jsonTest, typeApp)
   intent.putExtra("params", request)
   sendBroadcast(intent)
  } catch (e: Exception) {
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }
 private fun procesarRespuestaImpresion(data: Intent?) {
  val requestData = RequestManager.getCurrentRequest()
  val resultCode = data?.getIntExtra("resultCode", -1) ?: -1

  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showPrintServiceResult(this, data, requestData = requestData)
   }
   RESULT_CANCELED -> {
    JsonParser.showErrorWithRetry(
     activity = this,
     data = data,
     title = "IMPRESIÓN CANCELADA",
     onRetry = { enviarImpresion() }
    )
   }
   else -> {
    JsonParser.showErrorWithRetry(
     activity = this,
     data = data,
     title = "IMPRESIÓN RECHAZADA",
     onRetry = { enviarImpresion() }
    )
   }
  }
 }

 private fun getCurrentDateTime(): String {
  return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
 }
}