package com.mtsenov.formulaex

import com.mtsenov.formulaex.model.CheckFormulaResponse
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {

    @POST("media/math/check/tex")
    fun checkFormula(@Body query: CheckFormulaResponse): Call<CheckFormulaResponse>

}