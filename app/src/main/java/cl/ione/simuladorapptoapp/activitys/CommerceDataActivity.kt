package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.activitys.MultiComercioActivitys.VentaMCActivity
import cl.ione.simuladorapptoapp.databinding.ActivityCommerceDataBinding
import org.json.JSONObject

class CommerceDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommerceDataBinding
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = "" // Para guardar el request actual
    private val REQUEST_CODE_GET_PARAMS = 3452 // Código para GET PARAMS MC

    // Datos temporales para el RUT y serial number
    private var tempRut: String = ""
    private var tempSerialNumber: String = ""

    private val startVentaMC = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommerceDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

        setupHeader()
        setupFooterButtons()
        setupTraerDatosButton() // Nuevo método
        setDefaultValues()
        configurarActualizacionRequest()
        actualizarRequestJson()
    }

    private fun setupHeader() {
        val titulo = if (isCommandsMode) {
            "Datos Comercio Hijo JSON"
        } else {
            "Datos del Comercio Hijo"
        }

        binding.header.setup(
            title = titulo,
            showBackButton = true,
            showRequestButton = true,
            onBackClick = { finish() },
            onRequestClick = {
                if (currentRequestJson.isNotEmpty()) {
                    binding.header.showRequestJson(currentRequestJson, "REQUEST COMERCIO DATOS")
                } else {
                    Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupFooterButtons() {
        binding.footerButtons.setButtons(
            primaryText = "VOLVER",
            secondaryText = "IR A VENTA MC",
            onPrimaryClick = { finish() },
            onSecondaryClick = {
                if (validarCampos()) {
                    irAVentaMC()
                }
            }
        )
    }

    // NUEVO MÉTODO: Configurar el botón "TRAER DATOS"
    private fun setupTraerDatosButton() {
        binding.btnTraerDatos.setOnClickListener {
            mostrarDialogoIngresarRut()
        }
    }

    // NUEVO MÉTODO: Mostrar diálogo para ingresar RUT y serial number
    private fun mostrarDialogoIngresarRut() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_get_params_input, null)
        val etRutInput = dialogView.findViewById<EditText>(R.id.etRutInput)
        val etSerialInput = dialogView.findViewById<EditText>(R.id.etSerialInput)

        // Valores por defecto
        etRutInput.setText("09091125-2")
        etSerialInput.setText("23C4KD4F9626")

        AlertDialog.Builder(this)
            .setTitle("Obtener Parámetros del Comercio")
            .setMessage("Ingrese los datos para obtener los parámetros del comercio hijo:")
            .setView(dialogView)
            .setPositiveButton("OBTENER") { _, _ ->
                val rut = etRutInput.text.toString()
                val serial = etSerialInput.text.toString()

                if (rut.isNotEmpty() && serial.isNotEmpty()) {
                    tempRut = rut
                    tempSerialNumber = serial
                    enviarGetParamsMC(rut, serial)
                } else {
                    Toast.makeText(this, "Debe ingresar RUT y Serial Number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    // NUEVO MÉTODO: Enviar solicitud GET PARAMS MC
    private fun enviarGetParamsMC(rut: String, serialNumber: String) {
        try {
            // Crear el JSON de solicitud según el formato proporcionado
            val paramsMcRequest = JSONObject().apply {
                put("typeApp", 0) // Usamos typeApp 0 por defecto
                put("rutCommerceSon", rut)
                put("serialNumber", serialNumber)
                put("command", 127) // Comando 127 para PARAMETROS_MULTICOMERCIO
            }.toString()

            Log.d("COMMERCE_DATA", "📤 Enviando solicitud GET PARAMS MC:")
            Log.d("COMMERCE_DATA", "Request JSON: $paramsMcRequest")

            // Crear el Intent para el servicio de parámetros multicomercio
            val intent = Intent("cl.getnet.c2cservice.action.PARAMS_MC_REQUEST")
            intent.putExtra("params", paramsMcRequest)
            intent.putExtra("urlToResponse", "cl.getnet.c2cservice.action.PARAMS_MC_RESPONSE")

            // Usar startActivityForResult para recibir la respuesta
            startActivityForResult(intent, REQUEST_CODE_GET_PARAMS)

        } catch (e: Exception) {
            Log.e("COMMERCE_DATA", "Error al enviar solicitud: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // NUEVO MÉTODO: Procesar la respuesta de GET PARAMS MC
    private fun procesarRespuestaParamsMC(data: Intent?) {
        try {
            // Obtener la respuesta del intent
            val responseJson = data?.getStringExtra("response") ?:
            data?.getStringExtra("params") ?:
            "{}"

            Log.d("COMMERCE_DATA", "📥 Respuesta recibida:")
            Log.d("COMMERCE_DATA", responseJson)

            // Parsear la respuesta JSON
            val jsonResponse = JSONObject(responseJson)

            // Extraer los datos del comercio hijo
            val rut = jsonResponse.optString("rutCommerceSon", tempRut)
            val direccion = jsonResponse.optString("branchAddress", "Dirección no disponible")
            val ciudad = jsonResponse.optString("branchDistrict", "Ciudad no disponible")
            val razonSocial = jsonResponse.optString("legalName", "Razón Social no disponible")
            val nombreFantasia = jsonResponse.optString("branchName", "Nombre no disponible")

            // REEMPLAZAR los datos en las casillas
            binding.etRut.setText(rut)
            binding.etDireccion.setText(direccion)
            binding.etCiudad.setText(ciudad)
            binding.etRazonSocial.setText(razonSocial)
            binding.etNombreFantasia.setText(nombreFantasia)

            // Mostrar mensaje de éxito
            Toast.makeText(this, "✅ Datos obtenidos y cargados correctamente", Toast.LENGTH_LONG).show()

            // Actualizar el request JSON
            actualizarRequestJson()

            // Mostrar los datos obtenidos en un diálogo informativo
            val mensaje = """
                ✅ DATOS ACTUALIZADOS
                
                Se han reemplazado los datos del comercio hijo:
                
                • RUT: $rut
                • Dirección: $direccion
                • Ciudad: $ciudad
                • Razón Social: $razonSocial
                • Nombre Fantasía: $nombreFantasia
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Parámetros Obtenidos")
                .setMessage(mensaje)
                .setPositiveButton("ACEPTAR", null)
                .show()

        } catch (e: Exception) {
            Log.e("COMMERCE_DATA", "Error procesando respuesta: ${e.message}", e)
            Toast.makeText(this, "Error procesando respuesta: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GET_PARAMS) {
            when (resultCode) {
                RESULT_OK -> {
                    procesarRespuestaParamsMC(data)
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val errorMsg = data?.getStringExtra("error") ?:
                    data?.getStringExtra("message") ?:
                    "Error desconocido"
                    Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setDefaultValues() {
        binding.etRut.setText("09091125-2")
        binding.etDireccion.setText("AGUSTINAS 1127")
        binding.etCiudad.setText("Santiago")
        binding.etRazonSocial.setText("Eduardo ione")
        binding.etNombreFantasia.setText("EDUARDO IONE")
    }

    private fun configurarActualizacionRequest() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                actualizarRequestJson()
            }
        }

        binding.etRut.addTextChangedListener(textWatcher)
        binding.etDireccion.addTextChangedListener(textWatcher)
        binding.etCiudad.addTextChangedListener(textWatcher)
        binding.etRazonSocial.addTextChangedListener(textWatcher)
        binding.etNombreFantasia.addTextChangedListener(textWatcher)
    }

    private fun actualizarRequestJson() {
        try {
            val jsonObject = JSONObject().apply {
                put("rut", binding.etRut.text.toString())
                put("direccion", binding.etDireccion.text.toString())
                put("ciudad", binding.etCiudad.text.toString())
                put("razonSocial", binding.etRazonSocial.text.toString())
                put("nombreDeFantasia", binding.etNombreFantasia.text.toString())
            }

            currentRequestJson = jsonObject.toString(4)

        } catch (e: Exception) {
            currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
        }
    }

    private fun validarCampos(): Boolean {
        val rut = binding.etRut.text.toString()
        if (rut.isEmpty()) {
            Toast.makeText(this, "Ingrese RUT", Toast.LENGTH_SHORT).show()
            binding.etRut.requestFocus()
            return false
        }
        if (rut.length > 12) {
            Toast.makeText(this, "RUT no puede exceder 12 caracteres", Toast.LENGTH_SHORT).show()
            binding.etRut.requestFocus()
            return false
        }
        if (!validarFormatoRut(rut)) {
            Toast.makeText(this, "Formato de RUT inválido (use XX.XXX.XXX-X)", Toast.LENGTH_SHORT).show()
            binding.etRut.requestFocus()
            return false
        }

        val direccion = binding.etDireccion.text.toString()
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Ingrese dirección", Toast.LENGTH_SHORT).show()
            binding.etDireccion.requestFocus()
            return false
        }
        if (direccion.length > 50) {
            Toast.makeText(this, "Dirección no puede exceder 50 caracteres", Toast.LENGTH_SHORT).show()
            binding.etDireccion.requestFocus()
            return false
        }

        val ciudad = binding.etCiudad.text.toString()
        if (ciudad.isEmpty()) {
            Toast.makeText(this, "Ingrese ciudad", Toast.LENGTH_SHORT).show()
            binding.etCiudad.requestFocus()
            return false
        }
        if (ciudad.length > 30) {
            Toast.makeText(this, "Ciudad no puede exceder 30 caracteres", Toast.LENGTH_SHORT).show()
            binding.etCiudad.requestFocus()
            return false
        }

        val razonSocial = binding.etRazonSocial.text.toString()
        if (razonSocial.isEmpty()) {
            Toast.makeText(this, "Ingrese razón social", Toast.LENGTH_SHORT).show()
            binding.etRazonSocial.requestFocus()
            return false
        }
        if (razonSocial.length > 100) {
            Toast.makeText(this, "Razón social no puede exceder 100 caracteres", Toast.LENGTH_SHORT).show()
            binding.etRazonSocial.requestFocus()
            return false
        }

        val nombreFantasia = binding.etNombreFantasia.text.toString()
        if (nombreFantasia.length > 50) {
            Toast.makeText(this, "Nombre de fantasía no puede exceder 50 caracteres", Toast.LENGTH_SHORT).show()
            binding.etNombreFantasia.requestFocus()
            return false
        }

        return true
    }

    private fun validarFormatoRut(rut: String): Boolean {
        val rutLimpio = rut.replace(".", "").replace("-", "")
        if (rutLimpio.length < 8 || rutLimpio.length > 9) {
            return false
        }
        val digitoVerificador = rutLimpio.last()
        if (!digitoVerificador.isDigit() && digitoVerificador.uppercaseChar() != 'K') {
            return false
        }
        val numeros = rutLimpio.substring(0, rutLimpio.length - 1)
        if (!numeros.all { it.isDigit() }) {
            return false
        }
        return true
    }

    private fun irAVentaMC() {
        val commerceDataJson = """
        {
            "rut": "${binding.etRut.text}",
            "direccion": "${binding.etDireccion.text}",
            "ciudad": "${binding.etCiudad.text}",
            "razonSocial": "${binding.etRazonSocial.text}",
            "nombreDeFantasia": "${binding.etNombreFantasia.text}"
        }
        """.trimIndent()

        val intent = Intent(this, VentaMCActivity::class.java)
        intent.putExtra("isCommandsMode", isCommandsMode)
        intent.putExtra("commerceDataJson", commerceDataJson)

        startVentaMC.launch(intent)
        finish()
    }
}