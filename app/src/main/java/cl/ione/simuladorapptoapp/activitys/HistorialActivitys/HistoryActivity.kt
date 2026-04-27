package cl.ione.simuladorapptoapp.activitys.HistorialActivitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.adapters.SectionedHistoryAdapter
import cl.ione.simuladorapptoapp.databinding.ActivityHistoryBinding
import cl.ione.simuladorapptoapp.models.CommandType
import cl.ione.simuladorapptoapp.models.HistoryItem
import cl.ione.simuladorapptoapp.utils.HistoryManager
import cl.ione.simuladorapptoapp.utils.HistoryItemSerializer
import java.text.SimpleDateFormat
import java.util.*

enum class OrderType {
 RECENT_FIRST,  // Más reciente primero (por defecto)
 OLDEST_FIRST   // Más antiguo primero
}

class HistoryActivity : AppCompatActivity() {

 private lateinit var binding: ActivityHistoryBinding
 private lateinit var adapter: SectionedHistoryAdapter
 private val TAG = "HistoryActivity"
 private var currentOrderType: OrderType = OrderType.RECENT_FIRST
 private var currentFilter: CommandFilter = CommandFilter.ALL

 // Mapa para guardar el estado de expansión por combinación de filtro+orden
 private val expandedStates = mutableMapOf<String, Set<String>>()
 private var currentStateKey: String = ""

 // Filtro de comandos basado en los tipos disponibles
 sealed class CommandFilter {
  object ALL : CommandFilter()
  data class SPECIFIC(val commandType: CommandType) : CommandFilter()
  object OTHER : CommandFilter()

  fun getDisplayName(): String {
   return when (this) {
    is ALL -> "Todas las transacciones"
    is SPECIFIC -> this.commandType.displayName
    is OTHER -> "Otras transacciones"
   }
  }

  fun matches(item: HistoryItem): Boolean {
   return when (this) {
    is ALL -> true
    is SPECIFIC -> {
     val itemCommand = when (item.commandType) {
      "VENTA" -> CommandType.SALE
      "ANULACIÓN" -> CommandType.CANCELLATION
      "DEVOLUCIÓN" -> CommandType.REFUND
      "CIERRE" -> CommandType.CLOSE
      "DETALLE VENTA" -> CommandType.SALES_DETAIL
      "DUPLICADO" -> CommandType.DUPLICATE
      "VENTA MC" -> CommandType.SALE_MC
      "ANULACIÓN MC" -> CommandType.CANCELLATION_MC
      "DEVOLUCIÓN MC" -> CommandType.REFUND_MC
      "DETALLE MC" -> CommandType.SALES_DETAIL_MC
      "MAIN POS DATA" -> CommandType.MAIN_POS_DATA
      "IMPRESIÓN" -> CommandType.PRINT_SERVICE
      else -> null
     }
     itemCommand == this.commandType
    }
    is OTHER -> {
     val knownCommands = setOf(
      "VENTA", "ANULACIÓN", "DEVOLUCIÓN", "CIERRE", "DETALLE VENTA",
      "DUPLICADO", "VENTA MC", "ANULACIÓN MC", "DEVOLUCIÓN MC",
      "DETALLE MC", "MAIN POS DATA", "IMPRESIÓN"
     )
     !knownCommands.contains(item.commandType)
    }
   }
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityHistoryBinding.inflate(layoutInflater)
  setContentView(binding.root)

  setupHeader()
  setupFilterSpinner()
  setupOrderButton()
  setupRecyclerView()
  cargarHistorial()
 }

 override fun onSaveInstanceState(outState: Bundle) {
  super.onSaveInstanceState(outState)
  // Guardar estados expandidos en el bundle
  saveCurrentExpandedState()
  val expandedMapString = expandedStates.map { (key, value) ->
   "$key:${value.joinToString(",")}"
  }.joinToString(";")
  outState.putString("expandedStates", expandedMapString)
  outState.putString("currentStateKey", currentStateKey)
 }

