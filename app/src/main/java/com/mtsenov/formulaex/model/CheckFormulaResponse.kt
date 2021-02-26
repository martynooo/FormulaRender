package com.mtsenov.formulaex.model

import com.google.gson.annotations.SerializedName

class CheckFormulaResponse(
    @SerializedName("q")
    var q: String
) {
    @SerializedName("success")
    var success: Boolean? = null

    @SerializedName("checked")
    var checked: String? = null

    @SerializedName("requiredPackages")
    var requiredPackages: List<String>? = null

    @SerializedName("identifiers")
    var identifiers: List<String>? = null

    @SerializedName("endsWithDot")
    var endsWithDot: Boolean? = null
}