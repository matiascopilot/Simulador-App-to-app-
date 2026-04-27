package cl.ione.simuladorapptoapp.components

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import cl.ione.simuladorapptoapp.models.HistoryItem
import cl.ione.simuladorapptoapp.utils.HistoryManager
import org.json.JSONArray
import org.json.JSONObject

object JsonParser {
    private var currentDialog: android.app.AlertDialog? = null

    /**
     * Procesa y muestra la respuesta de venta en un diálogo con formato ordenado
     * Soporta ambos modos: librería (isCommandsMode = false) y JSON (isCommandsMode = true)
     */
    fun showVentaResult(
        activity: android.app.Activity,
        data: Intent?,
        isCommandsMode: Boolean = false,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseVentaResponseDetailed(data)

            val orderedJson = if (isCommandsMode) {
                createJsonModeJson(jsonObject)
            } else {
                createLibreriaModeJson(jsonObject)
            }

            val jsonFormatted = orderedJson.toString(2)
            val historyItem = HistoryItem(
                commandType = "VENTA",
                mode = if (isCommandsMode) "JSON" else "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Crea JSON para modo LIBRERÍA (formato básico)
     */
    private fun createLibreriaModeJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos básicos de la librería - sin valores hardcodeados
        jsonObject.optInt("FunctionCode").takeIf { it != 0 }?.let { orderedJson.put("FunctionCode", it) }
        jsonObject.optString("ResponseCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseCode", it) }
        jsonObject.optString("ResponseMessage").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseMessage", it) }
        jsonObject.optLong("CommerceCode").takeIf { it != 0L }?.let { orderedJson.put("CommerceCode", it) }
        jsonObject.optString("TerminalId").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TerminalId", it) }
        jsonObject.optString("Ticket").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Ticket", it) }
        jsonObject.optString("AuthorizationCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AuthorizationCode", it) }
        jsonObject.optInt("Amount").takeIf { it != 0 }?.let { orderedJson.put("Amount", it) }
        jsonObject.optInt("SharesNumber").takeIf { it != 0 }?.let { orderedJson.put("SharesNumber", it) }
        jsonObject.optInt("SharesAmount").takeIf { it != 0 }?.let { orderedJson.put("SharesAmount", it) }
        jsonObject.optInt("Last4Digits").takeIf { it != 0 }?.let { orderedJson.put("Last4Digits", it) }
        jsonObject.optInt("OperationId").takeIf { it != 0 }?.let { orderedJson.put("OperationId", it) }
        jsonObject.optString("CardType").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardType", it) }
        jsonObject.optString("AccountingDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountingDate", it) }
        jsonObject.optString("AccountNumber").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountNumber", it) }
        jsonObject.optString("CardBrand").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardBrand", it) }
        jsonObject.optString("RealDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("RealDate", it) }
        jsonObject.optInt("EmployeeId").takeIf { it != 0 }?.let { orderedJson.put("EmployeeId", it) }
        jsonObject.optInt("Tip").takeIf { it != 0 }?.let { orderedJson.put("Tip", it) }
        jsonObject.optInt("SaleType").takeIf { it != 0 }?.let { orderedJson.put("SaleType", it) }
        jsonObject.optInt("PosMode").takeIf { it != 0 }?.let { orderedJson.put("PosMode", it) }
        jsonObject.optInt("Cashback").takeIf { it != 0 }?.let { orderedJson.put("Cashback", it) }

        return orderedJson
    }

    /**
     * Crea JSON para modo COMMANDS (formato completo con todos los campos)
     */
    private fun createJsonModeJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos base - sin valores hardcodeados
        jsonObject.optInt("FunctionCode").takeIf { it != 0 }?.let { orderedJson.put("FunctionCode", it) }
        jsonObject.optString("ResponseCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseCode", it) }
        jsonObject.optString("ResponseMessage").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseMessage", it) }
        jsonObject.optLong("CommerceCode").takeIf { it != 0L }?.let { orderedJson.put("CommerceCode", it) }
        jsonObject.optString("TerminalId").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TerminalId", it) }
        jsonObject.optString("Ticket").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Ticket", it) }
        jsonObject.optString("AuthorizationCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AuthorizationCode", it) }
        jsonObject.optInt("Amount").takeIf { it != 0 }?.let { orderedJson.put("Amount", it) }
        jsonObject.optInt("SharesNumber").takeIf { it != 0 }?.let { orderedJson.put("SharesNumber", it) }
        jsonObject.optInt("SharesAmount").takeIf { it != 0 }?.let { orderedJson.put("SharesAmount", it) }
        jsonObject.optInt("Last4Digits").takeIf { it != 0 }?.let { orderedJson.put("Last4Digits", it) }
        jsonObject.optInt("OperationId").takeIf { it != 0 }?.let { orderedJson.put("OperationId", it) }
        jsonObject.optString("CardType").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardType", it) }
        jsonObject.optString("AccountingDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountingDate", it) }
        jsonObject.optString("AccountNumber").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountNumber", it) }
        jsonObject.optString("CardBrand").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardBrand", it) }
        jsonObject.optString("RealDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("RealDate", it) }
        jsonObject.optInt("EmployeeId").takeIf { it != 0 }?.let { orderedJson.put("EmployeeId", it) }
        jsonObject.optInt("Tip").takeIf { it != 0 }?.let { orderedJson.put("Tip", it) }
        jsonObject.optInt("SaleType").takeIf { it != 0 }?.let { orderedJson.put("SaleType", it) }
        jsonObject.optInt("PosMode").takeIf { it != 0 }?.let { orderedJson.put("PosMode", it) }
        jsonObject.optInt("Cashback").takeIf { it != 0 }?.let { orderedJson.put("Cashback", it) }

        // Campos adicionales solo en modo JSON - sin valores hardcodeados
        jsonObject.optString("TransToken").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TransToken", it) }
        jsonObject.optString("ExpiryDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ExpiryDate", it) }
        jsonObject.optString("EntryMode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("EntryMode", it) }
        jsonObject.optString("Aid").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Aid", it) }
        jsonObject.optString("CommerceRut").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CommerceRut", it) }
        jsonObject.optString("CommerceName").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CommerceName", it) }
        jsonObject.optString("BranchName").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchName", it) }
        jsonObject.optString("BranchAddress").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchAddress", it) }
        jsonObject.optString("BranchDistrict").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchDistrict", it) }
        jsonObject.optString("Bin").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Bin", it) }

        return orderedJson
    }

    /**
     * Extrae el mensaje de error de un Intent de forma consistente
     */
    fun extractErrorMessage(data: Intent?, includeDetails: Boolean = true): String {
        if (data == null) return "No se recibió información de error"

        // Intentar obtener params como String (formato JSON)
        val paramsString = data.getStringExtra("params")

        if (!paramsString.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(paramsString)
                val responseMessage = jsonObject.optString("ResponseMessage", "")
                val responseCode = jsonObject.optString("ResponseCode", "")

                if (responseMessage.isNotEmpty()) {
                    return if (includeDetails && responseCode.isNotEmpty() && responseCode != "0") {
                        "Error $responseCode: $responseMessage"
                    } else {
                        responseMessage
                    }
                }
            } catch (e: Exception) {
                Log.e("JsonParser", "Error parseando JSON: ${e.message}")
            }
        }

        // Buscar en otras keys comunes
        val errorMsg = data.getStringExtra("error") ?:
        data.getStringExtra("message") ?:
        data.getStringExtra("errorMessage") ?:
        data.getStringExtra("error_description")

        return if (!errorMsg.isNullOrEmpty()) errorMsg else "Error desconocido"
    }

    /**
     * Muestra un diálogo de error con opción de reintentar
     */
    fun showErrorWithRetry(
        activity: android.app.Activity,
        data: Intent?,
        title: String = "ERROR EN TRANSACCIÓN",
        onRetry: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val errorMessage = extractErrorMessage(data, includeDetails = true)

        android.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage("$errorMessage\n\n¿Desea intentar nuevamente?")
            .setPositiveButton("REINTENTAR") { dialog, _ ->
                dialog.dismiss()
                onRetry()
            }
            .setNegativeButton("CANCELAR") { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Muestra un diálogo de error simple (solo información, sin reintentar)
     */
    fun showErrorSimple(
        activity: android.app.Activity,
        data: Intent?,
        title: String = "ERROR",
        onDismiss: (() -> Unit)? = null
    ) {
        val errorMessage = extractErrorMessage(data, includeDetails = true)

        android.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton("ACEPTAR") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(false)
            .show()
    }
    /**
     * Parsea respuesta de venta y retorna el status y el JSONObject
     */
    private fun parseVentaResponseDetailed(data: Intent?): Pair<String, JSONObject> {
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
        } else {
            jsonObject = buildJsonFromExtras(extras)
        }

        val status = determineVentaStatusDetailed(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Construye JSON a partir de los extras individuales
     */
    private fun buildJsonFromExtras(extras: Bundle): JSONObject {
        val jsonObject = JSONObject()
        val keys = extras.keySet()

        for (key in keys) {
            try {
                when (val value = extras.get(key)) {
                    is Int -> jsonObject.put(key, value)
                    is Long -> jsonObject.put(key, value)
                    is Double -> jsonObject.put(key, value)
                    is Boolean -> jsonObject.put(key, value)
                    is String -> jsonObject.put(key, value)
                    else -> jsonObject.put(key, value?.toString())
                }
            } catch (e: Exception) {
            }
        }
        return jsonObject
    }

    /**
     * Determina estado de la venta
     */
    private fun determineVentaStatusDetailed(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")
        val functionCode = jsonObject.optInt("FunctionCode", -1)

        return when {
            responseCode == "0" || responseCode == "00" -> "VENTA APROBADA"
            functionCode == 109 -> "DUPLICADO DE VENTA"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "VENTA RECHAZADA - $responseMessage"
            responseCode == "-1" -> "VENTA RECHAZADA"
            else -> "VENTA RECHAZADA (Código: $responseCode)"
        }
    }

    /**
     * Procesa y muestra la respuesta de Venta Multicomercio en un diálogo con formato ordenado
     */
    fun showVentaMCResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseVentaMCResponseDetailed(data)

            // Crear JSON ordenado específico para Venta MC
            val orderedJson = createVentaMCJson(jsonObject)

            val jsonFormatted = orderedJson.toString(2)

            // Guardar en historial
            val historyItem = HistoryItem(
                commandType = "VENTA MC",
                mode = "JSON",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Crea JSON ordenado específico para Venta MC con todos los campos requeridos
     */
    private fun createVentaMCJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos principales de respuesta - solo si existen en el JSON original
        jsonObject.optInt("FunctionCode").takeIf { it != 0 }?.let { orderedJson.put("FunctionCode", it) }
        jsonObject.optString("ResponseCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseCode", it) }
        jsonObject.optString("ResponseMessage").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseMessage", it) }
        jsonObject.optLong("CommerceCode").takeIf { it != 0L }?.let { orderedJson.put("CommerceCode", it) }
        jsonObject.optString("TerminalId").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TerminalId", it) }
        jsonObject.optString("Ticket").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Ticket", it) }
        jsonObject.optString("AuthorizationCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AuthorizationCode", it) }
        jsonObject.optInt("Amount").takeIf { it != 0 }?.let { orderedJson.put("Amount", it) }
        jsonObject.optInt("SharesNumber").takeIf { it != 0 }?.let { orderedJson.put("SharesNumber", it) }
        jsonObject.optInt("SharesAmount").takeIf { it != 0 }?.let { orderedJson.put("SharesAmount", it) }
        jsonObject.optString("Last4Digits").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Last4Digits", it) }
        jsonObject.optInt("OperationId").takeIf { it != 0 }?.let { orderedJson.put("OperationId", it) }
        jsonObject.optString("CardType").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardType", it) }
        jsonObject.optString("AccountingDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountingDate", it) }
        jsonObject.optString("AccountNumber").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountNumber", it) }
        jsonObject.optString("CardBrand").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardBrand", it) }
        jsonObject.optString("RealDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("RealDate", it) }
        jsonObject.optInt("EmployeeId").takeIf { it != 0 }?.let { orderedJson.put("EmployeeId", it) }
        jsonObject.optInt("Tip").takeIf { it != 0 }?.let { orderedJson.put("Tip", it) }
        jsonObject.optInt("SaleType").takeIf { it != 0 }?.let { orderedJson.put("SaleType", it) }
        jsonObject.optInt("PosMode").takeIf { it != 0 }?.let { orderedJson.put("PosMode", it) }
        jsonObject.optInt("Cashback").takeIf { it != 0 }?.let { orderedJson.put("Cashback", it) }

        // Campos específicos de Venta MC - solo si existen
        jsonObject.optString("TransToken").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TransToken", it) }
        jsonObject.optString("ExpiryDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ExpiryDate", it) }
        jsonObject.optString("EntryMode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("EntryMode", it) }
        jsonObject.optString("AID").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AID", it) }
        jsonObject.optString("CommerceRut").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CommerceRut", it) }
        jsonObject.optString("CommerceName").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CommerceName", it) }
        jsonObject.optString("BranchName").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchName", it) }
        jsonObject.optString("BranchAddress").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchAddress", it) }
        jsonObject.optString("BranchDistrict").takeIf { it.isNotEmpty() }?.let { orderedJson.put("BranchDistrict", it) }
        jsonObject.optString("Bin").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Bin", it) }

        // Campos ISO - solo si existen
        jsonObject.optString("hostRRN").takeIf { it.isNotEmpty() }?.let { orderedJson.put("hostRRN", it) }
        jsonObject.optString("mti").takeIf { it.isNotEmpty() }?.let { orderedJson.put("mti", it) }
        jsonObject.optString("de11").takeIf { it.isNotEmpty() }?.let { orderedJson.put("de11", it) }
        jsonObject.optString("de12").takeIf { it.isNotEmpty() }?.let { orderedJson.put("de12", it) }
        jsonObject.optString("de13").takeIf { it.isNotEmpty() }?.let { orderedJson.put("de13", it) }

        return orderedJson
    }

    /**
     * Parsea respuesta de Venta MC y retorna el status y el JSONObject
     */
    private fun parseVentaMCResponseDetailed(data: Intent?): Pair<String, JSONObject> {
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

        val status = determineVentaMCStatus(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Determina el estado de la Venta MC
     */
    private fun determineVentaMCStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            responseCode == "0" || responseCode == "00" -> "VENTA MC APROBADA"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "VENTA MC RECHAZADA - $responseMessage"
            responseCode == "-1" -> "VENTA MC RECHAZADA"
            else -> "VENTA MC RECHAZADA (Código: $responseCode)"
        }
    }
    /**
     * Procesa y muestra la respuesta de cierre en un diálogo
     * Uso: JsonParser.showCierreResult(activity, data)
     */
    fun showCierreResult(activity: android.app.Activity, data: Intent?, requestData: String = "") {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted, jsonObject) = parseCierreResponseDetailed(data)

            // Crear HistoryItem
            val historyItem = HistoryItem(
                commandType = "CIERRE",
                mode = "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = null,
                ticketNumber = null,
                authorizationCode = null,
                functionCode = jsonObject.optInt("FunctionCode")

            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Parsea cualquier respuesta de cierre
     */

    private fun parseCierreResponseDetailed(data: Intent?): Triple<String, String, JSONObject> {
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

        val status = determineCierreStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Triple(status, formattedJson, jsonObject)
    }

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
            .setTitle("ERROR")
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
    fun showAnulacionResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted, jsonObject) = parseAnulacionResponseDetailed(data)

            // Crear HistoryItem
            val historyItem = HistoryItem(
                commandType = "ANULACIÓN",
                mode = "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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

    private fun parseAnulacionResponseDetailed(data: Intent?): Triple<String, String, JSONObject> {
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

        val status = determineAnulacionStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Triple(status, formattedJson, jsonObject)
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
    fun showDevolucionResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted, jsonObject) = parseDevolucionResponseDetailed(data)

            // Crear HistoryItem
            val historyItem = HistoryItem(
                commandType = "DEVOLUCIÓN",
                mode = "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Procesa y muestra la respuesta de anulación MC en un diálogo con formato ordenado
     */
    fun showAnulacionMCResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseAnulacionMCResponseDetailed(data)

            // Crear JSON ordenado específico para Anulación MC
            val orderedJson = createAnulacionMCJson(jsonObject)

            val jsonFormatted = orderedJson.toString(2)

            // Guardar en historial
            val historyItem = HistoryItem(
                commandType = "ANULACIÓN MC",
                mode = "JSON",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Crea JSON ordenado específico para Anulación MC con todos los campos requeridos
     */
    private fun createAnulacionMCJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos principales de respuesta
        orderedJson.put("FunctionCode", jsonObject.optInt("FunctionCode", 122))
        orderedJson.put("ResponseCode", jsonObject.optString("ResponseCode", "0"))
        orderedJson.put("ResponseMessage", jsonObject.optString("ResponseMessage", "Aprobado"))
        orderedJson.put("CommerceCode", jsonObject.optLong("CommerceCode", 0))
        orderedJson.put("TerminalId", jsonObject.optString("TerminalId", ""))
        orderedJson.put("AuthorizationCode", jsonObject.optString("AuthorizationCode", ""))
        orderedJson.put("OperationID", jsonObject.optInt("OperationID", 0))
        orderedJson.put("Success", jsonObject.optBoolean("Success", false))

        // Token y datos de tarjeta
        orderedJson.putOpt("TransToken", jsonObject.optString("TransToken"))
        orderedJson.putOpt("ExpiryDate", jsonObject.optString("ExpiryDate"))
        orderedJson.putOpt("IssuerId", jsonObject.optString("IssuerId"))
        orderedJson.putOpt("Last4Digits", jsonObject.optString("Last4Digits"))

        // Datos del comercio
        orderedJson.putOpt("CommerceRut", jsonObject.optString("CommerceRut"))
        orderedJson.putOpt("CommerceName", jsonObject.optString("CommerceName"))
        orderedJson.putOpt("BranchName", jsonObject.optString("BranchName"))
        orderedJson.putOpt("BranchAddress", jsonObject.optString("BranchAddress"))
        orderedJson.putOpt("BranchDistrict", jsonObject.optString("BranchDistrict"))

        return orderedJson
    }

    /**
     * Parsea respuesta de Anulación MC y retorna el status y el JSONObject
     */
    private fun parseAnulacionMCResponseDetailed(data: Intent?): Pair<String, JSONObject> {
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

        val status = determineAnulacionMCStatus(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Determina el estado de la Anulación MC
     */
    private fun determineAnulacionMCStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val success = jsonObject.optBoolean("Success", false)
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            success || responseCode == "0" || responseCode == "00" -> "ANULACIÓN MC APROBADA"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "ANULACIÓN MC RECHAZADA - $responseMessage"
            responseCode == "-1" -> "ANULACIÓN MC RECHAZADA"
            else -> "ANULACIÓN MC RECHAZADA (Código: $responseCode)"
        }
    }

    /**
     * Parsea respuesta de devolución
     */

    private fun parseDevolucionResponseDetailed(data: Intent?): Triple<String, String, JSONObject> {
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

        val status = determineDevolucionStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Triple(status, formattedJson, jsonObject)
    }

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
     * Procesa y muestra la respuesta de devolución MC en un diálogo con formato ordenado
     */
    fun showDevolucionMCResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseDevolucionMCResponseDetailed(data)

            // Crear JSON ordenado específico para Devolución MC
            val orderedJson = createDevolucionMCJson(jsonObject)

            val jsonFormatted = orderedJson.toString(2)

            // Guardar en historial
            val historyItem = HistoryItem(
                commandType = "DEVOLUCIÓN MC",
                mode = "JSON",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Crea JSON ordenado específico para Devolución MC
     */
    private fun createDevolucionMCJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos principales de respuesta
        orderedJson.put("FunctionCode", jsonObject.optInt("FunctionCode", 123))
        orderedJson.put("ResponseCode", jsonObject.optString("ResponseCode", "0"))
        orderedJson.put("ResponseMessage", jsonObject.optString("ResponseMessage", "Aprobado"))
        orderedJson.put("CommerceCode", jsonObject.optLong("CommerceCode", 0))
        orderedJson.put("TerminalId", jsonObject.optString("TerminalId", ""))
        orderedJson.put("AuthorizationCode", jsonObject.optString("AuthorizationCode", ""))
        orderedJson.put("OperationID", jsonObject.optInt("OperationID", 0))
        orderedJson.put("Success", jsonObject.optBoolean("Success", false))
        orderedJson.put("DateTime", jsonObject.optString("DateTime", ""))

        // Token y datos de tarjeta
        orderedJson.putOpt("TransToken", jsonObject.optString("TransToken"))
        orderedJson.putOpt("ExpiryDate", jsonObject.optString("ExpiryDate"))
        orderedJson.putOpt("IssuerId", jsonObject.optString("IssuerId"))
        orderedJson.putOpt("Last4Digits", jsonObject.optString("Last4Digits"))

        return orderedJson
    }

    /**
     * Parsea respuesta de Devolución MC
     */
    private fun parseDevolucionMCResponseDetailed(data: Intent?): Pair<String, JSONObject> {
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

        val status = determineDevolucionMCStatus(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Determina el estado de la Devolución MC
     */
    private fun determineDevolucionMCStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val success = jsonObject.optBoolean("Success", false)
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            success || responseCode == "0" || responseCode == "00" -> "DEVOLUCIÓN MC APROBADA"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "DEVOLUCIÓN MC RECHAZADA - $responseMessage"
            responseCode == "-1" -> "DEVOLUCIÓN MC RECHAZADA"
            else -> "DEVOLUCIÓN MC RECHAZADA (Código: $responseCode)"
        }
    }

    /**
     * Procesa y muestra la respuesta de duplicado en un diálogo
     */
    fun showDuplicadoResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseDuplicadoResponseDetailed(data)

            val orderedJson = JSONObject()

            // Campos - solo incluir si existen en el JSON original
            jsonObject.optInt("FunctionCode").takeIf { it != 0 }?.let { orderedJson.put("FunctionCode", it) }
            jsonObject.optString("ResponseCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseCode", it) }
            jsonObject.optString("ResponseMessage").takeIf { it.isNotEmpty() }?.let { orderedJson.put("ResponseMessage", it) }
            jsonObject.optLong("CommerceCode").takeIf { it != 0L }?.let { orderedJson.put("CommerceCode", it) }
            jsonObject.optString("TerminalId").takeIf { it.isNotEmpty() }?.let { orderedJson.put("TerminalId", it) }
            jsonObject.optString("Ticket").takeIf { it.isNotEmpty() }?.let { orderedJson.put("Ticket", it) }
            jsonObject.optString("AuthorizationCode").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AuthorizationCode", it) }
            jsonObject.optInt("Amount").takeIf { it != 0 }?.let { orderedJson.put("Amount", it) }
            jsonObject.optInt("SharesNumber").takeIf { it != 0 }?.let { orderedJson.put("SharesNumber", it) }
            jsonObject.optInt("SharesAmount").takeIf { it != 0 }?.let { orderedJson.put("SharesAmount", it) }
            jsonObject.optInt("Last4Digits").takeIf { it != 0 }?.let { orderedJson.put("Last4Digits", it) }
            jsonObject.optInt("OperationId").takeIf { it != 0 }?.let { orderedJson.put("OperationId", it) }
            jsonObject.optString("CardType").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardType", it) }
            jsonObject.optString("AccountingDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountingDate", it) }
            jsonObject.optString("AccountNumber").takeIf { it.isNotEmpty() }?.let { orderedJson.put("AccountNumber", it) }
            jsonObject.optString("CardBrand").takeIf { it.isNotEmpty() }?.let { orderedJson.put("CardBrand", it) }
            jsonObject.optString("RealDate").takeIf { it.isNotEmpty() }?.let { orderedJson.put("RealDate", it) }
            jsonObject.optInt("EmployeeId").takeIf { it != 0 }?.let { orderedJson.put("EmployeeId", it) }
            jsonObject.optInt("Tip").takeIf { it != 0 }?.let { orderedJson.put("Tip", it) }
            jsonObject.optInt("SaleType").takeIf { it != 0 }?.let { orderedJson.put("SaleType", it) }
            jsonObject.optInt("PosMode").takeIf { it != 0 }?.let { orderedJson.put("PosMode", it) }
            jsonObject.optInt("Cashback").takeIf { it != 0 }?.let { orderedJson.put("Cashback", it) }

            val jsonFormatted = if (orderedJson.length() > 0) orderedJson.toString(2) else "{}"

            // Crear HistoryItem
            val historyItem = HistoryItem(
                commandType = "DUPLICADO",
                mode = "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode").takeIf { it.isNotEmpty() } ?: "-1",
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket").takeIf { it.isNotEmpty() },
                authorizationCode = jsonObject.optString("AuthorizationCode").takeIf { it.isNotEmpty() },
                functionCode = jsonObject.optInt("FunctionCode").takeIf { it != 0 }
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
    fun showDetalleVentaResult(activity: android.app.Activity, data: Intent?, requestData: String = "") {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted, jsonObject) = parseDetalleVentaResponseDetailed(data)

            // Crear y guardar en historial
            val historyItem = HistoryItem(
                commandType = "DETALLE VENTA",
                mode = "LIBRERÍA",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = jsonObject.optInt("Amount"),
                ticketNumber = jsonObject.optString("Ticket"),
                authorizationCode = jsonObject.optString("AuthorizationCode"),
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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

    private fun parseDetalleVentaResponseDetailed(data: Intent?): Triple<String, String, JSONObject> {
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

        val status = determineDetalleVentaStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Triple(status, formattedJson, jsonObject)
    }

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
                    responseMessage.contains(
                        "Aprobado",
                        ignoreCase = true
                    ) -> "DETALLE DE VENTA EXITOSO"

            jsonObject.has("error") -> "ERROR EN DETALLE DE VENTA"
            else -> "DETALLE DE VENTA RECHAZADO"
        }
    }

    /**
     * Procesa y muestra la respuesta de detalle de ventas MC
     */
    fun showDetalleMCResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonObject) = parseDetalleMCResponseDetailed(data)

            // Crear JSON ordenado para Detalle MC
            val orderedJson = createDetalleMCJson(jsonObject)

            val jsonFormatted = orderedJson.toString(2)

            // Guardar en historial
            val historyItem = HistoryItem(
                commandType = "DETALLE MC",
                mode = "JSON",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = null,
                ticketNumber = null,
                authorizationCode = null,
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

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
     * Crea JSON ordenado para Detalle MC con formato legible
     */
    private fun createDetalleMCJson(jsonObject: JSONObject): JSONObject {
        val orderedJson = JSONObject()

        // Campos principales
        orderedJson.put("FunctionCode", jsonObject.optInt("FunctionCode", 133))
        orderedJson.put("ResponseCode", jsonObject.optString("ResponseCode", "0"))
        orderedJson.put("ResponseMessage", jsonObject.optString("ResponseMessage", "Aprobado"))

        // Procesar SaleDetails si existe
        if (jsonObject.has("SaleDetails")) {
            val saleDetailsArray = jsonObject.getJSONArray("SaleDetails")
            val formattedArray = JSONArray()

            for (i in 0 until saleDetailsArray.length()) {
                val saleItem = saleDetailsArray.getJSONObject(i)
                val formattedItem = JSONObject()

                // Ordenar campos de cada venta
                formattedItem.put("OperationId", saleItem.optInt("Operationld", 0))
                formattedItem.put("Amount", saleItem.optInt("Amount", 0))
                formattedItem.put("AuthorizationCode", saleItem.optString("AuthorizationCode", ""))
                formattedItem.put("CardBrand", saleItem.optString("CardBrand", ""))
                formattedItem.put("CardType", saleItem.optString("CardType", ""))
                formattedItem.put("Last4Digits", saleItem.optString("Last4Digits", ""))
                formattedItem.put("AccountingDate", saleItem.optString("AccountingDate", ""))
                formattedItem.put("RealDate", saleItem.optString("RealDate", ""))
                formattedItem.put("CommerceName", saleItem.optString("CommerceName", ""))
                formattedItem.put("CommerceRut", saleItem.optString("CommerceRut", ""))
                formattedItem.put("BranchName", saleItem.optString("BranchName", ""))
                formattedItem.put("TerminalId", saleItem.optString("Terminalld", ""))
                formattedItem.put("TransToken", saleItem.optString("TransToken", ""))
                formattedItem.put("EntryMode", saleItem.optString("EntryMode", ""))
                formattedItem.put("ExpiryDate", saleItem.optString("ExpiryDate", ""))
                formattedItem.put("Aid", saleItem.optString("Aid", ""))
                formattedItem.put("Bin", saleItem.optString("Bin", ""))

                formattedArray.put(formattedItem)
            }

            orderedJson.put("SaleDetails", formattedArray)
            orderedJson.put("TotalVentas", formattedArray.length())
        }

        return orderedJson
    }

    /**
     * Parsea respuesta de Detalle MC
     */
    private fun parseDetalleMCResponseDetailed(data: Intent?): Pair<String, JSONObject> {
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

        val status = determineDetalleMCStatus(jsonObject)
        return Pair(status, jsonObject)
    }

    /**
     * Determina el estado del Detalle MC
     */
    private fun determineDetalleMCStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            responseCode == "0" || responseCode == "00" -> "DETALLE MC EXITOSO"
            jsonObject.has("SaleDetails") && jsonObject.getJSONArray("SaleDetails").length() > 0 -> "DETALLE MC EXITOSO"
            responseCode == "-1" && responseMessage.isNotEmpty() -> "DETALLE MC RECHAZADO - $responseMessage"
            else -> "DETALLE MC RECHAZADO"
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

    fun showMainPosDataResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = "",
        shouldFinishActivity: Boolean = true
    ) {
        currentDialog?.dismiss()
        try {
            val (status, jsonFormatted, jsonObject) = parseMainPosDataResponseDetailed(data)

            val historyItem = HistoryItem(
                commandType = "MAIN POS DATA",
                mode = "JSON",
                requestData = requestData,
                responseData = jsonFormatted,
                result = status,
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = null,
                ticketNumber = null,
                authorizationCode = null,
                functionCode = jsonObject.optInt("FunctionCode")
            )
            HistoryManager.saveTransaction(activity, historyItem)

            currentDialog = android.app.AlertDialog.Builder(activity)
                .setTitle(status)
                .setMessage(jsonFormatted)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    currentDialog = null
                    if (shouldFinishActivity) {
                        activity.finish()
                    }
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

    private fun parseMainPosDataResponseDetailed(data: Intent?): Triple<String, String, JSONObject> {
        val extras = data?.extras ?: Bundle()
        var jsonObject = JSONObject()

        if (extras.containsKey("response")) {
            // Usar getSerializable con la nueva API
            val response = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                extras.getSerializable("response", java.io.Serializable::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras.getSerializable("response")
            }

            if (response != null) {
                val originalJson = JSONObject(response.toString())

                // Crear JSON ordenado
                val orderedJson = JSONObject()

                // 1. PRIMERO los campos principales
                orderedJson.put("FunctionCode", originalJson.optInt("FunctionCode"))
                orderedJson.put("ResponseCode", originalJson.optString("ResponseCode"))
                orderedJson.put("ResponseMessage", originalJson.optString("ResponseMessage"))

                // 2. DESPUÉS CommerceData como ARRAY
                if (originalJson.has("CommerceData")) {
                    val commerceArray = JSONArray()

                    // Obtener los datos de CommerceData (ya sea objeto o array)
                    val commerceDataObj = if (originalJson.get("CommerceData") is JSONObject) {
                        originalJson.getJSONObject("CommerceData")
                    } else {
                        val array = originalJson.getJSONArray("CommerceData")
                        if (array.length() > 0) array.getJSONObject(0) else JSONObject()
                    }

                    // Crear el objeto ordenado según tu ejemplo
                    val orderedCommerceItem = JSONObject()
                    orderedCommerceItem.put("LegalName", commerceDataObj.optString("LegalName"))
                    orderedCommerceItem.put("CommerceNumber", commerceDataObj.optString("CommerceNumber"))
                    orderedCommerceItem.put("CommerceRut", commerceDataObj.optString("CommerceRut"))
                    orderedCommerceItem.put("BranchNumber", commerceDataObj.optString("BranchNumber"))
                    orderedCommerceItem.put("BranchName", commerceDataObj.optString("BranchName"))
                    orderedCommerceItem.put("LittleBranchName", commerceDataObj.optString("LittleBranchName"))
                    orderedCommerceItem.put("BranchAddress", commerceDataObj.optString("BranchAddress"))
                    orderedCommerceItem.put("BranchDistrict", commerceDataObj.optString("BranchDistrict"))
                    orderedCommerceItem.put("TerminalId", commerceDataObj.optString("TerminalId"))
                    orderedCommerceItem.put("SerialNumber", commerceDataObj.optString("SerialNumber"))

                    commerceArray.put(orderedCommerceItem)
                    orderedJson.put("CommerceData", commerceArray)
                }

                jsonObject = orderedJson
            }
        } else if (extras.containsKey("params")) {
            val jsonString = extras.getString("params", "")
            if (jsonString.isNotEmpty()) {
                jsonObject = JSONObject(jsonString)
            }
        }

        val status = determineMainPosDataStatus(jsonObject)
        val formattedJson = jsonObject.toString(2)
        return Triple(status, formattedJson, jsonObject)
    }

    private fun determineMainPosDataStatus(jsonObject: JSONObject): String {
        val responseCode = jsonObject.optString("ResponseCode", "-1")
        val responseMessage = jsonObject.optString("ResponseMessage", "")

        return when {
            responseCode == "0" || responseCode == "00" -> "CONSULTA EXITOSA"
            responseMessage.contains("Aprobado", ignoreCase = true) -> "CONSULTA EXITOSA"
            else -> "CONSULTA FALLIDA"
        }
    }
    /**
     * Procesa y muestra la respuesta de impresión en un diálogo
     */
    fun showPrintServiceResult(
        activity: android.app.Activity,
        data: Intent?,
        requestData: String = ""
    ) {
        currentDialog?.dismiss()
        try {
            val resultData = data?.getStringExtra("resultData") ?: "{}"
            val formattedJson = formatJson(resultData)
            val jsonObject = JSONObject(resultData)

            // Crear HistoryItem
            val historyItem = HistoryItem(
                commandType = "IMPRESIÓN",
                mode = "SERVICIO",
                requestData = requestData,
                responseData = formattedJson,
                result = determinePrintServiceStatus(jsonObject),
                responseCode = jsonObject.optString("ResponseCode", "-1"),
                amount = null,
                ticketNumber = null,
                authorizationCode = null,
                functionCode = jsonObject.optInt("FunctionCode")

            )
            HistoryManager.saveTransaction(activity, historyItem)

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