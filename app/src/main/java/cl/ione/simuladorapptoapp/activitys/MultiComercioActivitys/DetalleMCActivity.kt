package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.*

class DetalleMCActivity : AppCompatActivity() {

    private val REQUEST_CODE = 3445
    private var isCommandsMode: Boolean = false
    private lateinit var header: Header
    private lateinit var footerButtons: FooterButtons
    private lateinit var btnIniciar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mc)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        initViews()
        setupHeader()
        setupFooterButtons()
        setupRequestManager()
    }

    private fun initViews() {
        header = findViewById(R.id.header)
        footerButtons = findViewById(R.id.footerButtons)
        btnIniciar = findViewById(R.id.btnIniciarDetalle)
    }

    private fun setupHeader() {
        header.setup(
            title = "Detalle Ventas MC",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    private fun setupRequestManager() {
        RequestManager.bind(header)

        // No hay parámetros, solo un JSON vacío o con TypeApp
        val buildRequestJson = {
            org.json.JSONObject().apply {
                put("TypeApp", 0) // Opcional, pero se puede enviar
            }
        }

        RequestManager.initWithDefault(buildRequestJson, "REQUEST DETALLE MC")
    }

    private fun setupFooterButtons() {
        footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "CONSULTAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { consultarDetalleMC() }
        )
        btnIniciar.setOnClickListener {
            consultarDetalleMC()
        }
    }

    // ✅ Eliminé el método verificarAppGetnet()

    private fun consultarDetalleMC() {
        // ✅ Eliminé la verificación de PackageManager

        try {
            // La operación no requiere parámetros, pero podemos enviar TypeApp opcional
            val jsonRequest = org.json.JSONObject().apply {
                put("TypeApp", 0)
            }.toString()

            RequestManager.updateRequest(jsonRequest, "REQUEST DETALLE MC ENVIADO")

            Log.d("DETALLE_MC_DEBUG", "════════════════════════════════")
            Log.d("DETALLE_MC_DEBUG", "📤 ENVIANDO DETALLE MC")
            Log.d("DETALLE_MC_DEBUG", "JSON Enviado: $jsonRequest")
            Log.d("DETALLE_MC_DEBUG", "════════════════════════════════")

            val intent = Intent("cl.getnet.payment.action.SALES_DETAIL_MC")
            intent.putExtra("params", jsonRequest)
            startActivityForResult(intent, REQUEST_CODE)

        } catch (e: Exception) {
            Log.e("DETALLE_MC_DEBUG", "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            val requestData = RequestManager.getCurrentRequest()
            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showDetalleMCResult(
                        activity = this,
                        data = data,
                        requestData = requestData
                    )
                }

                RESULT_CANCELED -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "CONSULTA CANCELADA",
                        onRetry = { consultarDetalleMC() }
                    )
                }

                else -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "DETALLE MC RECHAZADO",
                        onRetry = { consultarDetalleMC() }
                    )
                }
            }
        }
    }
}