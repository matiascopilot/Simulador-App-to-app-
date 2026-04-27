package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.activitys.HistorialActivitys.HistoryActivity
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import cl.ione.simuladorapptoapp.databinding.ActivityConfigBinding
import cl.ione.simuladorapptoapp.managers.MainPosDataManager

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding
    private var isCommandsMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        setupHeader()
        setupListeners()
    }

    private fun setupHeader() {
        binding.header.setup(
            title = "Configuración",
            showBackButton = true,
            showRequestButton = false,
            onBackClick = { finish() }
        )
    }

    private fun setupListeners() {
        binding.cardMainposdata.setOnClickListener {
            MainPosDataManager.consultarMainPosData(this)
        }

        binding.cardHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }

        binding.cardScannerService.setOnClickListener {
            val intent = Intent(this, ScannerServiceActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }

        binding.cardC2CMode.setOnClickListener {
            val intent = Intent(this, C2CModeActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }
        binding.cardLogcat.setOnClickListener {
            val intent = Intent(this, LogcatActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainPosDataManager.getRequestCode()) {
            val requestData = RequestManager.getCurrentRequest()
            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showMainPosDataResult(
                        activity = this,
                        data = data,
                        requestData = requestData,
                        shouldFinishActivity = false
                    )
                }
                RESULT_CANCELED -> {
                    mostrarResultado("CONSULTA CANCELADA")
                }
                else -> {
                    procesarRespuestaError(data)
                }
            }
        }
    }

    private fun mostrarResultado(mensaje: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Información")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR", null)
            .show()
    }

    private fun procesarRespuestaError(data: Intent?) {
        val errorMsg = data?.getStringExtra("error") ?:
        data?.getStringExtra("message") ?:
        "Error desconocido"
        mostrarResultado("Error: $errorMsg")
    }
}