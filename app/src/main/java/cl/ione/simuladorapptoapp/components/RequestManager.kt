package cl.ione.simuladorapptoapp.components

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import org.json.JSONObject

object RequestManager {

    private var currentRequestJson: String = ""
    private var currentTitle: String = "Request"
    private var header: Header? = null

    /**
     * Vincular el Header con el RequestManager
     */
    fun bind(header: Header) {
        this.header = header
        setupHeaderListener()
    }

    /**
     * Configurar el listener del header
     */
    private fun setupHeaderListener() {
        header?.setOnRequestClickListener {
            if (currentRequestJson.isNotEmpty()) {
                header?.showRequestJson(currentRequestJson, currentTitle)
            } else {
                Toast.makeText(header?.context, "No hay request para mostrar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Actualizar el request con un JSONObject
     */
    fun updateRequest(jsonObject: JSONObject, title: String = "Request") {
        currentRequestJson = jsonObject.toString(4)
        currentTitle = title
    }

    /**
     * Actualizar el request con un String JSON
     */
    fun updateRequest(jsonString: String, title: String = "Request") {
        currentRequestJson = try {
            val jsonObject = JSONObject(jsonString)
            jsonObject.toString(4)
        } catch (e: Exception) {
            jsonString
        }
        currentTitle = title
    }

    /**
     * Obtener el request actual
     */
    fun getCurrentRequest(): String = currentRequestJson

    /**
     * Limpiar el request
     */
    fun clear() {
        currentRequestJson = ""
    }

    /**
     * Helper para crear TextWatcher que actualiza automáticamente
     */
    fun createAutoUpdater(updateFunction: () -> JSONObject, title: String = "Request"): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    val jsonObject = updateFunction()
                    updateRequest(jsonObject, title)
                } catch (e: Exception) {
                    // Silently fail
                }
            }
        }
    }

    /**
     * Vincular EditText para actualización automática
     */
    fun bindEditText(vararg editTexts: EditText, updateFunction: () -> JSONObject, title: String = "Request") {
        val watcher = createAutoUpdater(updateFunction, title)
        editTexts.forEach { it.addTextChangedListener(watcher) }
    }

    /**
     * Vincular RadioGroup para actualización automática
     */
    fun bindRadioGroup(radioGroup: RadioGroup, updateFunction: () -> JSONObject, title: String = "Request") {
        radioGroup.setOnCheckedChangeListener { _, _ ->
            try {
                val jsonObject = updateFunction()
                updateRequest(jsonObject, title)
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    /**
     * Vincular Spinner para actualización automática
     */
    fun bindSpinner(spinner: Spinner, updateFunction: () -> JSONObject, title: String = "Request") {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    val jsonObject = updateFunction()
                    updateRequest(jsonObject, title)
                } catch (e: Exception) {
                    // Silently fail
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Vincular CheckBox para actualización automática
     */
    fun bindCheckBox(checkBox: CheckBox, updateFunction: () -> JSONObject, title: String = "Request") {
        checkBox.setOnCheckedChangeListener { _, _ ->
            try {
                val jsonObject = updateFunction()
                updateRequest(jsonObject, title)
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    /**
     * Inicializar con valores por defecto
     */
    fun initWithDefault(updateFunction: () -> JSONObject, title: String = "Request") {
        try {
            val jsonObject = updateFunction()
            updateRequest(jsonObject, title)
        } catch (e: Exception) {
            // Silently fail
        }
    }
}