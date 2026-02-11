package cl.ione.simuladorapptoapp.components

import android.content.Intent
import android.os.Bundle
import org.json.JSONObject

object JsonParser {
    private var currentDialog: android.app.AlertDialog? = null

    /**
     * Procesa y muestra la respuesta de venta en un diálogo
     * Uso: JsonParser.showVentaResult(activity, data)
     */
    fun showVentaResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseVentaResponse(data)

            // Mostrar diálogo
            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Procesa y muestra la respuesta de cierre en un diálogo
     * Uso: JsonParser.showCierreResult(activity, data)
     */
    fun showCierreResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseCierreResponse(data)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Parsea cualquier respuesta de cierre
     */
    private fun parseCierreResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        // Caso 1: JSON viene en el campo "response" (compatible con startActivityForResult)
        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determineCierreStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 2: JSON viene en el campo "params" como string
        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineCierreStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 3: Error viene en campo "error"
        if (extras.containsKey("error")) {
            val error = extras.getString("error", "Error desconocido")
            val jsonObject = JSONObject().apply {
                put("ResponseCode", "-1")
                put("ResponseMessage", error)
                put("Success", false)
            }
            return Pair("CIERRE FALLIDO", jsonObject.toString(2))
        }

        return Pair("RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina el estado del cierre basado en Success y ResponseCode
     */
    private fun determineCierreStatus(jsonObject: JSONObject): String {
        val success = jsonObject.optBoolean("Success", false)
        val responseCode = jsonObject.optString("ResponseCode", "-1")

        return when {
            success || responseCode == "0" || responseCode == "00" -> "CIERRE EXITOSO"
            jsonObject.has("error") -> "ERROR DE CIERRE"
            else -> "CIERRE RECHAZADO"
        }
    }

    /**
     * VERSIÓN ESPECÍFICA PARA CIERRE CON FORMATO AMIGABLE
     */
    fun showCierreResultFormatted(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val extras = data?.extras ?: Bundle()
            val response = extras.getSerializable("response")
            val jsonObject = if (response != null) {
                JSONObject(response.toString())
            } else {
                JSONObject()
            }

            // Extraer campos específicos
            val success = jsonObject.optBoolean("Success", false)
            val responseCode = jsonObject.optString("ResponseCode", "N/A")
            val responseMessage = jsonObject.optString("ResponseMessage", "Sin mensaje")
            val commerceCode = jsonObject.optLong("CommerceCode", 0)
            val terminalId = jsonObject.optString("TerminalId", "N/A")
            val functionCode = jsonObject.optInt("FunctionCode", 0)

            // Formato amigable
            val friendlyMessage = buildString {
                appendLine(if (success) "CIERRE EXITOSO" else "CIERRE FALLIDO")
                appendLine()
                appendLine("📊 DETALLE DEL CIERRE:")
                appendLine("• Código Respuesta: $responseCode")
                appendLine("• Mensaje: $responseMessage")
                appendLine("• Código Comercio: $commerceCode")
                appendLine("• Terminal ID: $terminalId")
                appendLine("• Función: $functionCode")
                appendLine()
                appendLine("📋 JSON COMPLETO:")
                appendLine(jsonObject.toString(2))
            }

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(if (success) "Cierre de Ventas" else "Error en Cierre")
                .setMessage(friendlyMessage.toString())
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showCierreResult(activity, data) // Fallback al método simple
        }
    }

    /**
     * Parsea cualquier respuesta de venta
     */
    private fun parseVentaResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        // Caso 1: JSON viene en el campo "params" como string
        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 2: JSON viene como múltiples campos individuales
        val jsonObject = JSONObject()
        extras.keySet().forEach { key ->
            if (key != "params") {
                val value = extras.get(key)
                safePut(jsonObject, key, value)
            }
        }

