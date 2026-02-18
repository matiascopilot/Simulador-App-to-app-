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
                    activity.finish()
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
                    activity.finish()
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
                    activity.finish()
                }
                .setOnDismissListener {
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
                    activity.finish()
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
            val (status, jsonObject) = parseDuplicadoResponseDetailed(data)

            // Crear un nuevo JSONObject con el orden específico
            val orderedJson = JSONObject()

            // Agregar campos en el orden solicitado
            orderedJson.put("FunctionCode", jsonObject.optInt("FunctionCode", 109))
            orderedJson.put("ResponseCode", jsonObject.optString("ResponseCode", "0"))
            orderedJson.put("ResponseMessage", jsonObject.optString("ResponseMessage", "Aprobado"))
            orderedJson.put("CommerceCode", jsonObject.optLong("CommerceCode", 550062700310))
            orderedJson.put("TerminalId", jsonObject.optString("TerminalId", "ABC1234C"))
            orderedJson.put("Ticket", jsonObject.optString("Ticket", "123456789012345678901234"))
            orderedJson.put("AuthorizationCode", jsonObject.optString("AuthorizationCode", "XZ123456"))
            orderedJson.put("Amount", jsonObject.optInt("Amount", 15000))
            orderedJson.put("SharesNumber", jsonObject.optInt("SharesNumber", 3))
            orderedJson.put("SharesAmount", jsonObject.optInt("SharesAmount", 5000))
            orderedJson.put("Last4Digits", jsonObject.optInt("Last4Digits", 6677))
            orderedJson.put("OperationId", jsonObject.optInt("OperationId", 60))
            orderedJson.put("CardType", jsonObject.optString("CardType", "CR"))
            orderedJson.put("AccountingDate", jsonObject.optString("AccountingDate", "2023-12-28 22:35:12"))
            orderedJson.put("AccountNumber", jsonObject.optString("AccountNumber", "30000000000"))
            orderedJson.put("CardBrand", jsonObject.optString("CardBrand", "AX"))
            orderedJson.put("RealDate", jsonObject.optString("RealDate", "2023-12-28 22:35:12"))
            orderedJson.put("EmployeeId", jsonObject.optInt("EmployeeId", 1))
            orderedJson.put("Tip", jsonObject.optInt("Tip", 1500))
            orderedJson.put("SaleType", jsonObject.optInt("SaleType", 1))
            orderedJson.put("PosMode", jsonObject.optInt("PosMode", 1))
            orderedJson.put("Cashback", jsonObject.optInt("Cashback", 1000))

            val jsonFormatted = orderedJson.toString(2)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                    activity.finish()
                }
                .setOnDismissListener {
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
     * Parsea respuesta de duplicado y retorna el status y el JSONObject
     */
    private fun parseDuplicadoResponseDetailed(data: Intent?): Pair<String, JSONObject> {
        val extras = data?.extras ?: Bundle()
        var jsonObject = JSONObject()

        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                jsonObject = JSONObject(response.toString())
            }
        } else if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                jsonObject = JSONObject(jsonString)
            }
        }

        val status = determineDuplicadoStatusDetailed(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Determina estado del duplicado
     */
    private fun determineDuplicadoStatusDetailed(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")
        val success = jsonObject.optBoolean("Success", false)

        return when {
            success || responseCode == "0" || responseCode == "00" -> "DUPLICADO APROBADO"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "DUPLICADO RECHAZADO - $responseMessage"
            responseCode == "-1" -> "DUPLICADO RECHAZADO"
            else -> "DUPLICADO RECHAZADO (Codigo: $responseCode)"
        }
    }
    /**
     * Procesa y muestra la respuesta de detalle de venta en un diálogo
     */
    fun showDetalleVentaResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted) = parseDetalleVentaResponse(data)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                    activity.finish()
                }
                .setOnDismissListener {
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
     * Parsea respuesta de detalle de venta
     */
    private fun parseDetalleVentaResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        // Caso 1: JSON viene en el campo "response"
        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determineDetalleVentaStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 2: JSON viene en el campo "params"
        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determineDetalleVentaStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 3: Error
        if (extras.containsKey("error")) {
            val error = extras.getString("error", "Error desconocido")
            val jsonObject = JSONObject().apply {
                put("FunctionCode", 105)
                put("ResponseCode", -1)
                put("ResponseMessage", error)
            }
            return Pair("DETALLE DE VENTA FALLIDO", jsonObject.toString(2))
        }

        return Pair("RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina estado del detalle de venta
     */
    private fun determineDetalleVentaStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            responseCode == "0" || responseCode == "00" ||
                    responseMessage.contains("Aprobado", ignoreCase = true) -> "DETALLE DE VENTA EXITOSO"
            jsonObject.has("error") -> "ERROR EN DETALLE DE VENTA"
            else -> "DETALLE DE VENTA RECHAZADO"
        }
    }

    /**
     * Procesa y muestra la respuesta de cierre en un diálogo (versión completa)
     */
    fun showCierreResultCompleto(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val extras = data?.extras ?: Bundle()
            val response = extras.getSerializable("response")

            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val success = jsonObject.optBoolean("Success", false)
                val responseCode = jsonObject.optString("ResponseCode", "-1")
                val responseMessage = jsonObject.optString("ResponseMessage", "")
                val commerceCode = jsonObject.optLong("CommerceCode", 0)
                val terminalId = jsonObject.optString("TerminalId", "")

                // Construir mensaje formateado
                val mensaje = StringBuilder()
                mensaje.appendLine(if (success) "CIERRE EXITOSO" else "CIERRE FALLIDO")
                mensaje.appendLine()
                mensaje.appendLine("DATOS DEL CIERRE:")
                mensaje.appendLine("• Código: $responseCode")
                mensaje.appendLine("• Mensaje: $responseMessage")
                mensaje.appendLine("• Comercio: $commerceCode")
                mensaje.appendLine("• Terminal: $terminalId")

                // Si hay SaleDetails y NO está vacío, mostrar resumen
                if (jsonObject.has("SaleDetails")) {
                    val saleDetails = jsonObject.getJSONArray("SaleDetails")
                    if (saleDetails.length() > 0) {
                        mensaje.appendLine()
                        mensaje.appendLine("VENTAS DEL DÍA: ${saleDetails.length()}")

                        // Calcular total
                        var total = 0L
                        for (i in 0 until saleDetails.length()) {
                            val venta = saleDetails.getJSONObject(i)
                            total += venta.optLong("Amount", 0)
                        }
                        mensaje.appendLine("Total: $$total")
                    } else {
                        mensaje.appendLine()
                        mensaje.appendLine("Sin detalle de ventas (impresión no solicitada)")
                    }
                }

                mensaje.appendLine()
                mensaje.appendLine("JSON COMPLETO:")
                mensaje.appendLine(jsonObject.toString(2))

                currentDialog = android.app.AlertDialog.Builder(activity)
                    .setTitle("Resultado de Cierre")
                    .setMessage(mensaje.toString())
                    .setPositiveButton("ACEPTAR") { dialog, _ ->
                        dialog.dismiss()
                        currentDialog = null
                        activity.finish()
                    }
                    .setOnDismissListener {
                        currentDialog = null
                    }
                    .setCancelable(false)
                    .show()
            } else {
                showCierreResult(activity, data)
            }

        } catch (e: Exception) {
            currentDialog = null
            showCierreResult(activity, data)
        }
    }

    /**
     * Procesa y muestra la respuesta de impresión en un diálogo
     */
    fun showPrintServiceResult(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val resultData = data?.getStringExtra("resultData") ?: "{}"
            val formattedJson = formatJson(resultData)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle("Resultado Impresión")
                .setMessage(formattedJson)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                    activity.finish()
                }
                .setOnDismissListener { currentDialog = null }
                .setCancelable(false)
                .show()

        } catch (e: Exception) {
            currentDialog = null
            showErrorDialog(activity, "Error: ${e.message}", data?.extras?.toString())
        }
    }

    private fun formatJson(jsonString: String): String {
        return try {
            val jsonObject = JSONObject(jsonString)
            jsonObject.toString(4)
        } catch (e: Exception) {
            jsonString
        }
    }

    /**
     * Parsea respuesta de impresión
     */
    private fun parsePrintServiceResponse(data: Intent?): Pair<String, String> {
        val extras = data?.extras ?: Bundle()

        // Caso 1: JSON viene en el campo "response"
        if (extras.containsKey("response")) {
            val response = extras.getSerializable("response")
            if (response != null) {
                val jsonObject = JSONObject(response.toString())
                val status = determinePrintServiceStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 2: JSON viene en el campo "params"
        if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                val jsonObject = JSONObject(jsonString)
                val status = determinePrintServiceStatus(jsonObject)
                val formattedJson = jsonObject.toString(2)
                return Pair(status, formattedJson)
            }
        }

        // Caso 3: Error
        if (extras.containsKey("error")) {
            val error = extras.getString("error", "Error desconocido")
            val jsonObject = JSONObject().apply {
                put("FunctionCode", 117)
                put("ResponseCode", -1)
                put("ResponseMessage", error)
            }
            return Pair("IMPRESIÓN FALLIDA", jsonObject.toString(2))
        }

        return Pair("RESPUESTA DESCONOCIDA", "{}")
    }

    /**
     * Determina estado de la impresión
     */
    private fun determinePrintServiceStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            responseCode == "0" || responseCode == "00" -> "IMPRESIÓN EXITOSA"
            responseMessage.contains("Impresión OK", ignoreCase = true) -> "IMPRESIÓN EXITOSA"
            jsonObject.has("error") -> "ERROR DE IMPRESIÓN"
            else -> "IMPRESIÓN RECHAZADA"
        }
    }

    /**
     * Procesa y muestra la respuesta de devolución con formato detallado
     */
    fun showDevolucionResultFormatted(activity: android.app.Activity, data: Intent?) {
        currentDialog?.dismiss()
        try {
            val extras = data?.extras ?: Bundle()
            val response = extras.getSerializable("response")

            if (response != null) {
                val jsonObject = JSONObject(response.toString())

                val functionCode = jsonObject.optInt("FunctionCode", 108)
                val responseCode = jsonObject.optString("ResponseCode", "-1")
                val responseMessage = jsonObject.optString("ResponseMessage", "")
                val commerceCode = jsonObject.optLong("CommerceCode", 0)
                val terminalId = jsonObject.optString("TerminalId", "")
                val authorizationCode = jsonObject.optString("AuthorizationCode", "")
                val operationId = jsonObject.optInt("OperationID", 0)
                val dateTime = jsonObject.optString("DateTime", "")
                val last4Digits = jsonObject.optString("Last4Digits", "")
                val commerceName = jsonObject.optString("CommerceName", "")

                val mensaje = StringBuilder()
                mensaje.appendLine(if (responseCode == "0") "DEVOLUCIÓN APROBADA" else "DEVOLUCIÓN RECHAZADA")
                mensaje.appendLine()
                mensaje.appendLine("DETALLE DE LA DEVOLUCIÓN:")
                mensaje.appendLine("• Código: $responseCode")
                mensaje.appendLine("• Mensaje: $responseMessage")
                mensaje.appendLine("• Autorización: $authorizationCode")
                mensaje.appendLine("• Operación: $operationId")
                mensaje.appendLine("• Comercio: $commerceName")
                mensaje.appendLine("• Código Comercio: $commerceCode")
                mensaje.appendLine("• Terminal: $terminalId")
                mensaje.appendLine("• Últimos 4 dígitos: $last4Digits")
                mensaje.appendLine("• Fecha: $dateTime")
                mensaje.appendLine()
                mensaje.appendLine("JSON COMPLETO:")
                mensaje.appendLine(jsonObject.toString(2))

                currentDialog = android.app.AlertDialog.Builder(activity)
                    .setTitle("Resultado de Devolución")
                    .setMessage(mensaje.toString())
                    .setPositiveButton("ACEPTAR") { dialog, _ ->
                        dialog.dismiss()
                        currentDialog = null
                        activity.finish()
                    }
                    .setOnDismissListener {
                        currentDialog = null
                    }
                    .setCancelable(false)
                    .show()
            } else {
                showDevolucionResult(activity, data)
            }

        } catch (e: Exception) {
            currentDialog = null
            showDevolucionResult(activity, data)
        }
    }
}