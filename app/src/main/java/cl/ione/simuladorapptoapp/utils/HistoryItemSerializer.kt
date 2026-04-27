package cl.ione.simuladorapptoapp.utils

import cl.ione.simuladorapptoapp.models.HistoryItem
import org.json.JSONObject

object HistoryItemSerializer {

    fun serialize(item: HistoryItem): String {
        return JSONObject().apply {
            put("id", item.id)
            put("timestamp", item.timestamp)
            put("commandType", item.commandType)
            put("mode", item.mode)
            put("requestData", item.requestData)
            put("responseData", item.responseData)
            put("result", item.result)
            put("responseCode", item.responseCode)
            item.amount?.let { put("amount", it) }
            item.ticketNumber?.let { put("ticketNumber", it) }
            item.authorizationCode?.let { put("authorizationCode", it) }
        }.toString()
    }

    fun deserialize(jsonString: String?): HistoryItem? {
        if (jsonString.isNullOrEmpty()) return null

        return try {
            val json = JSONObject(jsonString)
            HistoryItem(
                id = json.optLong("id", System.currentTimeMillis()),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                commandType = json.optString("commandType", "DESCONOCIDO"),
                mode = json.optString("mode", ""),
                requestData = json.optString("requestData", ""),
                responseData = json.optString("responseData", ""),
                result = json.optString("result", ""),
                responseCode = json.optString("responseCode", "-1"),
                amount = if (json.has("amount")) json.optInt("amount") else null,
                ticketNumber = if (json.has("ticketNumber")) json.optString("ticketNumber") else null,
                authorizationCode = if (json.has("authorizationCode")) json.optString("authorizationCode") else null
            )
        } catch (e: Exception) {
            null
        }
    }
}