package cl.ione.simuladorapptoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Version + ambiente
        findViewById<TextView>(R.id.txtVersion).text =
            "v${BuildConfig.VERSION_NAME} | ${BuildConfig.ENVIRONMENT}"

        // Esperar 2 segundos y luego ir a MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}
