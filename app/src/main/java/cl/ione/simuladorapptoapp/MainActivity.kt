package cl.ione.simuladorapptoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    data class Command(
        val id: Int,
        val name: String,
        val description: String,
        val activityClass: Class<*>
    )

    // Lista de comandos
    private val commands = listOf(
        Command(
            id = 1,
            name = "Venta",
            description = "Transacción de venta",
            activityClass = VentaActivity::class.java
        ),
        Command(
            id = 2,
            name = "Anulación",
            description = "Anular transacción",
            activityClass = AnulacionActivity::class.java
        ),
        Command(
            id = 3,
            name = "Devolución",
            description = "Reversar transacción",
            activityClass = DevolucionActivity::class.java
        ),
        Command(
            id = 4,
            name = "Duplicado",
            description = "Generar duplicado",
            activityClass = DuplicadoActivity::class.java
        ),
        Command(
            id = 5,
            name = "Detalle de Ventas",
            description = "Ver historial",
            activityClass = DetalleVentaActivity::class.java
        ),
        Command(
            id = 6,
            name = "Cierre",
            description = "Cierre de lote",
            activityClass = CierreActivity::class.java
        ),
        Command(
            id = 7,
            name = "Servicio de impresión",
            description = "Impresión",
            activityClass = PrintServicesActivity::class.java
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar listeners para cada tarjeta
        setupCardListeners()
    }

    private fun setupCardListeners() {
        // Mapeo de todos los cards a sus Activities
        val cardMappings = mapOf(
            R.id.cardNewSale to VentaActivity::class.java,
            R.id.cardVoid to AnulacionActivity::class.java,
            R.id.cardRefund to DevolucionActivity::class.java,
            R.id.cardDuplicate to DuplicadoActivity::class.java,
            R.id.cardHistory to DetalleVentaActivity::class.java,
            R.id.cardBatch to CierreActivity::class.java,
            R.id.cardPrint to PrintServicesActivity::class.java
        )

        cardMappings.forEach { (cardId, activityClass) ->
            findViewById<View>(cardId)?.setOnClickListener {
                abrirActivity(activityClass)
            }
        }
    }

    private fun abrirActivity(activityClass: Class<*>) {
        try {
            // Verificar si la clase existe realmente
            Class.forName(activityClass.name)

            // Intentar abrir
            startActivity(Intent(this, activityClass))

        } catch (e: ClassNotFoundException) {
            // La clase NO existe
            showInDevelopment(activityClass.simpleName.replace("Activity", ""))

        } catch (e: Exception) {
            // La clase EXISTE pero hay otro error
            Toast.makeText(
                this,
                "Error en ${activityClass.simpleName}:\n${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun showInDevelopment(featureName: String) {
        Toast.makeText(
            this,
            "$featureName - En desarrollo",
            Toast.LENGTH_SHORT
        ).show()
    }
}