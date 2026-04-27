package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.components.RequestManager
import cl.ione.simuladorapptoapp.databinding.ActivityC2cModeBinding
import org.json.JSONObject

class C2CModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityC2cModeBinding
    private val TAG = "C2CModeActivity"
    private val C2C_MODE_ACTION = "cl.getnet.payment.action.C2C_MODE"

    private var selectedMode: Int = 0 // 0: Atendido, 1: Desatendido
    private val typeApp: Int = 0 // Siempre 0 para aplicación Android

    // Variable para saber si se aplicó el cambio
    private var cambioAplicado = false
    private var modoAplicado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityC2cModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupSwitch()
        setupFooterButtons()
        setupRequestManager()

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            cambioAplicado = savedInstanceState.getBoolean("cambioAplicado", false)
            modoAplicado = savedInstanceState.getInt("modoAplicado", 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("cambioAplicado", cambioAplicado)
        outState.putInt("modoAplicado", modoAplicado)
    }

    override fun onResume() {
        super.onResume()
        // Al volver a la actividad, mostrar Toast si se aplicó un cambio
        if (cambioAplicado) {
            val modoTexto = if (modoAplicado == 1) "DESATENDIDO" else "ATENDIDO"
            Toast.makeText(
                this,
                "Modo cambiado a $modoTexto",
                Toast.LENGTH_LONG
            ).show()
            // Resetear la bandera
            cambioAplicado = false
        }
    }

    private fun setupHeader() {
        binding.header.setup(
            title = "Modo Desatendido",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    private fun setupRequestManager() {
        RequestManager.bind(binding.header)
        val buildRequestJson = {
            JSONObject().apply {
                put("mode", selectedMode)
                put("typeApp", typeApp)
            }
        }
        RequestManager.initWithDefault(buildRequestJson, "C2C MODE")
    }

    private fun setupSwitch() {
        // Configurar switch - por defecto: Atendido (false)
        binding.switchUnattendedMode.isChecked = false

        binding.switchUnattendedMode.setOnCheckedChangeListener { _, isChecked ->
            selectedMode = if (isChecked) 1 else 0
            setupRequestManager() // Actualizar RequestManager
        }
    }

    private fun setupFooterButtons() {
        binding.footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "APLICAR",
            onPrimaryClick = {
                // Si vuelve sin aplicar, no mostrar mensaje
                finish()
            },
            onSecondaryClick = { enviarModoDesatendido() }
        )
    }

    private fun enviarModoDesatendido() {
        try {
            // Guardar el modo que se está aplicando
            modoAplicado = selectedMode

            // Crear el JSON de solicitud
            val requestJson = JSONObject().apply {
                put("mode", selectedMode)
                put("typeApp", typeApp)
            }.toString()

            Log.d(TAG, "Enviando: $requestJson")
            RequestManager.updateRequest(requestJson, "C2C MODE ENVIADO")

            val intent = Intent(C2C_MODE_ACTION)
            intent.putExtra("params", requestJson)

            // Enviar broadcast
            sendBroadcast(intent)

            val modoTexto = if (selectedMode == 1) "DESATENDIDO" else "ATENDIDO"

            // Marcar que se aplicó un cambio
            cambioAplicado = true

            // Mostrar Toast inmediato (opcional)
            Toast.makeText(
                this,
                "Solicitud enviada: Cambiar a modo $modoTexto",
                Toast.LENGTH_SHORT
            ).show()

            // Finalizar la actividad después de enviar
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            cambioAplicado = false
        }
    }
}