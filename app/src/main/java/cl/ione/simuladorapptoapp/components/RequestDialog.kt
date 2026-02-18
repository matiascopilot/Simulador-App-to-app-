package cl.ione.simuladorapptoapp.components

import android.app.AlertDialog
import android.content.Context
import cl.ione.simuladorapptoapp.R
import org.json.JSONObject

object RequestDialog {
    private var currentDialog: AlertDialog? = null

    fun show(
        context: Context,
        requestJson: String,
        title: String = "REQUEST JSON"
    ) {
        // Cerrar diálogo anterior si existe
        currentDialog?.dismiss()

        try {
            val formattedJson = formatJson(requestJson)

            currentDialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(formattedJson)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }
                .setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(context, "Error: ${e.message}", requestJson)
        }
    }

    private fun formatJson(json: String): String {
        return try {
            val trimmed = json.trim()
            when {
                trimmed.startsWith("{") -> {
                    val jsonObject = JSONObject(trimmed)
                    jsonObject.toString(2) // 2 espacios de indentación como en JsonParser
                }
                trimmed.startsWith("[") -> {
                    val jsonArray = org.json.JSONArray(trimmed)
                    jsonArray.toString(2)
                }
                else -> json
            }
        } catch (e: Exception) {
            json
        }
    }

    private fun showErrorDialog(context: Context, message: String, data: String?) {
        val errorMessage = "$message\n\nDatos:\n$data"

        AlertDialog.Builder(context)
            .setTitle("ERROR")
            .setMessage(errorMessage)
            .setPositiveButton("ACEPTAR", null)
            .show()
    }

    fun dismissCurrent() {
        currentDialog?.dismiss()
        currentDialog = null
    }
}