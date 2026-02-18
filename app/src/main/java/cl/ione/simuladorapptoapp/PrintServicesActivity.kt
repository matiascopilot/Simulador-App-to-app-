package cl.ione.simuladorapptoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.PrintServiceRequest
import cl.ione.simuladorapptoapp.databinding.ActivityPrintServicesBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import java.text.SimpleDateFormat
import java.util.*

class PrintServicesActivity : AppCompatActivity() {

 private lateinit var binding: ActivityPrintServicesBinding
 private val REQUEST_CODE_PRINT = 3449
 private val TAG = "PrintServicesActivity"
 private var isCommandsMode: Boolean = false
 private var typeApp: Byte = 0

 // BroadcastReceiver para recibir la respuesta
 private val mBroadcastReceiver = object : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
   if ("cl.getnet.c2cservice.action.PRINT_SERVICE_RESPONSE" == intent.action) {
    try {
     Log.d(TAG, "=== RESPUESTA RECIBIDA ===")
     val extras = intent.extras
     if (extras != null) {
      for (key in extras.keySet()) {
       Log.d(TAG, "Extra: $key = ${extras.get(key)}")
      }
     }
     JsonParser.showPrintServiceResult(this@PrintServicesActivity, intent)
    } catch (ex: Exception) {
     Log.e(TAG, "Error al recibir respuesta: ${ex.message}", ex)
    }
   }
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityPrintServicesBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)
  obtenerDatosIntent()
  configurarHeader()
  configurarListeners()

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
  } catch (e: Exception) {
   Log.e(TAG, "Error al unregister receiver: ${e.message}")
  }
 }

 private fun obtenerDatosIntent() {
  intent?.let {
   typeApp = it.getByteExtra("typeApp", 0)
  }
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Servicio de Impresión JSON" else "Servicio de Impresión"
  binding.header.setup(
   title = titulo,
   showBackButton = true,
   onBackClick = { finish() }
  )
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

 /**
  * Optimiza la imagen para impresión térmica
  * - Reduce tamaño a máximo 384px de ancho
  * - Convierte a escala de grises
  * - Aumenta contraste
  */
 private fun optimizarImagenParaImpresion(originalBitmap: Bitmap): Bitmap {
  // 1. Redimensionar al ancho máximo de impresora térmica (384px)
  val maxWidth = 384
  val scale = maxWidth.toFloat() / originalBitmap.width.toFloat()
  val newHeight = (originalBitmap.height * scale).toInt()

  val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight, true)

  // 2. Crear bitmap en escala de grises
  val grayBitmap = Bitmap.createBitmap(maxWidth, newHeight, Bitmap.Config.RGB_565)
  val canvas = Canvas(grayBitmap)

  // Fondo blanco
  canvas.drawColor(Color.WHITE)

  // Pintura con filtro de escala de grises
  val paint = Paint()
  val colorMatrix = ColorMatrix()
  colorMatrix.setSaturation(0f)
  paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

  // Dibujar imagen redimensionada
  canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

  // 3. Binarizar (blanco y negro puro) para mejor impresión térmica
  for (x in 0 until maxWidth) {
   for (y in 0 until newHeight) {
    val pixel = grayBitmap.getPixel(x, y)
    val r = Color.red(pixel)
    val g = Color.green(pixel)
    val b = Color.blue(pixel)

    // Calcular luminosidad y aplicar umbral
    val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    val newPixel = if (gray > 128) Color.WHITE else Color.BLACK

    grayBitmap.setPixel(x, y, newPixel)
   }
  }

  // Limpiar bitmap temporal
  scaledBitmap.recycle()

  return grayBitmap
 }

 /**
  * Genera una imagen de texto simple (100% seguro que funciona)
  */
 private fun generarImagenTexto(): Bitmap {
  val width = 200
  val height = 60
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
  val canvas = Canvas(bitmap)

  // Fondo blanco
  canvas.drawColor(Color.WHITE)

  // Texto negro
  val paint = Paint()
  paint.color = Color.BLACK
  paint.textSize = 30f
  paint.isAntiAlias = false
  paint.typeface = android.graphics.Typeface.MONOSPACE

  canvas.drawText("GETNET", 140f, 50f, paint)
  canvas.drawLine(100f, 70f, 280f, 70f, paint)

  return bitmap
 }

 /**
  * Convierte Bitmap a Base64 optimizado
  */
 private fun bitmapToBase64Optimizado(bitmap: Bitmap): String {
  val stream = java.io.ByteArrayOutputStream()

  // Comprimir con calidad reducida para menor tamaño
  bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
  val byteArray = stream.toByteArray()

  Log.d(TAG, "Tamaño de imagen optimizada: ${byteArray.size} bytes")

  return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
 }

 /**
  * Intenta diferentes métodos hasta que uno funcione
  */
 private fun obtenerImagenBase64(): String {
  // Opción 1: Usar imagen de recursos optimizada
  try {
   val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.icono)
   val optimizada = optimizarImagenParaImpresion(originalBitmap)
   val base64 = bitmapToBase64Optimizado(optimizada)
   originalBitmap.recycle()
   optimizada.recycle()
   if (base64.isNotEmpty()) {
    Log.d(TAG, "Usando imagen optimizada desde recurso")
    return base64
   }
  } catch (e: Exception) {
   Log.e(TAG, "Error con imagen de recurso: ${e.message}")
  }

  // Opción 2: Generar imagen de texto simple
  try {
   val textoBitmap = generarImagenTexto()
   val base64 = bitmapToBase64Optimizado(textoBitmap)
   textoBitmap.recycle()
   if (base64.isNotEmpty()) {
    Log.d(TAG, "Usando imagen de texto generada")
    return base64
   }
  } catch (e: Exception) {
   Log.e(TAG, "Error generando imagen texto: ${e.message}")
  }

  // Opción 3: Base64 de punto negro mínimo (fallback)
  Log.d(TAG, "Usando imagen mínima de fallback")
  return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
 }

 private fun enviarImpresion() {
  try {
   val intent = Intent("cl.getnet.payment.action.PRINT_SERVICE")

   val imagenBase64 = obtenerImagenBase64()

   val jsonTest = if (imagenBase64.isEmpty()) {
    """
            [
                {
                    "printSeq": 1,
                    "type": "text",
                    "encode": "",
                    "data": "SIMULADOR APP TO APP",
                    "align": "center"
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
                    "align": "left"
                },
                {
                    "printSeq": 4,
                    "type": "barcode",
                    "encode": "ean13",
                    "data": "123456789012",
                    "align": "center"
                }
            ]
            """.trimIndent()
   } else {
    """
            [
                {
                    "printSeq": 1,
                    "type": "text",
                    "encode": "",
                    "data": "SIMULADOR APP TO APP",
                    "align": "center"
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
                    "align": "left"
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
                    "data": "IMPRESION EXITOSA!",
                    "align": "center"
                }
            ]
            """.trimIndent()
   }

   Log.d(TAG, "JSON con imagen? ${imagenBase64.isNotEmpty()}")
   Log.d(TAG, "Base64 de la imagen (primeros 50 chars): ${imagenBase64.take(50)}...")

   val request = PrintServiceRequest(jsonTest, typeApp)
   intent.putExtra("params", request)

   Log.d(TAG, "Enviando solicitud de impresión")
   sendBroadcast(intent)

   Toast.makeText(this, "Solicitud de impresión enviada", Toast.LENGTH_SHORT).show()

  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }
 private fun getCurrentDateTime(): String {
  val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
  return dateFormat.format(Date())
 }
}