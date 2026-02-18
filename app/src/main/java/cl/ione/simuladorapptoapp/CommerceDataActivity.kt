package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityCommerceDataBinding
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class CommerceDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommerceDataBinding
    private var isCommandsMode: Boolean = false
    private var currentRequestJson: String = "" // Para guardar el request actual

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
        setDefaultValues()
        configurarActualizacionRequest() // Configurar actualización automática
        actualizarRequestJson() // Generar request inicial
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
            showRequestButton = true, // Mostrar botón de request
            onBackClick = { finish() },
            onRequestClick = {
                // Mostrar el request actual
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

    private fun setDefaultValues() {
        binding.etRut.setText("7055871-8")
        binding.etDireccion.setText("Las bellotas 199")
        binding.etCiudad.setText("Santiago")
        binding.etRazonSocial.setText("Movired")
        binding.etNombreFantasia.setText("Simulador A2A MC")
    }

    // Configurar actualización automática cuando cambian los campos
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

    // Actualizar el JSON del request con los valores actuales
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