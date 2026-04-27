package cl.ione.simuladorapptoapp.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ActivityLogcatBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class LogcatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogcatBinding
    private lateinit var btnStartStop: Button
    private lateinit var btnSave: Button
    private lateinit var btnClear: Button
    private lateinit var autoCompleteFilter: AutoCompleteTextView
    private lateinit var tvStatus: TextView
    private lateinit var tvLogCount: TextView

    private var isCapturing = false
    private val handler = Handler(Looper.getMainLooper())
    private val logs = mutableListOf<LogEntry>()
    private var currentFilterPackage = ""
    private var currentFilterName = "TODAS LAS APLICACIONES"
    private var logcatProcess: Process? = null
    private var readerThread: Thread? = null

    data class LogEntry(
        val rawLine: String,
        val level: LogLevel,
        val timestamp: String,
        val pid: String,
        val tag: String,
        val message: String
    )

    enum class LogLevel(val color: Int, val emoji: String) {
        VERBOSE(Color.GRAY, "📝"),
        DEBUG(Color.parseColor("#4FC3F7"), "🔵"),
        INFO(Color.WHITE, "ℹ️"),
        WARNING(Color.parseColor("#FFA726"), "⚠️"),
        ERROR(Color.RED, "❌"),
        UNKNOWN(Color.WHITE, "⚪")
    }

    data class AppInfo(val packageName: String, val appName: String) {
        override fun toString(): String = "$appName ($packageName)" // Muestra nombre + package
    }

    private val appList = mutableListOf<AppInfo>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MAX_LOGS = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogcatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupHeader()
        loadInstalledApps()
        setupAutoComplete()
        setupListeners()
        checkPermissions()

        updateUIForStopped()
    }

    private fun setupViews() {
        btnStartStop = binding.btnStartStop
        btnSave = binding.btnSave
        btnClear = binding.btnClear
        autoCompleteFilter = binding.autoCompleteFilter
        tvStatus = binding.tvStatus
        tvLogCount = binding.tvLogCount

        binding.tvLogcat.movementMethod = ScrollingMovementMethod()

        btnSave.text = "GUARDAR"
        btnSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save, 0, 0, 0)
        btnSave.setBackgroundColor(Color.parseColor("#1976D2"))

        btnClear.text = "LIMPIAR"
        btnClear.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clear, 0, 0, 0)
        btnClear.setBackgroundColor(Color.parseColor("#F57C00"))

        binding.tvLogcat.text = "📱 LOGcat MONITOR\n\n" +
                "1. Selecciona una aplicación del filtro\n" +
                "2. Presiona INICIAR para comenzar\n" +
                "3. Los logs aparecerán aquí\n\n" +
                "💡 Tips:\n" +
                "• Los errores se muestran en ROJO\n" +
                "• Los warnings en NARANJA\n" +
                "• Los logs normales en BLANCO"
    }

    private fun setupHeader() {
        binding.header.setup(
            title = "Logcat Monitor",
            showBackButton = true,
            showRequestButton = false,
            onBackClick = {
                stopLogcat()
                finish()
            }
        )
    }

    private fun loadInstalledApps() {
        appList.clear()
        appList.add(AppInfo("", "📱 TODAS LAS APLICACIONES"))
        appList.add(AppInfo("AndroidRuntime", "💀 SOLO CRASHES"))

        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        for (pkg in packages) {
            pkg.applicationInfo?.let { appInfo ->
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val packageName = pkg.packageName

                if (!packageName.startsWith("com.android.") &&
                    !packageName.startsWith("android.")) {
                    appList.add(AppInfo(packageName, appName))
                }
            }
        }
        appList.sortBy { it.appName.lowercase() }
    }

    private fun setupAutoComplete() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, appList)
        autoCompleteFilter.setAdapter(adapter)
        autoCompleteFilter.threshold = 1
        autoCompleteFilter.setHint("🔍 Buscar aplicación...")

        autoCompleteFilter.setOnClickListener {
            autoCompleteFilter.showDropDown()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(autoCompleteFilter.windowToken, 0)
        }

        autoCompleteFilter.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position)
            selected?.let {
                currentFilterPackage = it.packageName
                currentFilterName = it.appName

                val filterMsg = when {
                    it.packageName.isEmpty() -> "TODAS las apps"
                    it.packageName == "AndroidRuntime" -> "SOLO CRASHES"
                    else -> "${it.appName} (${it.packageName})"
                }

                addSystemLog("📌 Filtro cambiado a: $filterMsg")

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(autoCompleteFilter.windowToken, 0)

                if (isCapturing) {
                    restartLogcat()
                }
            }
        }
    }

    private fun setupListeners() {
        btnStartStop.setOnClickListener {
            if (isCapturing) {
                stopLogcat()
            } else {
                startLogcat()
            }
        }

        btnSave.setOnClickListener {
            saveLogToFile()
        }

        btnClear.setOnClickListener {
            confirmClearLogs()
        }
    }

    private fun confirmClearLogs() {
        AlertDialog.Builder(this)
            .setTitle("Limpiar Logs")
            .setMessage("¿Eliminar todos los logs actuales?")
            .setPositiveButton("LIMPIAR") { _, _ -> clearLogs() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_LOGS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_LOGS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateUIForStopped() {
        btnStartStop.text = "INICIAR"
        btnStartStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0)
        btnStartStop.setBackgroundColor(Color.parseColor("#388E3C"))
        tvStatus.text = "● Detenido"
        tvStatus.setTextColor(Color.parseColor("#F44336"))
        isCapturing = false
    }

    private fun updateUIForRunning() {
        btnStartStop.text = "DETENER"
        btnStartStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)
        btnStartStop.setBackgroundColor(Color.parseColor("#D32F2F"))
        tvStatus.text = "● Capturando..."
        tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        isCapturing = true
    }

    private fun startLogcat() {
        if (isCapturing) return

        updateUIForRunning()
        startLogcatReader()
        addSystemLog("▶️ Captura iniciada")

        val filterMsg = when {
            currentFilterPackage.isEmpty() -> "TODAS las apps"
            currentFilterPackage == "AndroidRuntime" -> "SOLO CRASHES"
            else -> "$currentFilterName ($currentFilterPackage)"
        }
        addSystemLog("📌 Filtro activo: $filterMsg")
    }

    private fun stopLogcat() {
        if (!isCapturing) return

        updateUIForStopped()

        try {
            logcatProcess?.destroy()
            readerThread?.interrupt()
            logcatProcess = null
            readerThread = null
        } catch (e: Exception) { }

        addSystemLog("⏹️ Captura detenida")
    }

    private fun restartLogcat() {
        handler.post {
            if (isCapturing) {
                stopLogcat()
                handler.postDelayed({
                    startLogcat()
                }, 500)
            }
        }
    }

    private fun startLogcatReader() {
        readerThread = Thread {
            while (isCapturing && !Thread.currentThread().isInterrupted) {
                var process: Process? = null
                try {
                    process = executeLogcatCommand()
                    logcatProcess = process

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    val newLogs = mutableListOf<LogEntry>()
                    var lineCount = 0

                    val startTime = System.currentTimeMillis()
                    while (reader.readLine().also { line = it } != null &&
                        lineCount < 300 &&
                        System.currentTimeMillis() - startTime < 1000) {
                        line?.let {
                            val logEntry = parseLogLine(it)
                            if (shouldIncludeLog(logEntry)) {
                                newLogs.add(logEntry)
                                lineCount++
                            }
                        }
                    }

                    if (newLogs.isNotEmpty()) {
                        handler.post {
                            logs.addAll(newLogs)
                            while (logs.size > MAX_LOGS) {
                                logs.removeAt(0)
                            }
                            updateLogDisplay()
                            updateLogCount()
                        }
                    }

                } catch (e: IOException) {
                    // Error de lectura, ignorar
                } catch (e: Exception) {
                    // Otros errores
                } finally {
                    try {
                        process?.destroy()
                    } catch (e: Exception) { }
                }

                try {
                    Thread.sleep(800)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        readerThread?.start()
    }

    private fun executeLogcatCommand(): Process {
        val command = if (currentFilterPackage.isEmpty()) {
            arrayOf("logcat", "-v", "threadtime")
        } else if (currentFilterPackage == "AndroidRuntime") {
            arrayOf("logcat", "-v", "threadtime", "-s", "AndroidRuntime:E")
        } else {
            // Filtrar por package name - usando grep con -i para case insensitive
            arrayOf("sh", "-c", "logcat -v threadtime | grep -i \"$currentFilterPackage\"")
        }

        return Runtime.getRuntime().exec(command)
    }

    private fun parseLogLine(line: String): LogEntry {
        val regex = Regex("""^(\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+\d+\s+([VDIWE])\/([^:]+):\s*(.*)$""")
        val match = regex.find(line)

        return if (match != null) {
            val timestamp = match.groupValues[1]
            val pid = match.groupValues[2]
            val levelChar = match.groupValues[3].firstOrNull() ?: 'I'
            val tag = match.groupValues[4]
            val message = match.groupValues[5]

            val level = when (levelChar) {
                'V' -> LogLevel.VERBOSE
                'D' -> LogLevel.DEBUG
                'I' -> LogLevel.INFO
                'W' -> LogLevel.WARNING
                'E' -> LogLevel.ERROR
                else -> LogLevel.INFO
            }

            LogEntry(line, level, timestamp, pid, tag, message)
        } else {
            val level = when {
                line.contains("FATAL", ignoreCase = true) ||
                        line.contains("Exception", ignoreCase = true) -> LogLevel.ERROR
                line.contains("WARN", ignoreCase = true) -> LogLevel.WARNING
                else -> LogLevel.INFO
            }
            LogEntry(line, level, "", "", "UNKNOWN", line)
        }
    }

    private fun shouldIncludeLog(log: LogEntry): Boolean {
        if (currentFilterPackage.isEmpty()) return true
        if (currentFilterPackage == "AndroidRuntime") {
            return log.level == LogLevel.ERROR ||
                    log.tag == "AndroidRuntime" ||
                    log.message.contains("Exception") ||
                    log.message.contains("FATAL")
        }
        return log.tag.contains(currentFilterPackage, ignoreCase = true) ||
                log.message.contains(currentFilterPackage, ignoreCase = true)
    }

    private fun updateLogDisplay() {
        val spannable = SpannableStringBuilder()

        val logsToShow = if (logs.size > 800) logs.subList(logs.size - 800, logs.size) else logs

        logsToShow.forEach { log ->
            val colorSpan = ForegroundColorSpan(log.level.color)
            val time = if (log.timestamp.isNotEmpty()) "[${log.timestamp.substringAfter(" ")}] " else ""
            val formattedLine = "${log.level.emoji} $time${log.rawLine}\n"
            val start = spannable.length
            spannable.append(formattedLine)
            spannable.setSpan(colorSpan, start, start + formattedLine.length - 1, 0)
        }

        binding.tvLogcat.text = spannable
        scrollToBottom()
    }

    private fun updateLogCount() {
        tvLogCount.text = "${logs.size} logs"
    }

    private fun addSystemLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logLine = "System: $message"
        val logEntry = LogEntry(
            rawLine = logLine,
            level = LogLevel.INFO,
            timestamp = timestamp,
            pid = "0",
            tag = "System",
            message = message
        )
        logs.add(logEntry)
        handler.post {
            updateLogDisplay()
            updateLogCount()
            scrollToBottom()
        }
    }

    private fun clearLogs() {
        logs.clear()
        updateLogDisplay()
        updateLogCount()
        addSystemLog("🗑️ Logs limpiados")
    }

    private fun saveLogToFile() {
        if (logs.isEmpty()) {
            Toast.makeText(this, "No hay logs para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filterName = when {
                currentFilterPackage.isEmpty() -> "Todas"
                currentFilterPackage == "AndroidRuntime" -> "Crashes"
                else -> currentFilterPackage.replace(".", "_")
            }
            val fileName = "logcat_${filterName}_$timestamp.txt"
            val file = File(getExternalFilesDir(null), fileName)

            FileWriter(file).use { writer ->
                writer.write("=== LOGCAT EXPORT ===\n")
                writer.write("Fecha: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                writer.write("App: ${packageName}\n")
                writer.write("Filtro: $currentFilterName ($currentFilterPackage)\n")
                writer.write("Total logs: ${logs.size}\n")
                writer.write("=".repeat(50) + "\n\n")

                logs.forEach { log ->
                    writer.write("${log.rawLine}\n")
                }
            }

            Toast.makeText(this, "✅ Guardado: ${file.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("No se pudo guardar: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun scrollToBottom() {
        binding.svLogcat.post {
            binding.svLogcat.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permiso necesario")
                    .setMessage("Se necesita permiso READ_LOGS para capturar logs")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isCapturing = false
        try {
            logcatProcess?.destroy()
            readerThread?.interrupt()
        } catch (e: Exception) { }
    }
}