package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

class Category {
    @SerializedName("Id")
    var id : Int = 0
    @SerializedName("Name")
    var name : String = ""
    @SerializedName("Image")
    var image : String? = null
}