package com.esaurio.codingchallenge.data.api

import android.widget.ImageView
import com.esaurio.codingchallenge.BuildConfig
import com.esaurio.codingchallenge.MyApplication
import com.esaurio.codingchallenge.data.Prefs
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.lang.Exception


class ImagesManager {
    companion object {
        val sharedInstance by lazy { ImagesManager() }
    }

        private var client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
                .addHeader("AppVersion", BuildConfig.VERSION_NAME)
                .addHeader("AppPlatform", "A")
            Prefs.sharedInstance.authorizationToken?.let {
                builder.addHeader("Authorization", it)
            }
            val newRequest: Request = builder.build()
            chain.proceed(newRequest)
        }
        .build()

    private val picasso : Picasso by lazy {
        Picasso.Builder(MyApplication.instance)
            .loggingEnabled(true)
            .downloader(OkHttp3Downloader(client))
            .build()
    }

    fun showImage(imagePath : String, imageView : ImageView,placeholderResId : Int? = null, completionHandler : ((Boolean)->(Unit))? = null){
        var request = picasso.load("${JSONUtils.sharedInstance.apiURL}DownloadPicture/$imagePath")
        if (placeholderResId!=null)
            request = request.placeholder(placeholderResId)
        request
            .centerInside().fit()
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    completionHandler?.invoke(true)
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                    completionHandler?.invoke(false)
                }
            })
    }

    fun showImage(file : File, image : ImageView) {
        picasso.load(file).fit().into(image)
    }
}