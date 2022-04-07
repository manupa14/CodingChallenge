package com.esaurio.codingchallenge.data.api

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.esaurio.codingchallenge.BuildConfig
import java.io.*

class VolleyFileRequest(
        private val file : File,
        url : String,
        private val responseListener : Response.Listener<String>,
        errorListener : Response.ErrorListener?
) : Request<String>(
        Method.POST,
        url,
        errorListener
) {

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        val requestHeaders = JSONUtils.sharedInstance.getRequestHeaders(super.getHeaders())
        val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf('.'))
        requestHeaders["Extension"] = extension
        return requestHeaders
    }

    override fun getBodyContentType(): String {
        return ""
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
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

    override fun deliverResponse(response: String) {
        responseListener.onResponse(response)
    }


}