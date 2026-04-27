package cl.ione.simuladorapptoapp.models

import cl.ione.simuladorapptoapp.utils.RutUtils

data class Rut(
    val value: String
) {
    val cleanValue: String by lazy { RutUtils.cleanRut(value) }
    val formattedValue: String by lazy { RutUtils.formatRut(value) }
    val isValid: Boolean by lazy { RutUtils.validarRut(value) }

    val cuerpo: String
        get() = if (cleanValue.length > 1)
            cleanValue.substring(0, cleanValue.length - 1)
        else ""

    val dv: String
        get() = if (cleanValue.length > 1)
            cleanValue.last().toString()
        else ""

    companion object {
        fun from(cuerpo: String, dv: String): Rut {
            return Rut("$cuerpo$dv")
        }

        fun fromClean(cleanRut: String): Rut {
            return Rut(cleanRut)
        }
    }
}