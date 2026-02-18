package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.RefundRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDevolucionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.setupMoneyFormat
import cl.ione.simuladorapptoapp.components.getCleanMoneyValue
import cl.ione.simuladorapptoapp.components.RequestDialog
import org.json.JSONObject

class DevolucionActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDevolucionBinding
 private val REQUEST_CODE_DEVOLUCION = 3445
 private val TAG = "DevolucionActivity"
 private var isCommandsMode: Boolean = false
 private var currentRequestJson: String = "" // Para guardar el request actual

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDevolucionBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

  initViews()
  configurarHeader()
  configurarListeners()
  configurarActualizacionRequest() // Configurar actualización automática
  actualizarRequestJson() // Generar request inicial
 }

 private fun initViews() {
  binding.etAmount.setupMoneyFormat()
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Devolución JSON" else "Devolución"

  binding.header.setup(
   title = titulo,
   showBackButton = true,
   showRequestButton = true, // Mostrar botón de request
   onBackClick = { finish() },
   onRequestClick = {
    // Mostrar el request actual
    if (currentRequestJson.isNotEmpty()) {
     binding.header.showRequestJson(currentRequestJson, "REQUEST DEVOLUCIÓN")
    } else {
     Toast.makeText(this, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
    }
   }
  )
 }

 private fun configurarListeners() {
  binding.footerButtons.setButtons(
   primaryText = "VOLVER",
   secondaryText = "CONFIRMAR",
   onPrimaryClick = { finish() },
   onSecondaryClick = { solicitarDevolucion() }
  )
 }

 // Configurar actualización automática cuando cambian los campos
 private fun configurarActualizacionRequest() {
  // TextWatcher para campos de texto
  val textWatcher = object : android.text.TextWatcher {
   override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
   override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
   override fun afterTextChanged(s: android.text.Editable?) {
    actualizarRequestJson()
   }
  }

  binding.etAuthorizationCode.addTextChangedListener(textWatcher)
  binding.etAmount.addTextChangedListener(textWatcher)

  // Listener para RadioGroup
  binding.rgPrintOnPos.setOnCheckedChangeListener { _, _ ->
   actualizarRequestJson()
  }
 }

 // Actualizar el JSON del request con los valores actuales
 private fun actualizarRequestJson() {
  try {
   val authorizationCode = binding.etAuthorizationCode.text.toString()
   val amount = binding.etAmount.getCleanMoneyValue()
   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0

   val jsonObject = if (isCommandsMode) {
    JSONObject().apply {
     put("AuthorizationCode", authorizationCode)
     put("Amount", amount)
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
     put("PlaceCardToPayTimeout", 20)
     put("PaymentResultTimeout", 2)
    }
   } else {
    JSONObject().apply {
     put("AuthorizationCode", authorizationCode)
     put("Amount", amount)
     put("PrintOnPos", printOnPos)
     put("TypeApp", typeApp)
    }
   }

   currentRequestJson = jsonObject.toString(4)

  } catch (e: Exception) {
   currentRequestJson = "{\"error\": \"Error generando request: ${e.message}\"}"
  }
 }

 private fun solicitarDevolucion() {
  try {
   val authorizationCode = binding.etAuthorizationCode.text.toString()
   if (authorizationCode.isEmpty()) {
    Toast.makeText(this, "Ingrese código de autorización", Toast.LENGTH_SHORT).show()
    binding.etAuthorizationCode.requestFocus()
    return
   }

   if (!authorizationCode.all { Character.isDigit(it) }) {
    Toast.makeText(this, "El código de autorización debe ser numérico", Toast.LENGTH_SHORT).show()
    binding.etAuthorizationCode.requestFocus()
    return
   }
   val amount = binding.etAmount.getCleanMoneyValue()

   if (amount <= 0) {
    Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
    binding.etAmount.requestFocus()
    return
   }

   if (amount > 999999999) {
    Toast.makeText(this, "Monto máximo excedido", Toast.LENGTH_SHORT).show()
    binding.etAmount.requestFocus()
    return
   }

   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0
   val intent = Intent("cl.getnet.payment.action.REFUND")

   if (isCommandsMode) {
    val refundRequestJson = """
                {
                    "AuthorizationCode": "$authorizationCode",
                    "Amount": $amount,
                    "PrintOnPos": $printOnPos,
                    "TypeApp": $typeApp,
                    "PlaceCardToPayTimeout": 20,
                    "PaymentResultTimeout": 2
                }
                """.trimIndent()
    intent.putExtra("params", refundRequestJson)
    Log.d(TAG, "JSON Request: $refundRequestJson")

   } else {
    val request = RefundRequest(authorizationCode, amount, printOnPos, typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "RefundRequest: $authorizationCode, $amount, $printOnPos, $typeApp")
   }

   val actividades = packageManager.queryIntentActivities(intent, 0)
   if (actividades.isNotEmpty()) {
    startActivityForResult(intent, REQUEST_CODE_DEVOLUCION)
    // Actualizar request después de enviar
    actualizarRequestJson()
   } else {
    Toast.makeText(this,
     "Getnet no está disponible en este dispositivo",
     Toast.LENGTH_LONG).show()
   }

  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)
  if (requestCode == REQUEST_CODE_DEVOLUCION) {
   procesarRespuestaDevolucion(resultCode, data)
  }
 }

 private fun procesarRespuestaDevolucion(resultCode: Int, data: Intent?) {
  when (resultCode) {
   RESULT_OK -> {
    JsonParser.showDevolucionResult(this, data)
   }
   RESULT_CANCELED -> {
    val error = data?.getStringExtra("error") ?: "Operación cancelada"
    mostrarResultado("DEVOLUCIÓN CANCELADA\n\n$error")
   }
   else -> {
    val errorMsg = data?.getStringExtra("error") ?:
    data?.getStringExtra("message") ?:
    "Error desconocido"
    mostrarResultado("DEVOLUCIÓN RECHAZADA\n\nMotivo: $errorMsg")
   }
  }
 }

 private fun mostrarResultado(mensaje: String) {
  android.app.AlertDialog.Builder(this)
   .setTitle("Resultado de Devolución")
   .setMessage(mensaje)
   .setPositiveButton("ACEPTAR") { _, _ -> finish() }
   .setCancelable(false)
   .show()
 }
}