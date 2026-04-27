package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.getnet.payment.interop.parcels.RefundRequest
import cl.ione.simuladorapptoapp.databinding.ActivityDevolucionBinding
import cl.ione.simuladorapptoapp.components.JsonParser
import cl.ione.simuladorapptoapp.components.RequestManager  // ← IMPORTAR
import cl.ione.simuladorapptoapp.components.setupMoneyFormat
import cl.ione.simuladorapptoapp.components.getCleanMoneyValue
import org.json.JSONObject

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
  setupRequestManager()  // ← NUEVO
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
   showRequestButton = true,
   onBackClick = { finish() }
   // onRequestClick lo maneja RequestManager
  )
 }

 // 🎯 NUEVO: Setup de RequestManager
 private fun setupRequestManager() {
  // 1. Vincular el header con RequestManager
  RequestManager.bind(binding.header)

  // 2. Función que genera el JSON con los valores actuales
  val buildRequestJson = {
   val authorizationCode = binding.etAuthorizationCode.text.toString()
   val amount = binding.etAmount.getCleanMoneyValue()
   val printOnPos = binding.rgPrintOnPos.checkedRadioButtonId == binding.rbPrintYes.id
   val typeApp: Byte = 0

   if (isCommandsMode) {
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
  }

  // 3. Vincular los campos automáticamente
  val titulo = if (isCommandsMode) "Devolución JSON" else "Devolución (Librería)"

  // Bind EditTexts
  RequestManager.bindEditText(
   binding.etAuthorizationCode, binding.etAmount,
   updateFunction = buildRequestJson,
   title = titulo
  )

  // Bind RadioGroup
  RequestManager.bindRadioGroup(
   binding.rgPrintOnPos,
   updateFunction = buildRequestJson,
   title = titulo
  )

  // 4. Inicializar con valores por defecto
  RequestManager.initWithDefault(buildRequestJson, titulo)
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
    val request = RefundRequest(authorizationCode, amount, printOnPos, typeApp)
    intent.putExtra("params", request)
    Log.d(TAG, "RefundRequest: $authorizationCode, $amount, $printOnPos, $typeApp")
   }
    startActivityForResult(intent, REQUEST_CODE_DEVOLUCION)
  } catch (e: Exception) {
   Log.e(TAG, "Error: ${e.message}", e)
   Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
  }
 }

 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)

  if (requestCode == REQUEST_CODE_DEVOLUCION) {
   val requestData = RequestManager.getCurrentRequest()

   when (resultCode) {
    RESULT_OK -> {
     JsonParser.showDevolucionResult(this, data, requestData = requestData)
    }

    RESULT_CANCELED -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DEVOLUCIÓN CANCELADA",
      onRetry = { solicitarDevolucion() }
     )
    }

    else -> {
     JsonParser.showErrorWithRetry(
      activity = this,
      data = data,
      title = "DEVOLUCIÓN RECHAZADA",
      onRetry = { solicitarDevolucion() }
     )
    }
   }
  }
 }
}