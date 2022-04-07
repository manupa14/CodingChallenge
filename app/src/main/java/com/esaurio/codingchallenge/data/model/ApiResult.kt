package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Matias on 11/08/2016.
 */
open class ApiResult {
    @SerializedName("Message")
    var resultMessage: String? = null
    @SerializedName("Success")
    private var resultOK: Boolean = false

    val isResultOK: Boolean
        get() = resultOK

    constructor() {}

    constructor(resultMessage: String, resultOK: Boolean) {
        this.resultMessage = resultMessage
        this.resultOK = resultOK
    }
}
