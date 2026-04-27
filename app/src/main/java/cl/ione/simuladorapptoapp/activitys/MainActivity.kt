package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.getnet.payment.interop.parcels.PrintServiceRequest
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.activitys.MultiComercioActivitys.MulticommerceActivity
import cl.ione.simuladorapptoapp.components.ImageProcessor
import cl.ione.simuladorapptoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isCommandsMode: Boolean = false

    // Variables para el contador de toques
    private var tapCount = 0
    private val tapHandler = Handler(Looper.getMainLooper())
    private val resetTapRunnable = Runnable { tapCount = 0 }

    // Variables para impresión
    private var typeApp: Byte = 0
    private var imagenBase64Cached: String? = null
    private var isImageReady = false

    data class Command(
        val id: Int,
        val name: String,
        val description: String,
        val activityClass: Class<*>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCardListeners()
        setupHeaderListeners()
        setupLogoClickListener()
        precargarImagen()

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            isCommandsMode = savedInstanceState.getBoolean("isCommandsMode", false)
        }

        actualizarUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isCommandsMode", isCommandsMode)
    }

    private fun setupHeaderListeners() {
        // Título clickeable (misma función que arrows)
        binding.tvSectionTitle.setOnClickListener {
            toggleCommandsMode()
        }

        // Flechas (cambiar modo)
        binding.btnSwapCommands.setOnClickListener {
            toggleCommandsMode()
        }

        // Tuerca (abrir configuración)
        binding.btnConfig.setOnClickListener {
            abrirConfiguracion()
        }
    }

    private fun setupLogoClickListener() {
        // Encontrar el ImageView del logo y asignar el listener
        val logoImageView = findViewById<ImageView>(R.id.logo_app_to_app)
        logoImageView?.setOnClickListener {
            // Incrementar contador de toques
            tapCount++

            // Reiniciar el contador después de 2 segundos sin toques
            tapHandler.removeCallbacks(resetTapRunnable)
            tapHandler.postDelayed(resetTapRunnable, 2000)

            // Si llega a 10 toques, ejecutar impresión
            if (tapCount >= 10) {
                tapCount = 0
                tapHandler.removeCallbacks(resetTapRunnable)
                enviarImpresionDirecta()
            }
        }
    }

    private fun enviarImpresionDirecta() {
        try {
            // Mostrar feedback al usuario
            Toast.makeText(this, "Easter Egg Don eduardo", Toast.LENGTH_SHORT).show()

            // Generar JSON de impresión
            val printDataJson = generarPrintDataJson()

            // Crear y enviar el intent de impresión
            val intent = Intent("cl.getnet.payment.action.PRINT_SERVICE")
            val request = PrintServiceRequest(printDataJson.toString(), typeApp)
            intent.putExtra("params", request)
            sendBroadcast(intent)

            // Mostrar resultado (opcional - puedes mostrar un diálogo o Toast)
            Toast.makeText(this, "Easter Egg Don eduardo...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generarPrintDataJson(): org.json.JSONArray {
        var espera = 0
        while (!isImageReady && espera < 20) {
            try {
                Thread.sleep(100)
                espera++
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }

        val imagenBase64 = if (isImageReady && !imagenBase64Cached.isNullOrEmpty()) {
            imagenBase64Cached
        } else {
            // Si no hay imagen, usar una imagen por defecto
            obtenerImagenDefaultBase64()
        }

        return org.json.JSONArray("""
            [
                {
                    "printSeq": 1,
                    "type": "image",
                    "encode": "",
                    "data": "$imagenBase64",
                    "align": "center"
                }
            ]
        """.trimIndent())
    }

    private fun obtenerImagenDefaultBase64(): String {
        // Intentar cargar la imagen por defecto si está disponible
        return try {
            val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.donsergio)
            if (defaultBitmap != null) {
                val base64 = ImageProcessor.procesarImagenDesdeRecurso(defaultBitmap)
                defaultBitmap.recycle()
                base64
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getCurrentDateTime(): String {
        return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun precargarImagen() {
        Thread {
            try {
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.donsergio)
                if (originalBitmap != null) {
                    imagenBase64Cached = ImageProcessor.procesarImagenDesdeRecurso(originalBitmap)
                    originalBitmap.recycle()
                    isImageReady = true
                } else {
                    isImageReady = false
                }
            } catch (_: Exception) {
                isImageReady = false
            }
        }.start()
    }

    private fun toggleCommandsMode() {
        isCommandsMode = !isCommandsMode
        actualizarUI()
    }

    private fun abrirConfiguracion() {
        val intent = Intent(this, ConfigActivity::class.java)
        intent.putExtra("isCommandsMode", isCommandsMode)
        startActivity(intent)
    }

    private fun actualizarUI() {
        binding.tvSectionTitle.text = if (isCommandsMode) {
            getString(R.string.section_shortcuts)
        } else {
            getString(R.string.section_commands)
        }

        binding.btnSwapCommands.setImageResource(
            if (isCommandsMode) R.drawable.arrows else R.drawable.arrows2
        )

        updateIconColors(isCommandsMode)
    }

    private fun setupCardListeners() {
        val cardMappings = mapOf(
            R.id.cardNewSale to VentaActivity::class.java,
            R.id.cardVoid to AnulacionActivity::class.java,
            R.id.cardRefund to DevolucionActivity::class.java,
            R.id.cardDuplicate to DuplicadoActivity::class.java,
            R.id.cardHistory to DetalleVentaActivity::class.java,
            R.id.cardBatch to CierreActivity::class.java,
            R.id.cardPrint to PrintServicesActivity::class.java,
            R.id.cardMulticommerce to MulticommerceActivity::class.java
        )

        cardMappings.forEach { (cardId, activityClass) ->
            findViewById<View>(cardId)?.setOnClickListener {
                abrirActivity(activityClass)
            }
        }
    }

    private fun abrirActivity(activityClass: Class<*>) {
        try {
            val intent = Intent(this, activityClass)
            intent.putExtra("isCommandsMode", isCommandsMode)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error en ${activityClass.simpleName}:\n${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateIconColors(isDarkMode: Boolean) {
        updateSingleIconWithBackground(
            R.id.cardNewSale,
            if (isDarkMode) R.color.red_light else R.color.red_dark,
            if (isDarkMode) R.color.red_dark else R.color.red_light
        )
        updateSingleIconWithBackground(
            R.id.cardVoid,
            if (isDarkMode) R.color.purple_light else R.color.purple_dark,
            if (isDarkMode) R.color.purple_dark else R.color.purple_light
        )
        updateSingleIconWithBackground(
            R.id.cardRefund,
            if (isDarkMode) R.color.teal_light else R.color.teal_dark,
            if (isDarkMode) R.color.teal_dark else R.color.teal_light
        )
        updateSingleIconWithBackground(
            R.id.cardDuplicate,
            if (isDarkMode) R.color.blue_light else R.color.blue_dark,
            if (isDarkMode) R.color.blue_dark else R.color.blue_light
        )
        updateSingleIconWithBackground(
            R.id.cardHistory,
            if (isDarkMode) R.color.amber_light else R.color.amber_dark,
            if (isDarkMode) R.color.amber_dark else R.color.amber_light
        )
        updateSingleIconWithBackground(
            R.id.cardBatch,
            if (isDarkMode) R.color.emerald_light else R.color.emerald_dark,
            if (isDarkMode) R.color.emerald_dark else R.color.emerald_light
        )
        updateSingleIconWithBackground(
            R.id.cardPrint,
            if (isDarkMode) R.color.cyan_light else R.color.cyan_dark,
            if (isDarkMode) R.color.cyan_dark else R.color.cyan_light
        )
    }

    private fun updateSingleIconWithBackground(cardId: Int, tintColorResId: Int, backgroundColorResId: Int) {
        try {
            val cardView = findViewById<androidx.cardview.widget.CardView>(cardId) ?: return
            val linearLayout1 = cardView.getChildAt(0) as? LinearLayout ?: return
            val linearLayout2 = linearLayout1.getChildAt(0) as? LinearLayout ?: return
            val iconCardView = linearLayout2.getChildAt(0) as? androidx.cardview.widget.CardView ?: return
            val imageView = iconCardView.getChildAt(0) as? ImageView ?: return
            val tintColor = ContextCompat.getColor(this, tintColorResId)
            imageView.setColorFilter(tintColor)
            val backgroundColor = ContextCompat.getColor(this, backgroundColorResId)
            iconCardView.setCardBackgroundColor(backgroundColor)

        } catch (e: Exception) {
            // Ignorar errores, no crítico
        }
    }
}