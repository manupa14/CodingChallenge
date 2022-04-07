package com.esaurio.codingchallenge.data.api

import android.content.Intent
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.*
import com.esaurio.codingchallenge.BuildConfig
import com.esaurio.codingchallenge.MyApplication
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class JSONUtils {
    companion object {
        val sharedInstance : JSONUtils by lazy { JSONUtils() }
    }

    private val serverUrl : String by lazy { MyApplication.instance.getString(R.string.server_url) }
    internal val apiURL : String by lazy { serverUrl+"Quartz.svc/" }
    private val authURL : String by lazy { serverUrl+"Auth.svc/" }
    private var queue: RequestQueue

    private val retryPolicy: RetryPolicy
        get() {
            return DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        }

    init {
        val cache: Cache =
            DiskBasedCache(MyApplication.instance.cacheDir, 1024 * 1024 * 2) // 2MB cap

        val network: Network = BasicNetwork(HurlStack())
        queue = RequestQueue(cache, network)

        queue.start()
    }

    fun get(
        apiPath: String,
        responseListener: Response.Listener<String>,
        errorListener: Response.ErrorListener?,
        authorized : Boolean = true
    ) {
        val api = if(authorized) apiURL else authURL
        val request: StringRequest = object : StringRequest(
            Method.GET,
            api + apiPath,
            responseListener,
            getErrorListener(authorized, errorListener){
                get(apiPath, responseListener, errorListener, authorized)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders(super.getHeaders())
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    if(BuildConfig.DEBUG)
                        Log.d("JSONUtils","Code: "+response.statusCode+" Response: "+utf8String)
                    return Response.success(
                        utf8String,
                        HttpHeaderParser.parseCacheHeaders(response)
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                return super.parseNetworkResponse(response)
            }

        }
        request.retryPolicy = retryPolicy
        queue.add(request)
    }

    fun get(apiPath: String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener?, params: Map<String, String?>) {
        var urlParams = ""
        for (key in params.keys) {
            try {
                val query = URLEncoder.encode(params[key], "utf-8")
                if (urlParams.isNotEmpty()) urlParams += "&"
                urlParams += "$key=$query"
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        var url = apiPath
        if (urlParams.isNotEmpty()) url += "?$urlParams"
        get(url, responseListener, errorListener)
    }

    fun post(apiPath: String, responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener?, json: JSONObject?, authorized: Boolean = true) {
        val api = if(authorized) apiURL else authURL
        val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                api + apiPath,
                json,
                responseListener,
                getErrorListener(authorized, errorListener){
                    post(apiPath, responseListener, errorListener, json, authorized)
                }
        ) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                val parseNetworkResponse = super.parseNetworkResponse(response)
                if (response.headers.containsKey("Authorization") && response.headers.containsKey("Refresh")){
                    Prefs.sharedInstance.saveAuthTokens(response.headers["Authorization"], response.headers["Refresh"])
                }
                return parseNetworkResponse
            }
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders(super.getHeaders())
            }
        }
        request.retryPolicy = retryPolicy
        request.setShouldCache(false)
        queue.add(request)
    }

    fun postNoValidations(apiPath: String, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener?, json: JSONObject?, authorized: Boolean = true) {
        val api = if(authorized) apiURL else authURL
        val request: JsonRequest<*> = object : JsonRequest<String>(
                Method.POST,
                api + apiPath,
                json?.toString() ?: "",
                responseListener,
                errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders(super.getHeaders())
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    if(BuildConfig.DEBUG)
                        Log.d("JSONUtils","Code: "+response.statusCode+" Response: "+utf8String)
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return Response.success("", HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        request.retryPolicy = retryPolicy
        queue.add(request)
    }

    fun postArrayResponse(apiPath: String, responseListener: Response.Listener<JSONArray>, errorListener: Response.ErrorListener?, json: JSONObject, authorized: Boolean = true) {
        val api = if(authorized) apiURL else authURL
        val request: JsonRequest<*> = object : JsonRequest<JSONArray>(
                Method.POST,
                api + apiPath,
                json.toString(),
                responseListener,
                getErrorListener(authorized, errorListener) {
                    postArrayResponse(apiPath, responseListener, errorListener, json, authorized)
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders(super.getHeaders())
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
                try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    if(BuildConfig.DEBUG)
                        Log.d("JSONUtils","Code: "+response.statusCode+" Response: "+utf8String)
                    return Response.success(JSONArray(utf8String), HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return Response.success(JSONArray(), HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        request.retryPolicy = retryPolicy
        queue.add(request)
    }

    fun uploadFile(file : File,responseListener: Response.Listener<String>, errorListener: Response.ErrorListener?){
        val request = VolleyFileRequest(
                file,
                apiURL + "Upload",
                responseListener,
                getErrorListener(true, errorListener) {
                    uploadFile(file, responseListener, errorListener)
                }
        )
        request.retryPolicy = retryPolicy
        queue.add(request)
    }

    private fun getErrorListener(
        authorized: Boolean,
        errorListener: Response.ErrorListener?,
        retryCallback: () -> Unit
    ) : Response.ErrorListener {
        return Response.ErrorListener {
            if (it?.networkResponse == null) {
                errorListener?.onErrorResponse(it)
            } else if (validateVersion(it.networkResponse)) {
                if (authorized && it.networkResponse.statusCode == 401) {
                    refreshToken { result ->
                        when {
                            Prefs.sharedInstance.isTokenExpired -> MyApplication.instance.sendBroadcast(Intent(Utils.ACTION_RESET_APP))
                            result -> retryCallback()
                            else -> errorListener?.onErrorResponse(it)
                        }
                    }
                } else {
                    errorListener?.onErrorResponse(it)
                }
            }
        }
    }

    @Throws(AuthFailureError::class)
    internal fun getRequestHeaders(headers: MutableMap<String, String>?): MutableMap<String, String> {
        var mHeaders = headers
        if (mHeaders == null || mHeaders == emptyMap<Any, Any>()) {
            mHeaders = HashMap()
        }
        mHeaders["AppVersion"] = BuildConfig.VERSION_NAME
        mHeaders["AppPlatform"] = "A"
        mHeaders["Language"] = Locale.getDefault().language
        Prefs.sharedInstance.authorizationToken?.let {
            mHeaders["Authorization"] = it
        }
        return mHeaders
    }

    private fun refreshToken(completionHandler : (Boolean)->(Unit)){
        val authToken = Prefs.sharedInstance.authorizationToken
        val refreshToken = Prefs.sharedInstance.refreshToken
        if (authToken!=null && refreshToken != null){
            val params = JSONObject()
            params.put("authToken", authToken)
            params.put("refreshToken", refreshToken)
            val request: JsonRequest<String> = object : JsonRequest<String>(
                Method.POST,
                authURL + "RefreshToken",
                params.toString(),
                Response.Listener {
                    completionHandler(true)
                },
                Response.ErrorListener {
                    if (it.networkResponse?.statusCode == 401){
                        Prefs.sharedInstance.isTokenExpired = true
                    }
                    completionHandler(false)
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return getRequestHeaders(super.getHeaders())
                }

                override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                    return try {
                        val utf8String = String(response.data, Charsets.UTF_8)
                        if(BuildConfig.DEBUG)
                            Log.d("JSONUtils","Refresh token. Code: "+response.statusCode+" Response: "+utf8String)

                        if (response.headers.containsKey("Authorization") && response.headers.containsKey("Refresh")){
                            Prefs.sharedInstance.saveAuthTokens(response.headers["Authorization"], response.headers["Refresh"])
                        }else{
                            Prefs.sharedInstance.isTokenExpired = true
                        }
                        Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        Response.error(VolleyError(e))
                    }
                }
            }
            request.retryPolicy = retryPolicy
            queue.add(request)
        }else{
            completionHandler(false)
        }
    }

    private fun validateVersion(response : NetworkResponse) : Boolean{
        return if (response.statusCode == 499){
            MyApplication.instance.sendBroadcast(Intent(Utils.ACTION_RESET_APP))
            false
        }else
            true
    }
}