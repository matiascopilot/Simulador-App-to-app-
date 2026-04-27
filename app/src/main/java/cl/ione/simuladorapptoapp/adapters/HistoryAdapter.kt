package cl.ione.simuladorapptoapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.models.HistoryItem

class HistoryAdapter(
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var items: List<HistoryItem> = emptyList()
    private val TAG = "HistoryAdapter"

    fun submitList(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommandType: TextView = itemView.findViewById(R.id.tvCommandType)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvMode: TextView = itemView.findViewById(R.id.tvMode)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvTicket: TextView = itemView.findViewById(R.id.tvTicket)
        val tvFunctionCode: TextView = itemView.findViewById(R.id.tvFunctionCode)
        val cardView: androidx.cardview.widget.CardView = itemView as androidx.cardview.widget.CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Log.d(TAG, "Binding item at position $position: ${item.functionCode}")

        // Command Type
        holder.tvCommandType.text = when (item.commandType) {
            "Venta" -> "Venta"
            "Anulación" -> "Anulación"
            "Devolución" -> "Devolución"
            "Duplicado" -> "Duplicado"
            "Cierre" -> "Cierre"
            "Detalle Venta" -> "Detalle Venta"
            "Impresión" -> "Impresión"
            else -> item.commandType
        }


        // Date and Mode
        holder.tvDateTime.text = item.getFormattedDate().split(" ")[0]
        holder.tvMode.text = item.mode

        // Amount
        holder.tvAmount.apply {
            text = item.amount?.let { "$${it.formatMoney()}" } ?: ""
            visibility = if (item.amount != null) View.VISIBLE else View.INVISIBLE
        }

        // Ticket
        holder.tvTicket.apply {
            text = item.ticketNumber?.let { "Ticket: $it" } ?: ""
            visibility = if (!item.ticketNumber.isNullOrEmpty()) View.VISIBLE else View.INVISIBLE
        }

        // Function Code
        holder.tvFunctionCode.text = "Comando: ${item.functionCode}"

        // Hacer que todos los TextViews no sean clickeables para que no interfieran
        holder.tvCommandType.isClickable = false
        holder.tvDateTime.isClickable = false
        holder.tvMode.isClickable = false
        holder.tvAmount.isClickable = false
        holder.tvTicket.isClickable = false
        holder.tvFunctionCode.isClickable = false

        // Click listener en la CardView completa
        holder.cardView.setOnClickListener {
            onItemClick(item)
        }

        // También aseguramos que el itemView tenga el listener
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    private fun Int.formatMoney(): String {
        return String.format("%,d", this).replace(",", ".")
    }
}