package cl.ione.simuladorapptoapp.activitys.MultiComercioActivitys

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.activitys.AnulacionMCActivity
import cl.ione.simuladorapptoapp.activitys.DetalleMCActivity
import cl.ione.simuladorapptoapp.activitys.DevolucionMCActivity
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import cl.ione.simuladorapptoapp.databinding.ActivityMulticommerceBinding
import cl.ione.simuladorapptoapp.managers.MainPosDataManager

class MulticommerceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMulticommerceBinding
    private var isCommandsMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMulticommerceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        setupHeader()
        setupListeners()
    }

    private fun setupHeader() {
        binding.header.setup(
            title = "Comandos Multi-Comercio",
            showBackButton = true,
            showRequestButton = false,
            onBackClick = { finish() }
        )
    }

    private fun setupListeners() {
        // Venta Multicomercio
        binding.cardVentaMC.setOnClickListener {
            val intent = Intent(this, VentaMCActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }

        // Consultar MainPosData
        binding.cardMainPosData.setOnClickListener {
            MainPosDataManager.consultarMainPosData(this)
        }

        // En MulticommerceActivity, actualiza el listener de cardVoidMC
        binding.cardVoidMC.setOnClickListener {
            val intent = Intent(this, AnulacionMCActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }

        binding.cardRefundMC.setOnClickListener {
            val intent = Intent(this, DevolucionMCActivity::class.java)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        }

        binding.cardDetailMC.setOnClickListener {
            val intent = Intent(this, DetalleMCActivity::class.java)
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
                        shouldFinishActivity = false // NO finalizar, volver a MulticommerceActivity
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