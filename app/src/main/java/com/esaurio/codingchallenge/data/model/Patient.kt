package com.esaurio.codingchallenge.data.model

import com.esaurio.codingchallenge.utils.createDate
import com.esaurio.codingchallenge.utils.toString
import com.google.gson.annotations.SerializedName
import java.util.*

class Patient {
    @SerializedName("Id")
    var id : Int = 0
    @SerializedName("Name")
    var name : String = ""
    @SerializedName("Surname")
    var lastName : String = ""
    @SerializedName("Birthdate")
    var birthdateStr : String = ""
    @SerializedName("EntryDate")
    var createdAtStr : String = ""
    @SerializedName("LastPicture")
    var lastPictureDateStr : String? = null
    @SerializedName("LastDataModification")
    var lastEditionStr : String? = null
    @SerializedName("LastSubmission")
    var lastSubmissionDateStr : String? = null
    @SerializedName("PicturesCount")
    var picturesCount : Int = 0
    @SerializedName("Comments")
    var notes : String? = null
    @SerializedName("Gender")
    var gender : String = ""
    @SerializedName("Picture1")
    var picture1 : String? = null
    @SerializedName("Picture2")
    var picture2 : String? = null
    @SerializedName("Picture3")
    var picture3 : String? = null
    @SerializedName("Picture4")
    var picture4 : String? = null
    @SerializedName("Picture5")
    var picture5 : String? = null
    @SerializedName("Status")
    var status : String = ""
    @SerializedName("AdditionalPictures")
    var additionalPictures : MutableList<PatientAdditionalPicture> = mutableListOf()

    var lastPictureDate : Date?
        get() = lastPictureDateStr?.let {
            createDate(it, "yyyy-MM-dd HH:mm:ss")
        }
        set(value) {
            lastPictureDateStr = value?.toString("yyyy-MM-dd HH:mm:ss")
        }
    var lastSubmissionDate : Date?
        get() = lastSubmissionDateStr?.let {
            createDate(it, "yyyy-MM-dd HH:mm:ss")
        }
        set(value) {
            lastSubmissionDateStr = value?.toString("yyyy-MM-dd HH:mm:ss")
        }
    var createdAt : Date?
        get() = createDate(createdAtStr, "yyyy-MM-dd HH:mm:ss")
        set(value) {
            createdAtStr = value?.toString("yyyy-MM-dd HH:mm:ss") ?: ""
        }
    val lastEdition : Date?
        get() = lastEditionStr?.let { createDate(it, "yyyy-MM-dd HH:mm:ss") }
    var birthdate : Date?
        get() = createDate(birthdateStr, "yyyy-MM-dd")
        set(value) {
            birthdateStr = value?.toString("yyyy-MM-dd") ?: ""
        }
}