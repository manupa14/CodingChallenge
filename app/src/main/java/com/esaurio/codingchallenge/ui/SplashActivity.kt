package com.esaurio.codingchallenge.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esaurio.codingchallenge.BuildConfig
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startApp()
    }

    private fun showConnectionDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.error_conexion)
            .setPositiveButton(R.string.retry) { _, _ ->
                startApp()
            }
            .setNegativeButton(R.string.exit) { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }.show()
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.update_app_msg)
            .setPositiveButton(R.string.update) { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                finish()
            }
            .setNegativeButton(R.string.exit) { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }.show()
    }

    private fun startApp(){
        if (Prefs.sharedInstance.userEmail != null && !Prefs.sharedInstance.isTokenExpired){
            startActivity(Intent(this,MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }else{
            startActivity(Intent(this,LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}