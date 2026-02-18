package cl.ione.simuladorapptoapp.components

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class MoneyTextWatcher(private val editText: EditText) : TextWatcher {

    private var current = ""
    private val MAX_AMOUNT = 999999999L

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            // Limpiar todo excepto números
            val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

            // Convertir a Long y validar máximo
            var amount = cleanString.toLongOrNull() ?: 0L
            if (amount > MAX_AMOUNT) {
                amount = MAX_AMOUNT
            }

            // Formatear con $ y puntos cada 3 dígitos
            val formatted = formatMoney(amount)

            current = formatted
            editText.setText(formatted)

            // Posicionar cursor al final
            editText.setSelection(formatted.length)

            editText.addTextChangedListener(this)
        }
    }

    /**
     * Formatea un monto con $ y puntos cada 3 dígitos
     * Ejemplo: 1222222 -> $1.222.222
     */
    private fun formatMoney(amount: Long): String {
        if (amount == 0L) return ""

        val numberStr = amount.toString()
        val builder = StringBuilder()
        var count = 0

        for (i in numberStr.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                builder.append('.')
            }
            builder.append(numberStr[i])
            count++
        }

        return "$${builder.reverse().toString()}"
    }

    companion object {
        private const val MAX_AMOUNT = 999999999L

        fun getCleanValue(text: String): Long {
            return text.replace("$", "").replace(".", "").toLongOrNull() ?: 0L
        }

        fun formatMoney(amount: Long): String {
            if (amount == 0L) return ""

            // Validar máximo
            val validAmount = if (amount > MAX_AMOUNT) MAX_AMOUNT else amount

            val numberStr = validAmount.toString()
            val builder = StringBuilder()
            var count = 0

            for (i in numberStr.length - 1 downTo 0) {
                if (count > 0 && count % 3 == 0) {
                    builder.append('.')
                }
                builder.append(numberStr[i])
                count++
            }

            return "$${builder.reverse().toString()}"
        }
    }
}