 override fun onRestoreInstanceState(savedInstanceState: Bundle) {
  super.onRestoreInstanceState(savedInstanceState)
  // Restaurar estados expandidos
  val expandedMapString = savedInstanceState.getString("expandedStates", "")
  if (expandedMapString.isNotEmpty()) {
   expandedStates.clear()
   expandedMapString.split(";").forEach { entry ->
    if (entry.contains(":")) {
     val parts = entry.split(":", limit = 2)
     val key = parts[0]
     val sections = parts[1].split(",").filter { it.isNotEmpty() }.toSet()
     expandedStates[key] = sections
    }
   }
  }
  currentStateKey = savedInstanceState.getString("currentStateKey", "")
 }

 private fun setupHeader() {
  binding.header.setup(
   title = "Historial de Transacciones",
   showBackButton = true,
   showRequestButton = false,
   onBackClick = { finish() }
  )
 }

 private fun setupFilterSpinner() {
  // Obtener tipos de comando disponibles del historial
  val history = HistoryManager.getHistory(this)
  val availableFilters = getAvailableFilters(history)

  // Crear lista de opciones para el spinner
  val filterOptions = availableFilters.map { it.getDisplayName() }.toTypedArray()

  val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
  binding.spinnerFilter.adapter = adapter

  binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
   override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    // Guardar estado actual antes de cambiar filtro
    saveCurrentExpandedState()
    currentFilter = availableFilters[position]
    cargarHistorial()
   }

