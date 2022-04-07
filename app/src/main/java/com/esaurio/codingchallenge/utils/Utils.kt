package com.esaurio.codingchallenge.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.esaurio.codingchallenge.BuildConfig
import com.esaurio.codingchallenge.MyApplication

class Utils {

    companion object {
        const val ACTION_RESET_APP = BuildConfig.APPLICATION_ID+".ActionResetApp"

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun hideSoftKeyboard(view: View) {
            val imm = MyApplication.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun applyEdgeToEdgeConfig(window: Window){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val view = window.decorView
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    window.setDecorFitsSystemWindows(false)
                }else{
                    view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                }
            }
        }

        fun openAppsSettings(ctx: Activity, requestCode: Int) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", ctx.packageName, null)
                intent.data = uri
                ctx.startActivityForResult(intent, requestCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun CharSequence?.isValidEmail() = !this.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

@ColorInt
        /**
         * Devuelve el color asociado al ID de recurso que este Int representa.
         */
fun @receiver:ColorRes Int.toColorInt() : Int{
    return ContextCompat.getColor(MyApplication.instance,this)
}