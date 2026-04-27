package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager
import cl.ione.simuladorapptoapp.databinding.ActivityScannerServiceBinding
import org.json.JSONObject

class ScannerServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerServiceBinding
    private val TAG = "ScannerServiceActivity"
    private var selectedTypeApp: Int = 0
    private val SCANNER_SERVICE_ACTION = "cl.getnet.c2cservice.action.SCANNER_SERVICE"
    private val RESPONSE_ACTION = "cl.getnet.c2cservice.action.RESPONSE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupRequestManager()
        setupSpinner()
        setupFooterButtons()
    }

    private fun setupHeader() {
        binding.header.setup(
            title = "Scanner Service",
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() }
        )
    }

    private fun setupRequestManager() {
        RequestManager.bind(binding.header)
        val buildRequestJson = {
            JSONObject().apply {
                put("typeApp", selectedTypeApp)
            }
        }
        RequestManager.initWithDefault(buildRequestJson, "SCANNER SERVICE")
    }

    private fun setupSpinner() {
        // Opciones para el spinner
        val typeAppOptions = arrayOf("App Normal (0)", "App Restringida (1)")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeAppOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerTypeApp.adapter = adapter

        binding.spinnerTypeApp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTypeApp = position // 0 o 1
                // Actualizar el RequestManager cuando cambie el tipo
                setupRequestManager()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTypeApp = 0
            }
        }
    }

    private fun setupFooterButtons() {
        binding.footerButtons.setButtons(
            primaryText = "CANCELAR",
            secondaryText = "ESCANEAR",
            onPrimaryClick = { finish() },
            onSecondaryClick = { iniciarScannerService() }
        )
    }

    private fun iniciarScannerService() {
        try {
            val intent = Intent(SCANNER_SERVICE_ACTION)

            // Crear el JSON de solicitud
            val requestJson = JSONObject().apply {
                put("typeApp", selectedTypeApp)
            }.toString()

            intent.putExtra("params", requestJson)
            intent.putExtra("urlToResponse", RESPONSE_ACTION)

            Log.d(TAG, "Request: $requestJson")

                startService(intent)

                Toast.makeText(
                    this,
                    "Solicitud enviada al Scanner Service. Esperando resultado...",
                    Toast.LENGTH_LONG
                ).show()

                // Mostrar mensaje de que debe recibir respuesta por broadcast
                mostrarInstrucciones()
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarInstrucciones() {
        val mensaje = """
            La solicitud ha sido enviada al Scanner Service.
            
            IMPORTANTE: Como es un servicio Android (no una Activity), la respuesta llegará a través de un Broadcast Receiver con la acción:
            
            $RESPONSE_ACTION
            
            La app debe tener registrado un BroadcastReceiver para capturar la respuesta.
            
            Parámetros enviados:
            - typeApp: $selectedTypeApp
        """.trimIndent()

        Toast.makeText(this, "Ver instrucciones en LogCat", Toast.LENGTH_LONG).show()
        Log.d(TAG, mensaje)
    }

    /**
     * Método para procesar la respuesta del Scanner Service
     * Este método debería ser llamado desde un BroadcastReceiver
     */
    fun procesarRespuestaScanner(intent: Intent) {
        try {
            val resultData = intent.extras?.getString("data")
            val errorData = intent.extras?.getString("error")
            val requestData = RequestManager.getCurrentRequest()

            runOnUiThread {
                binding.txtResponseTitle.visibility = View.VISIBLE
                binding.cardResponse.visibility = View.VISIBLE

                val responseText = if (errorData != null) {
                    "ERROR:\n$errorData"
                } else {
                    formatResponse(resultData)
                }

                binding.txtResponse.text = responseText

                // También podrías usar JsonParser aquí si existe un método para scanner
                // JsonParser.showScannerResult(this, intent, requestData)
            }

            Log.d(TAG, "Respuesta recibida - Data: $resultData, Error: $errorData")

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando respuesta: ${e.message}", e)
        }
    }

    private fun formatResponse(response: String?): String {
        return try {
            if (response.isNullOrEmpty()) {
                "Respuesta vacía"
            } else {
                // Intentar formatear como JSON si es posible
                val jsonObj = JSONObject(response)
                "RESPUESTA:\n${jsonObj.toString(2)}"
            }
        } catch (e: Exception) {
            "RESPUESTA:\n$response"
        }
    }

    /**
     * Función para simular una respuesta (solo para pruebas sin POS)
     */
    fun simularRespuestaPrueba() {
        val mockResponse = """
            {
                "result": "success",
                "scannedData": "1234567890123",
                "format": "EAN_13",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        procesarRespuestaScanner(Intent().apply {
            putExtra("data", mockResponse)
        })
    }
}