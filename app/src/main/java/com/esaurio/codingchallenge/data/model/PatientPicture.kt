package com.esaurio.codingchallenge.data.model

class PatientPicture(
        val type: Type?,
        val filePath : String? = null,
        val name : String? = null,
        val id : Int? = null
) {


    enum class Type {
        Type1, Type2, Type3, Type4, Type5
    }
}