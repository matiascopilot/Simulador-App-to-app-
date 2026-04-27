package cl.ione.simuladorapptoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import cl.ione.simuladorapptoapp.activitys.ScannerServiceActivity

class ScannerServiceReceiver : BroadcastReceiver() {

    companion object {
        const val SCANNER_RESPONSE_ACTION = "cl.getnet.c2cservice.action.RESPONSE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScannerServiceReceiver", "Respuesta recibida: ${intent.action}")

        if (intent.action == SCANNER_RESPONSE_ACTION) {
            // Aquí puedes procesar la respuesta
            val data = intent.getStringExtra("data")
            val error = intent.getStringExtra("error")

            Log.d("ScannerServiceReceiver", "Data: $data")
            Log.d("ScannerServiceReceiver", "Error: $error")

            // Notificar a la actividad si está visible
            // Puedes usar EventBus, LiveData o un Intent con flag
            val notificationIntent = Intent("SCANNER_RESPONSE_RECEIVED")
            notificationIntent.putExtra("data", data)
            notificationIntent.putExtra("error", error)
            context.sendBroadcast(notificationIntent)
        }
    }
}