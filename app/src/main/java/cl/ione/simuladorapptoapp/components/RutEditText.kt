package cl.ione.simuladorapptoapp.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import cl.ione.simuladorapptoapp.utils.RutUtils

class RutEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var isFormatting = false
    private var rutListener: ((String, Boolean) -> Unit)? = null

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                if (isFormatting) return
                isFormatting = true

                editable?.let {
                    val original = it.toString()
                    val clean = RutUtils.cleanRut(original)

                    if (clean.isNotEmpty()) {
                        val formatted = if (clean.length <= 1) {
                            clean
                        } else {
                            val cuerpo = clean.substring(0, clean.length - 1)
                            val dv = clean.last()
                            val cuerpoFormateado = formatCuerpo(cuerpo)
                            "$cuerpoFormateado-$dv"
                        }

                        if (formatted != original) {
                            it.replace(0, it.length, formatted)
                        }

                        val isValid = clean.length > 1 && RutUtils.validarRut(clean)
                        rutListener?.invoke(clean, isValid)
                    } else {
                        rutListener?.invoke("", false)
                    }
                }

                isFormatting = false
            }
        })
    }

    private fun formatCuerpo(cuerpo: String): String {
        val reversed = cuerpo.reversed()
        val chunks = reversed.chunked(3)
        return chunks.joinToString(".").reversed()
    }

    fun getCleanRut(): String {
        return RutUtils.cleanRut(text.toString())
    }

    fun getFormattedRut(): String {
        return RutUtils.formatRut(text.toString())
    }

    fun isValidRut(): Boolean {
        return RutUtils.validarRut(text.toString())
    }

    fun setRut(rut: String) {
        val clean = RutUtils.cleanRut(rut)
        if (clean.isNotEmpty()) {
            val formatted = if (clean.length <= 1) {
                clean
            } else {
                val cuerpo = clean.substring(0, clean.length - 1)
                val dv = clean.last()
                val cuerpoFormateado = formatCuerpo(cuerpo)
                "$cuerpoFormateado-$dv"
            }
            setText(formatted)
        }
    }

    fun setOnRutChangeListener(listener: (cleanRut: String, isValid: Boolean) -> Unit) {
        this.rutListener = listener
    }
}