package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.ione.simuladorapptoapp.databinding.ActivityDevolucionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.setupMoneyFormat
import cl.ione.simuladorapptoapp.components.getCleanMoneyValue
import cl.getnet.payment.interop.parcels.RefundRequest

class DevolucionActivity : AppCompatActivity() {

 private lateinit var binding: ActivityDevolucionBinding
 private val REQUEST_CODE_DEVOLUCION = 3445
 private val TAG = "DevolucionActivity"
 private var isCommandsMode: Boolean = false

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityDevolucionBinding.inflate(layoutInflater)
  setContentView(binding.root)

  isCommandsMode = intent.getBooleanExtra("isCommandsMode", false)

  initViews()
  configurarHeader()
  configurarListeners()
 }

 private fun initViews() {
  binding.etAmount.setupMoneyFormat()
 }

 private fun configurarHeader() {
  val titulo = if (isCommandsMode) "Devolución JSON" else "Devolución"
  binding.header.setup(
   title = titulo,
   showBackButton = true,
   onBackClick = { finish() }
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
    // 🟢 MODO COMANDOS - Objeto RefundRequest
    val request = RefundRequest(authorizationCode, amount, printOnPos, typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "RefundRequest: $authorizationCode, $amount, $printOnPos, $typeApp")
   }

   val actividades = packageManager.queryIntentActivities(intent, 0)
   if (actividades.isNotEmpty()) {
    startActivityForResult(intent, REQUEST_CODE_DEVOLUCION)
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