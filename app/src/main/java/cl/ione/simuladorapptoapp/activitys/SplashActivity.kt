package cl.ione.simuladorapptoapp.activitys

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import cl.ione.simuladorapptoapp.BuildConfig
import cl.ione.simuladorapptoapp.R

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var isDestroyed = false
    private var startTime = 0L

    companion object {
        private const val MIN_SPLASH_TIME = 2500L // 2.5 segundos mínimo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startTime = System.currentTimeMillis()
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Siempre mostrar el splash (para que se vea la versión)
        setContentView(R.layout.activity_splash)

        // Mostrar versión
        findViewById<TextView>(R.id.txtVersion)?.text = "v${BuildConfig.VERSION_NAME}"

        // Marcar primera vez si corresponde
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }

        // Calcular tiempo restante para cumplir el mínimo de visualización
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = MIN_SPLASH_TIME - elapsedTime

        // Navegar después del tiempo restante (mínimo 2.5 segundos)
        handler.postDelayed({
            if (!isDestroyed) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, if (remainingTime > 0) remainingTime else 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
        handler.removeCallbacksAndMessages(null)
    }
}