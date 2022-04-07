package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

class LoginResultTO  {
    @SerializedName("ErrorMessage")
    var resultMessage: String? = null
    @SerializedName("ResultOK")
    private var resultOK: Boolean = false

    val isResultOK: Boolean
        get() = resultOK
}