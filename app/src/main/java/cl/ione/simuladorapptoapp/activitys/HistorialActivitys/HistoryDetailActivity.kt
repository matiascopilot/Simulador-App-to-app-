package cl.ione.simuladorapptoapp.activitys.HistorialActivitys

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ActivityHistoryDetailBinding
import cl.ione.simuladorapptoapp.models.HistoryItem
import cl.ione.simuladorapptoapp.utils.HistoryItemSerializer
import org.json.JSONObject

class HistoryDetailActivity : AppCompatActivity() {

 private lateinit var binding: ActivityHistoryDetailBinding
 private var requestData: String = ""
 private var responseData: String = ""

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
  setContentView(binding.root)

  val itemJson = intent.getStringExtra("history_item")
  Log.d("HistoryDetail", "JSON recibido: $itemJson")

  val item = HistoryItemSerializer.deserialize(itemJson)

  if (item == null) {
   Log.e("HistoryDetail", "Error: item es null")
   Toast.makeText(this, "Error al cargar detalle", Toast.LENGTH_SHORT).show()
   finish()
   return
  }

  requestData = item.requestData
  responseData = item.responseData

  setupHeader(item)
  mostrarDetalle(item)
  setupClickListeners()
 }

 private fun setupHeader(item: HistoryItem) {
  binding.header.setup(
   title = "Detalle de ${item.commandType}",
   showBackButton = true,
   showRequestButton = false,
   onBackClick = { finish() }
  )
 }

 private fun mostrarDetalle(item: HistoryItem) {
  // Información general
  binding.tvCommandType.text = when (item.commandType) {
   "Venta" -> "Venta"
   "Anulación" -> "Anulación"
   "Devolución" -> "Devolución"
   "Duplicado" -> "Duplicado"
   "Cierre" -> "Cierre"
   else -> item.commandType
  }

  binding.tvDateTime.text = item.getFormattedDate()
  binding.tvMode.text = "Modo: ${item.mode}"

  // Configurar Request
  if (item.requestData.isNotEmpty()) {
   binding.cardRequest.visibility = View.VISIBLE
   mostrarJsonComoPares(JSONObject(item.requestData), binding.containerRequest)
  }

  // Configurar Response
  if (item.responseData.isNotEmpty()) {
   binding.cardResponse.visibility = View.VISIBLE
   mostrarJsonComoPares(JSONObject(item.responseData), binding.containerResponse)
  }
 }

 private fun setupClickListeners() {
  // Click en toda la card de Request
  binding.cardRequest.setOnClickListener {
   mostrarJsonEnDialogo("REQUEST", requestData)
  }

  // Click en toda la card de Response
  binding.cardResponse.setOnClickListener {
   mostrarJsonEnDialogo("RESPONSE", responseData)
  }
 }

 private fun mostrarJsonComoPares(jsonObject: JSONObject, container: LinearLayout) {
  container.removeAllViews()

  // Definir grupos de campos para mejor organización
  val grupos = mapOf(
   "TRANSACCIÓN" to listOf("FunctionCode", "ResponseCode", "ResponseMessage", "OperationId"),
   "MONTO" to listOf("Amount", "Tip", "Cashback", "SharesNumber", "SharesAmount"),
   "TARJETA" to listOf("CardBrand", "CardType", "Last4Digits", "AuthorizationCode"),
   "COMERCIO" to listOf("CommerceCode", "TerminalId", "EmployeeId", "Ticket"),
   "FECHAS" to listOf("AccountingDate", "RealDate"),
   "OTROS" to listOf("SaleType", "PosMode", "PrintOnPos", "TypeApp", "tcBsan", "tdBsan")
  )

  // Agregar espacio superior
  val spacerTop = View(this).apply {
   layoutParams = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    8.dpToPx()
   )
  }
  container.addView(spacerTop)

  // Procesar cada grupo
  grupos.forEach { (tituloGrupo, campos) ->
   val camposVisibles = campos.filter { jsonObject.has(it) }

   if (camposVisibles.isNotEmpty()) {
    // Título del grupo
    val tvTituloGrupo = TextView(this).apply {
     text = tituloGrupo
     textSize = 12f
     setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_secondary))
     setTypeface(null, android.graphics.Typeface.BOLD)
     layoutParams = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
     ).apply {
      setMargins(0, 16.dpToPx(), 0, 8.dpToPx())
     }
    }
    container.addView(tvTituloGrupo)

    // Campos del grupo
    camposVisibles.forEach { key ->
     val value = jsonObject.opt(key)
     agregarFilaCampo(container, key, value)
    }

    // Espacio después del grupo
    val spacerGrupo = View(this).apply {
     layoutParams = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      8.dpToPx()
     )
    }
    container.addView(spacerGrupo)
   }
  }

  // Campos adicionales no incluidos en grupos
  val keysProcesadas = grupos.values.flatten().toSet()
  val keysExtra = jsonObject.keys().asSequence().toList().sorted()
   .filter { !keysProcesadas.contains(it) && jsonObject.has(it) }

  if (keysExtra.isNotEmpty()) {
   val tvTituloExtra = TextView(this).apply {
    text = "OTROS DATOS"
    textSize = 12f
    setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_secondary))
    setTypeface(null, android.graphics.Typeface.BOLD)
    layoutParams = LinearLayout.LayoutParams(
     LinearLayout.LayoutParams.MATCH_PARENT,
     LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
     setMargins(0, 16.dpToPx(), 0, 8.dpToPx())
    }
   }
   container.addView(tvTituloExtra)

   keysExtra.forEach { key ->
    val value = jsonObject.opt(key)
    agregarFilaCampo(container, key, value)
   }
  }
 }

 private fun agregarFilaCampo(container: LinearLayout, key: String, value: Any?) {
  // Crear una fila para cada par clave-valor
  val rowLayout = LinearLayout(this).apply {
   orientation = LinearLayout.HORIZONTAL
   layoutParams = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT
   ).apply {
    setMargins(0, 0, 24.dpToPx(), 12.dpToPx())  // Margen derecho de 24dp
   }
  }

  // TextView para la clave (con ancho fijo para alinear los ":")
  val tvKey = TextView(this).apply {
   text = formatearClave(key)
   textSize = 14f
   setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_secondary))
   layoutParams = LinearLayout.LayoutParams(
    160.dpToPx(),  // Ancho fijo aumentado a 160dp
    LinearLayout.LayoutParams.WRAP_CONTENT
   )
  }

  // TextView para el valor (formateado)
  val tvValue = TextView(this).apply {
   text = formatValueParaVisualizacion(key, value)
   textSize = 14f
   setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_primary))
   setTypeface(null, if (esCampoImportante(key)) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
   layoutParams = LinearLayout.LayoutParams(
    0,
    LinearLayout.LayoutParams.WRAP_CONTENT,
    1f  // Peso 1 para que ocupe el espacio restante
   )
  }

  rowLayout.addView(tvKey)
  rowLayout.addView(tvValue)
  container.addView(rowLayout)
 }

 private fun formatearClave(key: String): String {
  return when (key) {
   "AuthorizationCode" -> "Autorización:"
   "ResponseCode" -> "Código Resp.:"
   "ResponseMessage" -> "Mensaje:"
   "AccountingDate" -> "Fecha Contable:"
   "RealDate" -> "Fecha Real:"
   "CommerceCode" -> "Cód. Comercio:"
   "TerminalId" -> "Terminal ID:"
   "EmployeeId" -> "Empleado ID:"
   "Last4Digits" -> "Últimos 4:"
   "CardBrand" -> "Marca:"
   "CardType" -> "Tipo:"
   "FunctionCode" -> "Función:"
   "OperationId" -> "Operación:"
   "SaleType" -> "Tipo Venta:"
   "PosMode" -> "Modo POS:"
   "SharesNumber" -> "N° Cuotas:"
   "SharesAmount" -> "Valor Cuota:"
   "PrintOnPos" -> "Imprimir:"
   "TicketNumber" -> "Ticket N°:"
   "Ticket" -> "Ticket N°:"
   "TypeApp" -> "Tipo App:"
   "tcBsan" -> "Tarjeta Crédito:"
   "tdBsan" -> "Tarjeta Débito:"
   else -> {
    // Formatear camelCase a palabras
    val formatted = key.replace(Regex("([a-z])([A-Z])"), "$1 $2")
     .replaceFirstChar { it.uppercase() }
    "$formatted:"
   }
  }
 }

 private fun formatValueParaVisualizacion(key: String, value: Any?): String {
  return when {
   // Traducir marcas de tarjeta
   key.equals("CardBrand", ignoreCase = true) && value is String -> {
    when (value) {
     "MC" -> "Mastercard"
     "VS" -> "Visa"
     "AX" -> "American Express"
     "AM" -> "Amex"
     "DC" -> "Diners Club"
     "CS" -> "Discover"
     else -> value
    }
   }
   // Traducir tipos de tarjeta
   key.equals("CardType", ignoreCase = true) && value is String -> {
    when (value) {
     "PR" -> "Prepago"
     "CR" -> "Crédito"
     "DB" -> "Débito"
     else -> value
    }
   }
   // Traducir SaleType
   key.equals("SaleType", ignoreCase = true) && value is Number -> {
    when (value.toInt()) {
     1 -> "Venta"
     2 -> "Devolución"
     3 -> "Anulación"
     else -> "Tipo $value"
    }
   }
   // Booleanos
   key.equals("PrintOnPos", ignoreCase = true) && value is Boolean -> {
    if (value) "Sí" else "No"
   }
   key.equals("tcBsan", ignoreCase = true) && value is Boolean -> {
    if (value) "Sí" else "No"
   }
   key.equals("tdBsan", ignoreCase = true) && value is Boolean -> {
    if (value) "Sí" else "No"
   }
   // Montos con formato de moneda
   key.equals("Amount", ignoreCase = true) && value is Number -> {
    "$${value.toInt().formatMoney()}"
   }
   // Tip y Cashback también como moneda
   (key.equals("Tip", ignoreCase = true) || key.equals("Cashback", ignoreCase = true)) && value is Number -> {
    if (value.toInt() > 0) "$${value.toInt().formatMoney()}" else "$0"
   }
   // AuthorizationCode con formato más compacto
   key.equals("AuthorizationCode", ignoreCase = true) && value is String -> {
    if (value.length > 8) {
     value.takeLast(6)
    } else {
     value
    }
   }
   // Booleanos como texto descriptivo
   value is Boolean -> {
    if (value) "Sí" else "No"
   }
   // Números normales
   value is Number -> value.toString()
   // Strings
   value is String -> if (value.isEmpty()) "—" else value
   // Objetos anidados
   value is JSONObject -> "{...}"
   value is org.json.JSONArray -> "[...]"
   // Null
   value == null -> "—"
   // Otros
   else -> value.toString()
  }
 }

 private fun esCampoImportante(key: String): Boolean {
  return key.equals("Amount", ignoreCase = true) ||
          key.equals("ResponseCode", ignoreCase = true) ||
          key.equals("AuthorizationCode", ignoreCase = true) ||
          key.equals("ResponseMessage", ignoreCase = true)
 }

 // Función de extensión para convertir dp a píxeles
 private fun Int.dpToPx(): Int {
  return (this * resources.displayMetrics.density).toInt()
 }

 private fun agregarTextoPlano(container: LinearLayout, texto: String) {
  container.removeAllViews()

  val tvTexto = TextView(this).apply {
   this.text = texto
   textSize = 12f
   setTypeface(null, android.graphics.Typeface.ITALIC)
   setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_secondary))
   setBackgroundColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.gray))
   setPadding(16, 12, 16, 12)
  }
  container.addView(tvTexto)
 }

 private fun mostrarJsonEnDialogo(titulo: String, jsonString: String) {
  try {
   // Intentar formatear el JSON si es válido
   val jsonFormatted = try {
    val jsonObject = JSONObject(jsonString)
    jsonObject.toString(4)
   } catch (e: Exception) {
    jsonString
   }

   // Crear ScrollView para el contenido
   val scrollView = ScrollView(this)
   val textView = TextView(this).apply {
    text = jsonFormatted
    textSize = 14f
    setTextColor(ContextCompat.getColor(this@HistoryDetailActivity, R.color.text_primary))
    setPadding(24, 24, 24, 24)
   }
   scrollView.addView(textView)

   // Crear y mostrar el diálogo
   AlertDialog.Builder(this)
    .setTitle(titulo)
    .setView(scrollView)
    .setPositiveButton("CERRAR", null)
    .setCancelable(true)
    .show()

  } catch (e: Exception) {
   Toast.makeText(this, "Error al mostrar JSON", Toast.LENGTH_SHORT).show()
  }
 }

 // Función de extensión para formatear montos (solo una vez)
 private fun Int.formatMoney(): String {
  return String.format("%,d", this).replace(",", ".")
 }
}