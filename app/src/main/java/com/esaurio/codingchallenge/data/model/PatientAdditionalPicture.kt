package com.esaurio.codingchallenge.data.model

import com.google.gson.annotations.SerializedName

class PatientAdditionalPicture {
    @SerializedName("Id")
    var id : Int = 0
    @SerializedName("Name")
    var name : String = ""
    @SerializedName("PicturePath")
    var picturePath : String = ""
    @SerializedName("PictureNumber")
    var pictureNumber : Int? = null
}