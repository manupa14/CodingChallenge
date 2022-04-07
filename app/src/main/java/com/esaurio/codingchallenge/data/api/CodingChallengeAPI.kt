package com.esaurio.codingchallenge.data.api

import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.esaurio.codingchallenge.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.File

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

    fun emailExists(
            email : String,
            listener : DataListener<Boolean>
    ){
        try {
            val json = JSONObject()
            json.put("UserName", email)
            JSONUtils.sharedInstance.post(
                    "ValidateUsername",
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

    fun getCategories(search : String, page : Int, listener : DataListener<List<Category>>){
        try {
            val json = JSONObject()
            json.put("Page",page)
            json.put("Search", search)
            JSONUtils.sharedInstance.postArrayResponse(
                "ListCategories",
                Response.Listener { response ->
                    var res: List<Category>? = null
                    try {
                        res = Gson().fromJson<List<Category>>(response.toString(), object : TypeToken<List<Category>>() {}.type)
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

    fun saveCategory(id : Int?, name : String, image : String?, listener : DataListener<SaveCategoryResultTO>){
        try {
            val json = JSONObject()
            if (id!=null)
                json.put("Id",id)
            json.put("Name",name)
            json.put("Image", image)
            JSONUtils.sharedInstance.post(
                    "SaveCategory",
                    Response.Listener { response ->
                        var res: SaveCategoryResultTO? = null
                        try {
                            res = Gson().fromJson(response.toString(), SaveCategoryResultTO::class.java)
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

    fun getCategory(id : Int, listener: DataListener<Category>){
        try {
            val json = JSONObject()
            json.put("Id",id)
            JSONUtils.sharedInstance.post(
                    "GetCategory",
                    Response.Listener { response ->
                        var res: Category? = null
                        try {
                            res = Gson().fromJson(response.toString(), Category::class.java)
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

    fun deleteCategory(id : Int, listener: DataListener<Boolean>? = null){
        try {
            val json = JSONObject()
            json.put("Id",id)
            JSONUtils.sharedInstance.post(
                "DeleteCategory",
                Response.Listener { response ->
                    val result = response.getBoolean("Success")
                    Log.d("CodingChallengeAPI", "DeleteCategory result: $result")
                    listener?.onResponse(result)
                },
                getErrorListener(listener),
                json
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun uploadFile(categoryId : Int?, categoryName : String, file : File, listener: DataListener<SaveCategoryResultTO>){
        JSONUtils.sharedInstance.uploadFile(file, "UploadCategoryImage", Response.Listener {
            var res: SaveCategoryResultTO? = null
            try {
                res = Gson().fromJson(it.toString(), SaveCategoryResultTO::class.java)
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
                    saveCategory(categoryId, categoryName, imagePath, listener)
                }else
                    listener.onResponse(res)
            } else {
                listener.onError(0, "")
            }
        }, getErrorListener(listener))
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