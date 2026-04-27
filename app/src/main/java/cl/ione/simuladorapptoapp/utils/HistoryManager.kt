package cl.ione.simuladorapptoapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cl.ione.simuladorapptoapp.models.HistoryItem

object HistoryManager {
    private const val PREFS_NAME = "history_prefs"
    private const val KEY_HISTORY = "transaction_history"
    private const val MAX_HISTORY_SIZE = 100

    private val gson = Gson()

    fun saveTransaction(context: Context, item: HistoryItem) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = getHistory(context).toMutableList()

        // Agregar al inicio (más reciente primero)
        history.add(0, item)

        // Limitar tamaño
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }

        saveHistory(prefs, history)
    }

    fun getHistory(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]")
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(prefs: SharedPreferences, history: List<HistoryItem>) {
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun getStats(context: Context): Map<String, Any> {
        val history = getHistory(context)

        val total = history.size
        val aprobadas = history.count { it.result.contains("APROBADA") }
        val rechazadas = history.count { it.result.contains("RECHAZADA") }
        val canceladas = history.count { it.result.contains("CANCELADA") }
        val errores = history.count { it.result.contains("ERROR") }

        val porTipo = history.groupBy { it.commandType }
            .mapValues { it.value.size }

        val totalMonto = history.sumOf { it.amount ?: 0 }
        val montoPromedio = if (total > 0) totalMonto / total else 0

        return mapOf(
            "total" to total,
            "aprobadas" to aprobadas,
            "rechazadas" to rechazadas,
            "canceladas" to canceladas,
            "errores" to errores,
            "tasaExito" to if (total > 0) (aprobadas * 100 / total) else 0,
            "porTipo" to porTipo,
            "totalMonto" to totalMonto,
            "montoPromedio" to montoPromedio
        )
    }
}