package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

class SavePatientResultTO : ApiResult() {
    @SerializedName("Id")
    var id : Int? = null
}