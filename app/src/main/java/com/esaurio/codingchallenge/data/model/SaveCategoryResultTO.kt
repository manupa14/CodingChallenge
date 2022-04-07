package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

class SaveCategoryResultTO : ApiResult() {
    @SerializedName("Id")
    var id : Int? = null
}