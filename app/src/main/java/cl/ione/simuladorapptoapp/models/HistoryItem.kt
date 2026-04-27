package cl.ione.simuladorapptoapp.models

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val commandType: String,
    val mode: String,
    val requestData: String,
    val responseData: String,
    val functionCode: Int? = null,
    val result: String,
    val responseCode: String,
    val amount: Int? = null,
    val ticketNumber: String? = null,
    val authorizationCode: String? = null
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }
}