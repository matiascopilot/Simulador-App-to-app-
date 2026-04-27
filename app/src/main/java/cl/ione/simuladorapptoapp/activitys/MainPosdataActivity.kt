package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityMainPosdataBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import org.json.JSONObject

class MainPosdataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPosdataBinding
    private val REQUEST_CODE_MAIN_POS_DATA = 3450
    private val TAG = "MainPosdataActivity"
    private var typeApp: Byte = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPosdataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarHeader()
        setupRequestManager()
        setupFooterButtons()
    }

    private fun configurarHeader() {
        binding.header.setup(
            title = "Main Pos Data",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    private fun setupRequestManager() {
        RequestManager.bind(binding.header)
        val buildRequestJson = {
            JSONObject().apply {
                put("TypeApp", typeApp)
            }
        }
        RequestManager.initWithDefault(buildRequestJson, "MAIN POS DATA")
    }

    private fun setupFooterButtons() {
        binding.footerButtons.setButtons(
            primaryText = "CANCELAR",
            secondaryText = "CONSULTAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { consultarMainPosData() }
        )
    }

    private fun consultarMainPosData() {
        try {
            val intent = Intent("cl.getnet.payment.action.MAIN_POS_DATA")

            val requestJson = """
            {
                "TypeApp": $typeApp
            }
            """.trimIndent()

            intent.putExtra("params", requestJson)
            Log.d(TAG, "Request: $requestJson")
                startActivityForResult(intent, REQUEST_CODE_MAIN_POS_DATA)

        }catch (e: Exception) {
                Log.e(TAG, "❌ Error: ${e.message}", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_MAIN_POS_DATA) {
            val requestData = RequestManager.getCurrentRequest()

            when (resultCode) {
                RESULT_OK -> {
                    JsonParser.showMainPosDataResult(this, data, requestData = requestData)
                }

                RESULT_CANCELED -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "CONSULTA CANCELADA",
                        onRetry = { consultarMainPosData() }
                    )
                }

                else -> {
                    JsonParser.showErrorWithRetry(
                        activity = this,
                        data = data,
                        title = "CONSULTA RECHAZADA",
                        onRetry = { consultarMainPosData() }
                    )
                }
            }
        }
    }
}