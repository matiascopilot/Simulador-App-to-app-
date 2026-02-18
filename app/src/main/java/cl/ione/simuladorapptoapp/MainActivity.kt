package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.ione.simuladorapptoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isCommandsMode: Boolean = false

    data class Command(
        val id: Int,
        val name: String,
        val description: String,
        val activityClass: Class<*>
    )

    private val commands = listOf(
        Command(1, "Venta", "Transacción de venta", VentaActivity::class.java),
        Command(2, "Anulación", "Anular transacción", AnulacionActivity::class.java),
        Command(3, "Devolución", "Reversar transacción", DevolucionActivity::class.java),
        Command(4, "Duplicado", "Generar duplicado", DuplicadoActivity::class.java),
        Command(5, "Detalle de Ventas", "Ver historial", DetalleVentaActivity::class.java),
        Command(6, "Cierre", "Cierre de lote", CierreActivity::class.java),
        Command(7, "Servicio de impresión", "Impresión", PrintServicesActivity::class.java),
        Command(8, "Venta MC", "Venta multicomercio", CommerceDataActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCardListeners()

        binding.btnSwapCommands.setOnClickListener {
            changeText()
        }

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
            R.id.cardSaleMC to CommerceDataActivity::class.java
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

    private fun changeText() {
        isCommandsMode = !isCommandsMode
        actualizarUI()
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
        updateSingleIconWithBackground(
            R.id.cardSaleMC,
            if (isDarkMode) R.color.orange_light else R.color.orange_dark,
            if (isDarkMode) R.color.orange_dark else R.color.orange_light
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