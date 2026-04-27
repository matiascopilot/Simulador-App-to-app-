package cl.ione.simuladorapptoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.models.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

class SectionedHistoryAdapter(
    private val onItemClick: (HistoryItem) -> Unit,
    private val onSectionToggle: (String, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()
    private val expandedSections = mutableSetOf<String>() // Secciones expandidas
    private val TYPE_SECTION = 0
    private val TYPE_ITEM = 1

    // Mapa para saber qué items pertenecen a cada sección
    private val sectionItemsMap = mutableMapOf<String, List<HistoryItem>>()

    // Formato para la fecha de los items (para el título de sección)
    private val sectionDateFormat = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale("es", "ES"))
    private val itemDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submitGroupedData(groupedData: Map<String, List<HistoryItem>>) {
        items.clear()
        sectionItemsMap.clear()

        // Guardar el mapa de sección -> items
        sectionItemsMap.putAll(groupedData)

        // Construir la lista plana con el estado actual de expansión
        rebuildItemsList()
        notifyDataSetChanged()
    }

    private fun rebuildItemsList() {
        items.clear()
        sectionItemsMap.forEach { (date, historyItems) ->
            items.add(date) // Sección (siempre visible)
            if (expandedSections.contains(date)) {
                items.addAll(historyItems) // Items solo si la sección está expandida
            }
        }
    }

    fun toggleSection(sectionDate: String) {
        if (expandedSections.contains(sectionDate)) {
            expandedSections.remove(sectionDate) // Colapsar
        } else {
            expandedSections.add(sectionDate) // Expandir
        }
        rebuildItemsList()
        notifyDataSetChanged()
        onSectionToggle(sectionDate, expandedSections.contains(sectionDate))
    }

    fun setExpandedSections(sections: Set<String>) {
        expandedSections.clear()
        expandedSections.addAll(sections)
        rebuildItemsList()
        notifyDataSetChanged()
    }

    fun getExpandedSections(): Set<String> = expandedSections.toSet()

    fun expandAll() {
        expandedSections.clear()
        expandedSections.addAll(sectionItemsMap.keys)
        rebuildItemsList()
        notifyDataSetChanged()
    }

    fun collapseAll() {
        expandedSections.clear()
        rebuildItemsList()
        notifyDataSetChanged()
    }

    fun isSectionExpanded(sectionDate: String): Boolean = expandedSections.contains(sectionDate)

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        val ivExpandIcon: TextView = itemView.findViewById(R.id.ivExpandIcon)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommandType: TextView = itemView.findViewById(R.id.tvCommandType)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvMode: TextView = itemView.findViewById(R.id.tvMode)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvTicket: TextView = itemView.findViewById(R.id.tvTicket)
        val tvFunctionCode: TextView = itemView.findViewById(R.id.tvFunctionCode)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_SECTION else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SECTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_section, parent, false)
                SectionViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> {
                val sectionTitle = items[position] as String
                bindSection(holder, sectionTitle)
            }
            is ItemViewHolder -> {
                val item = items[position] as HistoryItem
                holder.bind(item)
                holder.itemView.setOnClickListener { onItemClick(item) }
            }
        }
    }

    private fun bindSection(holder: SectionViewHolder, sectionDate: String) {
        // Formatear la fecha para mostrar
        try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(sectionDate)
            holder.tvSectionTitle.text = date?.let {
                capitalizeFirstLetter(sectionDateFormat.format(it))
            } ?: sectionDate
        } catch (e: Exception) {
            holder.tvSectionTitle.text = sectionDate
        }

        // Mostrar contador de items en la sección
        val itemCount = sectionItemsMap[sectionDate]?.size ?: 0
        holder.tvSectionTitle.text = "${holder.tvSectionTitle.text} ($itemCount)"

        // Configurar ícono de expansión/colapso
        val isExpanded = expandedSections.contains(sectionDate)
        holder.ivExpandIcon.text = if (isExpanded) "▼" else "▶"

        // Click listener para expandir/colapsar
        holder.itemView.setOnClickListener {
            toggleSection(sectionDate)
        }
    }

    private fun ItemViewHolder.bind(item: HistoryItem) {
        // Tipo de comando
        tvCommandType.text = when (item.commandType) {
            "VENTA" -> "Venta"
            "ANULACIÓN" -> "Anulación"
            "DEVOLUCIÓN" -> "Devolución"
            "DUPLICADO" -> "Duplicado"
            "CIERRE" -> "Cierre"
            "DETALLE VENTA" -> "Detalle Venta"
            "MAIN POS DATA" -> "Main Pos Data"
            "IMPRESIÓN" -> "Impresión"
            "VENTA MC" -> "Venta MC"
            "ANULACIÓN MC" -> "Anulación MC"
            "DEVOLUCIÓN MC" -> "Devolución MC"
            "DETALLE MC" -> "Detalle MC"
            else -> item.commandType
        }

        // Fecha y hora
        try {
            val date = Date(item.timestamp)
            tvDateTime.text = itemDateFormat.format(date)
        } catch (e: Exception) {
            tvDateTime.text = item.getFormattedDate()
        }

        // Modo (JSON/Librería)
        tvMode.text = "[${item.mode}]"

        // Monto
        tvAmount.text = item.amount?.let { "$${it.formatMoney()}" } ?: ""
        tvAmount.visibility = if (item.amount != null && item.amount > 0) View.VISIBLE else View.GONE

        // Ticket
        tvTicket.text = item.ticketNumber?.let { "Ticket: $it" } ?: ""
        tvTicket.visibility = if (!item.ticketNumber.isNullOrEmpty()) View.VISIBLE else View.GONE

        // Código de función
        tvFunctionCode.text = item.functionCode?.let { "Función: $it" } ?: "Función: N/A"
    }

    override fun getItemCount() = items.size

    private fun Int.formatMoney(): String {
        return String.format("%,d", this).replace(",", ".")
    }

    private fun capitalizeFirstLetter(text: String): String {
        return text.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}