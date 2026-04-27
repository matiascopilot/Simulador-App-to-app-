package cl.ione.simuladorapptoapp.models

/**
 * Comandos disponibles en la integración con Getnet POS
 * Incluye tanto el nombre como el código de función y el action intent
 */
enum class CommandType(
    val code: Int,
    val action: String,
    val displayName: String,
    val category: CommandCategory
) {
    // Servicios
    GS_BIP(98, "cl.getnet.payment.action.GS_BIP", "GS BIP", CommandCategory.SERVICE),
    GET_TRANSACTION(99, "cl.getnet.payment.action.GET_TRANSACTION", "Obtener Transacción", CommandCategory.C2C),

    // Comandos estándar
    SALE(100, "cl.getnet.payment.action.SALE", "Venta", CommandCategory.STANDARD),
    CANCELLATION(102, "cl.getnet.payment.action.CANCELLATION", "Anulación", CommandCategory.STANDARD),
    CLOSE(103, "cl.getnet.payment.action.CLOSE", "Cierre", CommandCategory.STANDARD),
    SALES_DETAIL(105, "cl.getnet.payment.action.SALES_DETAIL", "Detalle Ventas", CommandCategory.STANDARD),
    REFUND(108, "cl.getnet.payment.action.REFUND", "Devolución", CommandCategory.STANDARD),
    DUPLICATE(109, "cl.getnet.payment.action.DUPLICATE", "Duplicado", CommandCategory.STANDARD),

    // Servicios adicionales
    PRINT_SERVICE(117, "cl.getnet.c2cservice.action.PRINT_SERVICE", "Servicio Impresión", CommandCategory.SERVICE),
    READ_CARD_PIN(118, "cl.getnet.payment.action.READ_CARD_PIN", "Leer PIN Tarjeta", CommandCategory.APP2APP),
    ACCOUNT_VALIDATION(119, "cl.getnet.payment.action.ACCOUNT_VALIDATION", "Validación Cuenta", CommandCategory.APP2APP),

    // Comandos Multicomercio
    SALE_MC(120, "cl.getnet.payment.action.SALE_MC", "Venta MC", CommandCategory.MULTICOMMERCE),
    BIOMETRIC_VAL(121, "cl.getnet.payment.action.BIOMETRIC_VAL", "Validación Biométrica", CommandCategory.APP2APP),
    CANCELLATION_MC(122, "cl.getnet.payment.action.CANCELLATION_MC", "Anulación MC", CommandCategory.MULTICOMMERCE),
    REFUND_MC(123, "cl.getnet.payment.action.REFUND_MC", "Devolución MC", CommandCategory.MULTICOMMERCE),
    MAIN_POS_DATA(124, "cl.getnet.payment.action.MAIN_POS_DATA", "Main Pos Data", CommandCategory.MULTICOMMERCE),

    // Más comandos
    EMV_VALIDATION(125, "cl.getnet.payment.action.EMV_VALIDATION", "Validación EMV", CommandCategory.APP2APP),
    SALE_PROMO(126, "cl.getnet.payment.action.SALE_PROMO", "Venta Promo", CommandCategory.C2C),
    GET_PARAMS_MC(127, "cl.getnet.payment.action.GET_PARAMS_MC", "Obtener Parámetros MC", CommandCategory.C2C),
    SALES_DETAIL_MC(133, "cl.getnet.payment.action.SALES_DETAIL_MC", "Detalle Ventas MC", CommandCategory.APP2APP),
    SCANNER_SERVICE(128, "cl.getnet.c2cservice.action.SCANNER_SERVICE", "Servicio Scanner", CommandCategory.SERVICE),
    UNATENDED_MODE(129, "cl.getnet.payment.action.UNATENDED_MODE", "Modo Desatendido", CommandCategory.C2C);

    companion object {
        fun fromCode(code: Int): CommandType? = values().find { it.code == code }
        fun fromAction(action: String): CommandType? = values().find { it.action == action }
        fun getByCategory(category: CommandCategory): List<CommandType> = values().filter { it.category == category }

        // Para usar en filtros del historial
        fun getDisplayNameForHistory(historyCommand: String): String {
            return when (historyCommand) {
                "VENTA" -> SALE.displayName
                "ANULACIÓN" -> CANCELLATION.displayName
                "DEVOLUCIÓN" -> REFUND.displayName
                "CIERRE" -> CLOSE.displayName
                "DETALLE VENTA" -> SALES_DETAIL.displayName
                "DUPLICADO" -> DUPLICATE.displayName
                "VENTA MC" -> SALE_MC.displayName
                "ANULACIÓN MC" -> CANCELLATION_MC.displayName
                "DEVOLUCIÓN MC" -> REFUND_MC.displayName
                "DETALLE MC" -> SALES_DETAIL_MC.displayName
                "MAIN POS DATA" -> MAIN_POS_DATA.displayName
                "IMPRESIÓN" -> PRINT_SERVICE.displayName
                else -> historyCommand
            }
        }

        // Para obtener el código de función desde el nombre del historial
        fun getFunctionCodeFromHistory(historyCommand: String): Int? {
            return when (historyCommand) {
                "VENTA" -> SALE.code
                "ANULACIÓN" -> CANCELLATION.code
                "DEVOLUCIÓN" -> REFUND.code
                "CIERRE" -> CLOSE.code
                "DETALLE VENTA" -> SALES_DETAIL.code
                "DUPLICADO" -> DUPLICATE.code
                "VENTA MC" -> SALE_MC.code
                "ANULACIÓN MC" -> CANCELLATION_MC.code
                "DEVOLUCIÓN MC" -> REFUND_MC.code
                "DETALLE MC" -> SALES_DETAIL_MC.code
                "MAIN POS DATA" -> MAIN_POS_DATA.code
                "IMPRESIÓN" -> PRINT_SERVICE.code
                else -> null
            }
        }
    }
}

/**
 * Categorías de comandos para organización
 */
enum class CommandCategory {
    STANDARD,       // Comandos estándar (venta, anulación, etc.)
    MULTICOMMERCE,  // Comandos multicomercio
    C2C,            // Comandos Cardholder to Cardholder
    APP2APP,        // Comandos App to App
    SERVICE         // Servicios adicionales
}