   override fun onNothingSelected(parent: AdapterView<*>?) {
    // No hacer nada
   }
  }
 }

 private fun getAvailableFilters(history: List<HistoryItem>): List<CommandFilter> {
  val filters = mutableListOf<CommandFilter>(CommandFilter.ALL)
  val commandTypes = mutableSetOf<CommandType>()
  var hasOther = false

  history.forEach { item ->
   when (item.commandType) {
    "VENTA" -> commandTypes.add(CommandType.SALE)
    "ANULACIÓN" -> commandTypes.add(CommandType.CANCELLATION)
    "DEVOLUCIÓN" -> commandTypes.add(CommandType.REFUND)
    "CIERRE" -> commandTypes.add(CommandType.CLOSE)
    "DETALLE VENTA" -> commandTypes.add(CommandType.SALES_DETAIL)
    "DUPLICADO" -> commandTypes.add(CommandType.DUPLICATE)
    "VENTA MC" -> commandTypes.add(CommandType.SALE_MC)
    "ANULACIÓN MC" -> commandTypes.add(CommandType.CANCELLATION_MC)
    "DEVOLUCIÓN MC" -> commandTypes.add(CommandType.REFUND_MC)
    "DETALLE MC" -> commandTypes.add(CommandType.SALES_DETAIL_MC)
    "MAIN POS DATA" -> commandTypes.add(CommandType.MAIN_POS_DATA)
    "IMPRESIÓN" -> commandTypes.add(CommandType.PRINT_SERVICE)
    else -> hasOther = true
   }
  }

  // Agregar filtros específicos ordenados por código
  filters.addAll(commandTypes.sortedBy { it.code }.map { CommandFilter.SPECIFIC(it) })

  if (hasOther) {
   filters.add(CommandFilter.OTHER)
  }

  return filters
 }

 private fun setupOrderButton() {
  updateOrderUI()

  binding.layoutOrder.setOnClickListener {
   // Guardar estado actual antes de cambiar
   saveCurrentExpandedState()

   // Cambiar el orden
   currentOrderType = if (currentOrderType == OrderType.RECENT_FIRST) {
    OrderType.OLDEST_FIRST
   } else {
    OrderType.RECENT_FIRST
   }
   updateOrderUI()
   cargarHistorial()
  }
 }

 private fun updateOrderUI() {
  when (currentOrderType) {
   OrderType.RECENT_FIRST -> {
    binding.tvOrderText.text = "Recientes"
    binding.ivOrderIcon.setImageResource(R.drawable.ic_chevron_up)
   }
   OrderType.OLDEST_FIRST -> {
    binding.tvOrderText.text = "Antiguos"
    binding.ivOrderIcon.setImageResource(R.drawable.ic_chevron_down)
   }
  }
 }

 private fun setupRecyclerView() {
  adapter = SectionedHistoryAdapter(
   onItemClick = { historyItem ->
    mostrarDetalleTransaccion(historyItem)
   },
   onSectionToggle = { sectionDate, isExpanded ->
    // Actualizar el estado de expansión en el mapa
    updateSectionExpandedState(sectionDate, isExpanded)
   }
  )

  binding.recyclerView.layoutManager = LinearLayoutManager(this)
  binding.recyclerView.adapter = adapter

  binding.recyclerView.setHasFixedSize(true)
  binding.recyclerView.isNestedScrollingEnabled = true
 }

 private fun cargarHistorial() {
  val history = HistoryManager.getHistory(this)

  // Filtrar según el filtro seleccionado
  val filteredHistory = history.filter { item ->
   currentFilter.matches(item)
  }

  // Ordenar según la selección
  val sortedHistory = when (currentOrderType) {
   OrderType.RECENT_FIRST -> filteredHistory.sortedByDescending { it.timestamp }
   OrderType.OLDEST_FIRST -> filteredHistory.sortedBy { it.timestamp }
  }

  // Agrupar por fecha
  val groupedHistory = sortedHistory.groupBy { item ->
   val date = Date(item.timestamp)
   SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
  }

  // Ordenar grupos por fecha
  val sortedGroups = if (currentOrderType == OrderType.RECENT_FIRST) {
   groupedHistory.toSortedMap(compareByDescending { it.toDate() })
  } else {
   groupedHistory.toSortedMap(compareBy { it.toDate() })
  }

  // Restaurar estado de expansión para esta combinación
  restoreExpandedState(sortedGroups.keys)

  adapter.submitGroupedData(sortedGroups)

  // Mostrar/ocultar empty state
  binding.tvEmpty.visibility = if (filteredHistory.isEmpty()) View.VISIBLE else View.GONE
  binding.recyclerView.visibility = if (filteredHistory.isEmpty()) View.GONE else View.VISIBLE
 }

 private fun String.toDate(): Date {
  return try {
   SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this) ?: Date(0)
  } catch (e: Exception) {
   Date(0)
  }
 }

 // Gestión de estados de expansión
 private fun saveCurrentExpandedState() {
  if (currentStateKey.isNotEmpty()) {
   expandedStates[currentStateKey] = adapter.getExpandedSections()
  }
 }

 private fun restoreExpandedState(availableSections: Set<String>) {
  currentStateKey = when (currentFilter) {
   is CommandFilter.ALL -> "ALL_${currentOrderType.ordinal}"
   is CommandFilter.SPECIFIC -> "${(currentFilter as CommandFilter.SPECIFIC).commandType.code}_${currentOrderType.ordinal}"
   is CommandFilter.OTHER -> "OTHER_${currentOrderType.ordinal}"
  }

  val savedState = expandedStates[currentStateKey]

  if (savedState != null) {
   // Restaurar solo las secciones que aún existen
   val validSections = savedState.filter { it in availableSections }.toSet()
   adapter.setExpandedSections(validSections)
  } else {
   // IMPORTANTE: Si no hay estado guardado, comenzar con TODAS expandidas
   adapter.setExpandedSections(availableSections) // ¡TODAS EXPANDIDAS POR DEFECTO!
  }
 }

 private fun updateSectionExpandedState(sectionDate: String, isExpanded: Boolean) {
  // Guardar el estado actual después de cada toggle
  saveCurrentExpandedState()
 }

 private fun mostrarDetalleTransaccion(item: HistoryItem) {
  try {
   val serialized = HistoryItemSerializer.serialize(item)
   val intent = Intent(this, HistoryDetailActivity::class.java).apply {
    putExtra("history_item", serialized)
   }
   startActivity(intent)
  } catch (e: Exception) {
   Log.e(TAG, "Error starting detail activity", e)
  }
 }
}