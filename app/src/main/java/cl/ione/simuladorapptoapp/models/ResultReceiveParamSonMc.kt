package cl.ione.simuladorapptoapp.models

data class ResultReceiveParamSonMc(
    val receive: Boolean = false,
    val isTimeout: Boolean = false,
    val isIntentCommand: Boolean = false,
    val commerceData: String? = null,
    val error: String? = null
)