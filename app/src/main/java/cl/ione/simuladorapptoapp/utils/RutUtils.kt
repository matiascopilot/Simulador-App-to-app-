package cl.ione.simuladorapptoapp.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat

object RutUtils {

    /**
     * Formatea un RUT agregando puntos y guión
     * Ej: 12345678-5 -> 12.345.678-5
     */
    fun formatRut(rut: String): String {
        if (rut.isEmpty()) return ""

        // Limpiar el RUT de puntos y guiones existentes
        val cleanRut = cleanRut(rut)

        // Separar cuerpo y dígito verificador
        val cuerpo = cleanRut.substring(0, cleanRut.length - 1)
        var dv = cleanRut.substring(cleanRut.length - 1)

        // Formatear cuerpo con puntos
        val cuerpoFormateado = formatCuerpo(cuerpo)

        return "$cuerpoFormateado-$dv"
    }

    /**
     * Formatea solo el cuerpo del RUT con puntos
     */
    private fun formatCuerpo(cuerpo: String): String {
        val reversed = cuerpo.reversed()
        val chunks = reversed.chunked(3)
        val withDots = chunks.joinToString(".").reversed()
        return withDots
    }

    /**
     * Limpia el RUT dejando solo números y dígito verificador
     */
    fun cleanRut(rut: String): String {
        return rut.replace("[^0-9kK]".toRegex(), "").uppercase()
    }

    /**
     * Valida el dígito verificador de un RUT usando módulo 11
     */
    fun validarRut(rut: String): Boolean {
        val cleanRut = cleanRut(rut)
        if (cleanRut.length < 2) return false

        val cuerpo = cleanRut.substring(0, cleanRut.length - 1)
        val dvIngresado = cleanRut.last().toString()

        val dvCalculado = calcularDigitoVerificador(cuerpo)

        return dvIngresado.equals(dvCalculado, ignoreCase = true)
    }

    /**
     * Calcula el dígito verificador usando módulo 11
     */
    fun calcularDigitoVerificador(cuerpo: String): String {
        if (cuerpo.isEmpty()) return ""

        var suma = 0
        var multiplicador = 2

        // Recorrer de derecha a izquierda
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = suma % 11
        val dv = 11 - resto

        return when (dv) {
            11 -> "0"
            10 -> "K"
            else -> dv.toString()
        }
    }

    /**
     * TextWatcher para formatear RUT automáticamente mientras se escribe
     */
    class RutTextWatcher : TextWatcher {
        private var isFormatting = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            if (isFormatting) return
            isFormatting = true

            editable?.let {
                val original = it.toString()
                // Limpiar el texto actual
                val clean = cleanRut(original)

                if (clean.isNotEmpty()) {
                    val formatted = if (clean.length <= 1) {
                        clean
                    } else {
                        val cuerpo = clean.substring(0, clean.length - 1)
                        val dv = clean.last()
                        val cuerpoFormateado = formatCuerpo(cuerpo)
                        "$cuerpoFormateado-$dv"
                    }

                    // Solo actualizar si cambió
                    if (formatted != original) {
                        it.replace(0, it.length, formatted)
                    }
                }
            }

            isFormatting = false
        }
    }

    /**
     * Aplica formateo automático a un EditText
     */
    fun EditText.applyRutFormatting() {
        this.addTextChangedListener(RutTextWatcher())
    }
}