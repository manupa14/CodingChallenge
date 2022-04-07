package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.ApiResult
import com.esaurio.codingchallenge.utils.*
import kotlinx.android.synthetic.main.activity_recover_password.*
import kotlinx.android.synthetic.main.activity_recover_password.toolbar

class RecoverPasswordActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_recover_password)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, windowInsetsCompat.systemWindowInsetBottom)
            windowInsetsCompat
        }

        cpass_edEmail.setText(intent.getStringExtra("email"))
        cpass_progressBar.hide()
        cpass_layPasswords.hide()
        cpass_viewCode.hide()

        cpass_btContinue.setOnClickListener {
            if (!cpass_viewCode.isVisible){
                //estamos en el 1er paso. enviamos el mail con el codigo
                sendCode()
            }else if(!cpass_layPasswords.isVisible){
                //estamos en el 2do pas. validamos el codigo ingresado
                validateCode()
            }else{
                //estamos en el último paso. guardamos la nueva contraseña
                changePassword()
            }
        }

        cpass_btHasCode.setOnClickListener {
            cpass_btHasCode.hide()
            cpass_progressBar.hide()
            cpass_btContinue.show()
            cpass_viewCode.show()
        }
        cpass_btPasteCode.setOnClickListener { loadClipboardCode() }

    }

    private fun loadClipboardCode() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard!=null && cpass_edCode.isEnabled){
            val clip = clipboard.primaryClip
            if (clip !=null && clip.itemCount > 0){
                clip.getItemAt(0).text?.toString()?.let { text ->
                    if (text.matches("^\\d{4}\\s\\d{4}\\s\\d{2}\$".toRegex())){
                        cpass_edCode.setText(text)
                    }
                }
            }
        }
    }

    private fun sendCode() {
        val email = cpass_edEmail.text.toString()
        if (email.isValidEmail()){
            Utils.hideSoftKeyboard(cpass_edEmail)
            cpass_edEmail.clearFocus()
            cpass_edEmail.setBackgroundResource(R.drawable.edit_text_background)
            cpass_txInvalidEmail.hide()
            cpass_edEmail.isEnabled = false
            cpass_progressBar.show()
            cpass_btContinue.hide()
            cpass_btHasCode.hide()
            CodingChallengeAPI.SHARED_INSTANCE.forgotPassword(email, object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    if (data.isResultOK){
                        cpass_progressBar.hide()
                        cpass_btContinue.show()
                        cpass_viewCode.show()
                    }else{
                        showError(data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    showError(getString(R.string.error_conexion))
                }

                fun showError(message : String){
                    AlertFactory.showMessage(this@RecoverPasswordActivity, message)
                    cpass_edEmail.isEnabled = true
                    cpass_progressBar.hide()
                    cpass_btContinue.show()
                    cpass_btHasCode.show()
                }
            })
        }else{
            cpass_edEmail.setBackgroundResource(R.drawable.edit_text_error)
            cpass_txInvalidEmail.show()
        }
    }

    private fun validateCode() {
        val email = cpass_edEmail.text.toString()
        val code = cpass_edCode.rawText
        if (code.isEmpty()){
            cpass_edCode.setBackgroundResource(R.drawable.edit_text_error)
        }else{
            Utils.hideSoftKeyboard(cpass_edCode)
            cpass_edCode.clearFocus()
            cpass_edCode.setBackgroundResource(R.drawable.edit_text_background)
            cpass_progressBar.show()
            cpass_btContinue.hide()
            cpass_edCode.isEnabled = false

            CodingChallengeAPI.SHARED_INSTANCE.validatePasswordCode(email, code, object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    if (data.isResultOK){
                        cpass_progressBar.hide()
                        cpass_btContinue.show()
                        cpass_layPasswords.show()
                    }else{
                        showError(data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    showError(getString(R.string.error_conexion))
                }

                fun showError(message : String){
                    AlertFactory.showMessage(this@RecoverPasswordActivity, message)
                    cpass_edCode.isEnabled = true
                    cpass_progressBar.hide()
                    cpass_btContinue.show()
                }
            })
        }
    }

    fun changePassword(){
        val email = cpass_edEmail.text.toString()
        val code = cpass_edCode.rawText
        val pass = cpass_edPassword.text.toString()
        val pass2 = cpass_edPassword2.text.toString()
        cpass_txPassLength.isVisible = pass.length < 6
        cpass_edPassword.setBackgroundResource(
                if (pass.length < 6 || pass != pass2) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        cpass_txPassDontMatch.isVisible = pass != pass2
        cpass_edPassword2.setBackgroundResource(
                if (pass != pass2) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        Utils.hideSoftKeyboard(cpass_edPassword)
        requestFocus.requestFocus()
        if (pass.length >= 6 && pass == pass2){
            cpass_progressBar.show()
            cpass_btContinue.hide()
            cpass_edPassword.isEnabled = false
            cpass_edPassword2.isEnabled = false
            CodingChallengeAPI.SHARED_INSTANCE.changePassword(email, code, pass, object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    if (data.isResultOK){
                        AlertFactory.showMessage(this@RecoverPasswordActivity,R.string.password_changed).setOnDismissListener {
                            val intent = Intent()
                            intent.putExtra("email", email)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }else{
                        showError(data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    showError(getString(R.string.error_conexion))
                }

                fun showError(message : String){
                    AlertFactory.showMessage(this@RecoverPasswordActivity, message)
                    cpass_progressBar.hide()
                    cpass_btContinue.show()
                    cpass_edPassword.isEnabled = true
                    cpass_edPassword2.isEnabled = true
                }
            })
        }
    }
}