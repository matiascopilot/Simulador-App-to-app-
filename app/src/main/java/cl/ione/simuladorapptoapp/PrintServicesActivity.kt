package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.PrintServiceRequest
import cl.ione.simuladorapptoapp.databinding.ActivityPrintServicesBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PrintServicesActivity : AppCompatActivity() {

 private lateinit var binding: ActivityPrintServicesBinding

 companion object {
  const val EXTRA_DETAIL = "detail"
  const val EXTRA_TYPE_APP = "typeApp"
  private const val TAG = "PrintServices"
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityPrintServicesBinding.inflate(layoutInflater)
  setContentView(binding.root)

  obtenerDatosIntent()
  configurarHeader()
  configurarListeners()
 }

 private fun obtenerDatosIntent() {
  intent?.let {
   val detail = it.getStringExtra(EXTRA_DETAIL)
   val typeApp = it.getByteExtra(EXTRA_TYPE_APP, 0)
   Log.d(TAG, "Datos recibidos - detail: $detail, typeApp: $typeApp")
  }
 }

 private fun configurarHeader() {
  binding.header.setup(
   title = "Servicio de Impresión",
   showBackButton = true,
   onBackClick = { finish() }
  )
  binding.footerButtons.setButtons(
   primaryText = "CANCELAR",
   secondaryText = "IMPRIMIR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { probarImpresionSimple() }
  )
 }

 private fun configurarListeners() {
  binding.btnIniciarImpresion.setOnClickListener {
   probarImpresionSimple()
  }
 }

 private fun probarImpresionSimple() {
  try {
   val jsonArray = JSONArray()

   // Línea 1 - Título
   val linea1 = JSONObject()
   linea1.put("printSeq", 1)
   linea1.put("type", "text")
   linea1.put("encode", "")
   linea1.put("data", "PRUEBA DE IMPRESION GETNET")
   linea1.put("align", "center")
   jsonArray.put(linea1)

   // Línea 2 - Fecha
   val linea2 = JSONObject()
   linea2.put("printSeq", 2)
   linea2.put("type", "text")
   linea2.put("encode", "")
   linea2.put("data", "Fecha: ${getCurrentDateTime()}")
   linea2.put("align", "left")
   jsonArray.put(linea2)

   // Línea 3 - Mensaje
   val linea3 = JSONObject()
   linea3.put("printSeq", 3)
   linea3.put("type", "text")
   linea3.put("encode", "")
   linea3.put("data", "App To App - Simulador")
   linea3.put("align", "left")
   jsonArray.put(linea3)

   // Línea 4 - Separador
   val linea4 = JSONObject()
   linea4.put("printSeq", 4)
   linea4.put("type", "text")
   linea4.put("encode", "")
   linea4.put("data", "----------------------------------------")
   linea4.put("align", "center")
   jsonArray.put(linea4)

   // Línea 5 - Mensaje final
   val linea5 = JSONObject()
   linea5.put("printSeq", 5)
   linea5.put("type", "text")
   linea5.put("encode", "")
   linea5.put("data", "IMPRESION EXITOSA!")
   linea5.put("align", "center")
   jsonArray.put(linea5)

   val detailJsonString = jsonArray.toString()
   val request = PrintServiceRequest(detailJsonString, 0)
   val intent = Intent("cl.getnet.payment.action.PRINT_SERVICE")
   intent.putExtra("params", request)
   sendBroadcast(intent)

   Toast.makeText(this, "Comando de impresión enviado", Toast.LENGTH_SHORT).show()
   Log.d(TAG, "Broadcast enviado: $detailJsonString")

  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
   e.printStackTrace()
  }
 }

 private fun getCurrentDateTime(): String {
  val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
  return dateFormat.format(Date())
 }
}