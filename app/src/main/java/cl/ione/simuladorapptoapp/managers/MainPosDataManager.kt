package cl.ione.simuladorapptoapp.managers

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.components.RequestManager
import org.json.JSONObject

object MainPosDataManager {
    private const val TAG = "MainPosDataManager"
    private const val ACTION_MAIN_POS_DATA = "cl.getnet.payment.action.MAIN_POS_DATA"
    private const val REQUEST_CODE = 3450

    fun consultarMainPosData(activity: AppCompatActivity, typeApp: Byte = 0) {
        try {
            val intent = Intent(ACTION_MAIN_POS_DATA)

            val requestJson = JSONObject().apply {
                put("TypeApp", typeApp.toInt())
            }.toString()

            intent.putExtra("params", requestJson)

            RequestManager.updateRequest(requestJson, "MAIN POS DATA")

            Log.d(TAG, "Enviando consulta MainPosData: $requestJson")
            activity.startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            mostrarError(activity, "Error: ${e.message}")
        }
    }

    private fun mostrarError(context: android.content.Context, mensaje: String) {
        android.app.AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR", null)
            .setCancelable(false)
            .show()
    }

    fun getRequestCode(): Int = REQUEST_CODE
}