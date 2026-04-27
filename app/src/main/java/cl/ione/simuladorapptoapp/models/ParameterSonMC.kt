package cl.ione.simuladorapptoapp.models

import com.google.gson.annotations.SerializedName

data class ParameterSonMC(
    @SerializedName("command") var command: Int = 0,
    @SerializedName("serialNumber") var serialNumber: String = "",
    @SerializedName("rutCommerceSon") var rutCommerceSon: String = ""
)