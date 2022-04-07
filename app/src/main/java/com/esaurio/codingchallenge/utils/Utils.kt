package com.esaurio.codingchallenge.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
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
    }
}

fun CharSequence?.isUsernameValid() = !this.isNullOrEmpty()

@ColorInt
        /**
         * Devuelve el color asociado al ID de recurso que este Int representa.
         */
fun @receiver:ColorRes Int.toColorInt() : Int{
    return ContextCompat.getColor(MyApplication.instance,this)
}