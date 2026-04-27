package cl.ione.simuladorapptoapp.models

data class SaleMcRequest(
    val Amount: Long,
    val PrintOnPos: Boolean,
    val SaleType: Int,
    val TypeApp: Int,
    val RutCommerceSon: String?,
    val RestrictedCards: List<String>,
    val AllowedCards: List<String>,
    val CommerceData: CommerceData?,
    val CommerceParams: CommerceParams?,
    val PlaceCardToPayTimeout: Int,
    val PaymentResultTimeout: Int
)

data class CommerceData(
    val LegalName: String,
    val CommerceNumber: String,
    val CommerceRut: String,
    val BranchNumber: String,
    val BranchName: String,
    val LittleBranchName: String,
    val BranchAddress: String,
    val BranchDistrict: String,
    val TerminaId: String,
    val SerialNumber: String
)

data class CommerceParams(
    val IndicadorBimoneda: Int,
    val IndicadorBoleta: Int,
    val IndicadorNoVendedor: Int,
    val IndicadorComprobanteComoBoleta: Int,
    val IndicadorPropina: Int,
    val IndicadorVuelto: Int,
    val IndicadorCuotasEmisor: Int,
    val MinimoCuotasEmisor: Int,
    val MaximoCuotasEmisor: Int,
    val IndicadorCuotasComercio: Int,
    val MinimoCuotasComercio: Int,
    val MaximoCuotasComercio: Int,
    val IndicadorCuotasTasaCero: Int,
    val MinimoCuotasTasaCero: Int,
    val MaximoCuotasTasaCero: Int,
    val IndicadorCuotasTasaInteresConocida: Int,
    val MinimoCuotasTasaInteresConocida: Int,
    val MaximoCuotasTasaInteresConocida: Int,
    val TipoProductoCreditoVisa: Int,
    val TipoProductoDebitoVisa: Int,
    val TipoProductoDebitoVisaElectron: Int,
    val TipoProductoPrepagoVisa: Int,
    val TipoProductoCreditoMastercard: Int,
    val TipoProductoDebitoMastercard: Int,
    val TipoProductoDebitoMaestro: Int,
    val TipoProductoPrepagoMastercard: Int,
    val TipoProductoCreditoAmex: Int,
    val TipoProductoDebitoAmex: Int,
    val TipoProductoPrepagoAmex: Int,
    val TipoProductoMagna: Int,
    val NumeroDeFolio: Int,
    val PosAvance: Int
)