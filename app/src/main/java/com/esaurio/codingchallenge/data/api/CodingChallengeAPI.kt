package com.esaurio.codingchallenge.data.api

import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.esaurio.codingchallenge.data.model.*
import com.esaurio.codingchallenge.utils.toString
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class CodingChallengeAPI {
    companion object {
        val SHARED_INSTANCE : CodingChallengeAPI by lazy { CodingChallengeAPI() }
    }

    fun login(
        email: String,
        pass: String,
        listener: DataListener<LoginResultTO>
    ) {
        try {
            val json = JSONObject()
            json.put("Username", email)
            json.put("Password", pass)
            JSONUtils.sharedInstance.post(
                "Login",
                Response.Listener { response ->
                    var res: LoginResultTO? = null
                    try {
                        res = Gson().fromJson(response.toString(), LoginResultTO::class.java)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json,
                false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun forgotPassword(
            userName : String,
            listener: DataListener<ApiResult>
    ){
        val json = JSONObject()
        json.put("UserName", userName)
        JSONUtils.sharedInstance.post(
                "ForgotPassword",
                Response.Listener { response ->
                    var res: ApiResult? = null
                    try {
                        res = Gson().fromJson(response.toString(), ApiResult::class.java)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json,
                false
        )
    }

    fun changePassword(
            userName: String,
            code : String,
            pass : String,
            listener: DataListener<ApiResult>
    ){
        val json = JSONObject()
        json.put("UserName", userName)
        json.put("Password", pass)
        json.put("Code", code)
        JSONUtils.sharedInstance.post(
                "ChangePassword",
                Response.Listener { response ->
                    var res: ApiResult? = null
                    try {
                        res = Gson().fromJson(response.toString(), ApiResult::class.java)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json,
                false
        )
    }

    fun validatePasswordCode(
            userName: String,
            code : String,
            listener: DataListener<ApiResult>
    ){
        val json = JSONObject()
        json.put("UserName", userName)
        json.put("Code", code)
        JSONUtils.sharedInstance.post(
                "ValidatePasswordCode",
                Response.Listener { response ->
                    var res: ApiResult? = null
                    try {
                        res = Gson().fromJson(response.toString(), ApiResult::class.java)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json,
                false
        )
    }

    fun sendRegistrationCode(
            email : String,
            listener : DataListener<ApiResult>
    ){
        try {
            val json = JSONObject()
            json.put("UserName", email)
            JSONUtils.sharedInstance.post(
                    "GetRequestRegistrationCode",
                    Response.Listener { response ->
                        var res: ApiResult? = null
                        try {
                            res = Gson().fromJson(response.toString(), ApiResult::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun emailExists(
            email : String,
            listener : DataListener<Boolean>
    ){
        try {
            val json = JSONObject()
            json.put("UserName", email)
            JSONUtils.sharedInstance.post(
                    "ValidateEmail",
                    Response.Listener { response ->
                        var res: Boolean? = null
                        try {
                            res = response.getBoolean("Success")
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun isRegistrationCodeValid(
            email : String,
            code : String,
            listener : DataListener<ApiResult>
    ){
        try {
            val json = JSONObject()
            json.put("UserName", email)
            json.put("Code", code)
            JSONUtils.sharedInstance.post(
                    "ValidateRegistrationCode",
                    Response.Listener { response ->
                        var res: ApiResult? = null
                        try {
                            res = Gson().fromJson(response.toString(), ApiResult::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun register(
            email : String,
            code : String,
            pass : String,
            name : String,
            lastName : String,
            phone : String,
            listener : DataListener<ApiResult>
    ) {
        try {
            val json = JSONObject()
            json.put("UserName", email)
            json.put("Code", code)
            json.put("Password", pass)
            json.put("Name", name)
            json.put("Surname", lastName)
            json.put("PhoneNumber", phone)
            JSONUtils.sharedInstance.post(
                    "RegisterUser",
                    Response.Listener { response ->
                        var res: ApiResult? = null
                        try {
                            res = Gson().fromJson(response.toString(), ApiResult::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getPatients(search : String, page : Int, listener : DataListener<List<Patient>>){
        try {
            val json = JSONObject()
            json.put("Page",page)
            json.put("Search", search)
            JSONUtils.sharedInstance.postArrayResponse(
                "ListPatients",
                Response.Listener { response ->
                    var res: List<Patient>? = null
                    try {
                        res = Gson().fromJson<List<Patient>>(response.toString(), object : TypeToken<List<Patient>>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun savePatient(id : Int?, name : String, lastName : String, gender : String, birthdate : Date, creationDate : Date, notes : String, listener : DataListener<SavePatientResultTO>){
        try {
            val json = JSONObject()
            if (id!=null)
                json.put("Id",id)
            json.put("Name",name)
            json.put("Surname",lastName)
            json.put("Gender",gender)
            json.put("Birthdate",birthdate.toString("yyyy-MM-dd"))
            json.put("EntryDate",creationDate.toString("yyyy-MM-dd"))
            json.put("Comments",notes)
            JSONUtils.sharedInstance.post(
                    "SavePatient",
                    Response.Listener { response ->
                        var res: SavePatientResultTO? = null
                        try {
                            res = Gson().fromJson(response.toString(), SavePatientResultTO::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getPatient(id : Int, listener: DataListener<Patient>){
        try {
            val json = JSONObject()
            json.put("Id",id)
            JSONUtils.sharedInstance.post(
                    "GetPatient",
                    Response.Listener { response ->
                        var res: Patient? = null
                        try {
                            res = Gson().fromJson(response.toString(), Patient::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun deletePatient(id : Int, listener: DataListener<Boolean>? = null){
        try {
            val json = JSONObject()
            json.put("Id",id)
            JSONUtils.sharedInstance.post(
                "DeletePatient",
                Response.Listener { response ->
                    val result = response.getBoolean("Success")
                    Log.d("FirstFitAPI", "DeletePatient result: $result")
                    listener?.onResponse(result)
                },
                getErrorListener(listener),
                json
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun isAppVersionEnabled(listener: DataListener<Boolean>){
        JSONUtils.sharedInstance.postNoValidations("IsVersionEnabled",Response.Listener {
            listener.onResponse(it.equals("true",true))
        },getErrorListener(listener), null, false)
    }

    fun uploadFile(patientId : Int, imageType : PatientPicture.Type?, imageId: Int?, imageName : String?,file : File, listener: DataListener<ApiResult>){
        JSONUtils.sharedInstance.uploadFile(file,Response.Listener {
            var res: ApiResult? = null
            try {
                res = Gson().fromJson(it.toString(), ApiResult::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (res != null) {
                val message = res.resultMessage
                if (res.isResultOK && !message.isNullOrEmpty()){
                    val idx = message.lastIndexOf("\\")
                    val imagePath = if (idx > 0) {
                        message.substring(idx)
                    }else
                        message
                    savePicture(patientId, imageType, imageId, imageName, imagePath, listener)
                }else
                    listener.onResponse(res)
            } else {
                listener.onError(0, "")
            }
        }, getErrorListener(listener))
    }

    private fun savePicture(patientId : Int, imageType: PatientPicture.Type?, imageId: Int?, imageName : String?, imagePath : String, listener: DataListener<ApiResult>){
        try {
            val json = JSONObject()
            json.put("Id", patientId)
            json.put("ImagePath", imagePath)
            imageId?.let { json.put("ImageId", it) }
            imageName?.let { json.put("Name", it) }
            if (imageType != null){
                json.put("ImageNumber", when(imageType){
                    PatientPicture.Type.Type1 -> 1
                    PatientPicture.Type.Type2 -> 2
                    PatientPicture.Type.Type3 -> 3
                    PatientPicture.Type.Type4 -> 4
                    PatientPicture.Type.Type5 -> 5
                })
            }
            JSONUtils.sharedInstance.post(
                    "SavePicture",
                    Response.Listener { response ->
                        var res: ApiResult? = null
                        try {
                            res = Gson().fromJson(response.toString(), ApiResult::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    true
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sharePatient(id : Int,listener: DataListener<ApiResult>){
        try {
            val json = JSONObject()
            json.put("Id", id)
            JSONUtils.sharedInstance.post(
                "ShareCard",
                Response.Listener { response ->
                    var res: ApiResult? = null
                    try {
                        res = Gson().fromJson(response.toString(), ApiResult::class.java)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                    if (res != null) listener.onResponse(res) else listener.onError(0, "")
                },
                getErrorListener(listener),
                json,
                true
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun deletePatientPicture(patientId : Int, imageType : PatientPicture.Type?, imageId : Int?, listener : DataListener<ApiResult>){
        try {
            val json = JSONObject()
            json.put("Id", patientId)
            if (imageId != null){
                json.put("ImageId", imageId)
            }else if (imageType!=null){
                json.put("ImageNumber",when(imageType){
                    PatientPicture.Type.Type1 -> 1
                    PatientPicture.Type.Type2 -> 2
                    PatientPicture.Type.Type3 -> 3
                    PatientPicture.Type.Type4 -> 4
                    PatientPicture.Type.Type5 -> 5
                })
            }
            JSONUtils.sharedInstance.post(
                    "DeletePicture",
                    Response.Listener { response ->
                        var res: ApiResult? = null
                        try {
                            res = Gson().fromJson(response.toString(), ApiResult::class.java)
                        } catch (e: JsonSyntaxException) {
                            e.printStackTrace()
                        }
                        if (res != null) listener.onResponse(res) else listener.onError(0, "")
                    },
                    getErrorListener(listener),
                    json,
                    true
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface ErrorListener {
        fun onError(code: Int, message: String?)
    }

    interface DataListener<T> : ErrorListener {
        fun onResponse(data: T)
    }

    private fun handleError(error: VolleyError, listener: ErrorListener) {
        try {
            val utf8String = String(error.networkResponse.data, Charsets.UTF_8)
            listener.onError(error.networkResponse.statusCode, utf8String)
            Log.e(
                "JSONError",
                error.networkResponse?.statusCode.toString() + ": " + utf8String
            )
        } catch (e: Exception) {
            listener.onError(0, "")
        }
    }

    private fun getErrorListener(listener: ErrorListener? = null): Response.ErrorListener {
        return Response.ErrorListener { error ->
            if (listener != null) handleError(
                error,
                listener
            )
        }
    }
}