        val status = determineStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Pair(status, formattedJson)
    }

    /**
     * Agrega campos de UI al JSON si se proporcionan
     */
    fun showVentaResultWithUiFields(
        activity: android.app.Activity,
        data: Intent?,
        amount: String = "",
        employeeId: String = "",
        saleType: Int = 0,
        ticket: String = ""
    ) {
        try {
            val (status, jsonFormatted) = parseVentaResponse(data)
            val finalJson = addUiFieldsToJson(jsonFormatted, amount, employeeId, saleType, ticket)

            android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(finalJson)
                .setPositiveButton("ACEPTAR", null)
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Agrega campos de UI al JSON existente
     */
    private fun addUiFieldsToJson(
        jsonString: String,
        amount: String,
        employeeId: String,
        saleType: Int,
        ticket: String
    ): String {
        return try {
            val jsonObject = JSONObject(jsonString)

            if (!jsonObject.has("Amount") && amount.isNotEmpty()) {
                jsonObject.put("Amount", amount.toIntOrNull() ?: 0)
            }
            if (!jsonObject.has("EmployeeId") && employeeId.isNotEmpty()) {
                jsonObject.put("EmployeeId", employeeId.toIntOrNull() ?: 1)
            }
            if (!jsonObject.has("SaleType")) {
                jsonObject.put("SaleType", saleType)
            }
            if (!jsonObject.has("Ticket") && ticket.isNotEmpty()) {
                jsonObject.put("Ticket", ticket)
            }

            jsonObject.toString(2)
        } catch (e: Exception) {
            jsonString
        }
    }

    /**
     * Determina el estado basado en ResponseCode
     */
    private fun determineStatus(jsonObject: JSONObject): String {
        return when (jsonObject.optString("ResponseCode", "-1")) {
            "0", "00" -> "VENTA APROBADA"
            else -> "VENTA RECHAZADA"
        }
    }

    /**
     * Agrega un valor de forma segura al JSONObject
     */
    private fun safePut(jsonObject: JSONObject, key: String, value: Any?) {
        try {
            when (value) {
                is Int -> jsonObject.put(key, value)
                is Long -> jsonObject.put(key, value)
                is String -> jsonObject.put(key, value)
                is Boolean -> jsonObject.put(key, value)
                is Double -> jsonObject.put(key, value)
                is Float -> jsonObject.put(key, value)
                null -> jsonObject.put(key, JSONObject.NULL)
                else -> jsonObject.put(key, value.toString())
            }
        } catch (e: Exception) {
            jsonObject.put(key, value.toString())
        }
    }

    /**
     * Muestra diálogo de error
     */
    private fun showErrorDialog(activity: android.app.Activity, message: String, data: String?) {
        val errorMessage = "$message\n\nDatos recibidos:\n$data"

        android.app.AlertDialog.Builder(activity)
            .setTitle("❌ ERROR")
            .setMessage(errorMessage)
            .setPositiveButton("ACEPTAR", null)
            .show()
    }

    /**
     * Extrae un campo específico del JSON
     */
    fun extractField(jsonString: String, fieldName: String): String {
        return try {
            val jsonObject = JSONObject(jsonString)
            when {
                jsonObject.has(fieldName) -> jsonObject.getString(fieldName)
                else -> "N/A"
            }
        } catch (e: Exception) {
            "ERROR"
        }
    }

    /**
     * Procesa y muestra la respuesta de anulación en un diálogo
     * Uso: JsonParser.showAnulacionResult(activity, data)
     */
    fun showAnulacionResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseAnulacionResponse(data)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Parsea cualquier respuesta de anulación
     */
    private fun parseAnulacionResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        // Caso 1: JSON viene en el campo "response"
        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determineAnulacionStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 2: JSON viene en el campo "params"
        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineAnulacionStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 3: Error
        if (extras.containsKey("error")) {
            val error = extras.getString("error", "Error desconocido")
            val jsonObject = JSONObject().apply {
                put("ResponseCode", "-1")
                put("ResponseMessage", error)
                put("Success", false)
            }
            return Pair("ANULACIÓN FALLIDA", jsonObject.toString(2))
        }

        return Pair("RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina el estado de la anulación
     */
    private fun determineAnulacionStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val success = jsonObject.optBoolean("Success", false)

        return when {
            success || responseCode == "0" || responseCode == "00" -> "ANULACIÓN APROBADA"
            jsonObject.has("error") -> "ERROR DE ANULACIÓN"
            else -> "ANULACIÓN RECHAZADA"
        }
    }

    /**
     * Procesa y muestra la respuesta de devolución en un diálogo
     */
    fun showDevolucionResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseDevolucionResponse(data)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Parsea respuesta de devolución
     */
    private fun parseDevolucionResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determineDevolucionStatus(jsonObject)
                return Pair(status, jsonObject.toString(2))
            }
        }

        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineDevolucionStatus(jsonObject)
                return Pair(status, jsonObject.toString(2))
            }
        }

        return Pair("RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina estado de la devolución
     */
    private fun determineDevolucionStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val success = jsonObject.optBoolean("Success", false)

        return when {
            success || responseCode == "0" || responseCode == "00" -> "DEVOLUCIÓN APROBADA"
            else -> "DEVOLUCIÓN RECHAZADA"
        }
    }
    /**
     * Procesa y muestra la respuesta de duplicado en un diálogo
     */
    fun showDuplicadoResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseDuplicadoResponse(data)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                }.setOnDismissListener {
                    currentDialog = null
                }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    /**
     * Parsea respuesta de duplicado
     */
    private fun parseDuplicadoResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determineDuplicadoStatus(jsonObject)
                return Pair(status, jsonObject.toString(2))
            }
        }

        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineDuplicadoStatus(jsonObject)
                return Pair(status, jsonObject.toString(2))
            }
        }

        return Pair("⚠️ RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina estado del duplicado
     */
    private fun determineDuplicadoStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val success = jsonObject.optBoolean("Success", false)

        return when {
            success || responseCode == "0" || responseCode == "00" -> "✅ DUPLICADO APROBADO"
            else -> "⚠️ DUPLICADO RECHAZADO"
        }
    }
}