package com.esaurio.codingchallenge.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.ApiResult
import com.esaurio.codingchallenge.utils.AlertFactory
import com.esaurio.codingchallenge.utils.Utils
import com.esaurio.codingchallenge.utils.hide
import com.esaurio.codingchallenge.utils.show
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, windowInsetsCompat.systemWindowInsetBottom)
            windowInsetsCompat
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        reg_progressBar.hide()
        reg_layFullRegistration.hide()
        reg_viewCode.hide()

        reg_edEmail.setText(intent.getStringExtra("email"))
        reg_edEmail.isEnabled = reg_edEmail.text.isEmpty()

        reg_btRegister.setText(R.string.confirm_email)

        reg_btRegister.setOnClickListener {
            when {
                reg_layFullRegistration.isVisible -> {
                    sendRegistration()
                }
                reg_viewCode.isVisible -> {
                    validateCode()
                }
                else -> {
                    sendCodeToEmail()
                }
            }
        }
        reg_btHasCode.setOnClickListener {
            reg_viewCode.show()
            reg_btHasCode.hide()
            reg_btRegister.setText(R.string.validar_codigo)
        }
        reg_btPasteCode.setOnClickListener {
            loadClipboardCode()
        }
    }

    private fun loadClipboardCode() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard!=null && reg_edCode.isEnabled){
            val clip = clipboard.primaryClip
            if (clip !=null && clip.itemCount > 0){
                clip.getItemAt(0).text?.toString()?.let { text ->
                    if (text.matches("^\\d{4}\\s\\d{4}\\s\\d{2}\$".toRegex())){
                        reg_edCode.setText(text)
                    }
                }
            }
        }
    }

    private fun sendCodeToEmail(){
        val email = reg_edEmail.text.toString()
        if (email.isEmpty()){
            reg_edEmail.setBackgroundResource(R.drawable.edit_text_error)
        }else{
            Utils.hideSoftKeyboard(reg_edEmail)
            reg_edEmail.clearFocus()
            reg_edEmail.setBackgroundResource(R.drawable.edit_text_background)
            reg_btHasCode.hide()
            reg_progressBar.show()
            reg_btRegister.hide()
            reg_edEmail.isEnabled = false
            CodingChallengeAPI.SHARED_INSTANCE.sendRegistrationCode(email,object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    reg_progressBar.hide()
                    reg_btRegister.show()
                    if (data.isResultOK){
                        reg_viewCode.show()
                        reg_btRegister.setText(R.string.validar_codigo)
                    }else{
                        reg_edEmail.isEnabled = true
                        reg_btHasCode.show()
                        AlertFactory.showMessage(this@RegisterActivity, data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    reg_progressBar.hide()
                    reg_btRegister.show()
                    reg_btHasCode.show()
                    reg_edEmail.isEnabled = true
                    AlertFactory.showMessage(this@RegisterActivity,R.string.error_conexion)
                }
            })
        }
    }

    private fun validateCode() {
        val email = reg_edEmail.text.toString()
        val code = reg_edCode.rawText
        if (code.isEmpty()){
            reg_edCode.setBackgroundResource(R.drawable.edit_text_error)
        }else{
            Utils.hideSoftKeyboard(reg_edCode)
            reg_edCode.clearFocus()
            reg_edCode.setBackgroundResource(R.drawable.edit_text_background)
            reg_progressBar.show()
            reg_btRegister.hide()
            reg_edCode.isEnabled = false

            CodingChallengeAPI.SHARED_INSTANCE.isRegistrationCodeValid(email, code,object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    reg_progressBar.hide()
                    reg_btRegister.show()
                    if (data.isResultOK){
                        reg_viewCode.show()
                        reg_layFullRegistration.show()
                        reg_btRegister.setText(R.string.register)
                    }else{
                        reg_edCode.isEnabled = true
                        AlertFactory.showMessage(this@RegisterActivity, data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    reg_progressBar.hide()
                    reg_btRegister.show()
                    reg_edCode.isEnabled = true
                    AlertFactory.showMessage(this@RegisterActivity,R.string.error_conexion)
                }
            })
        }
    }

    private fun sendRegistration() {
        val email = reg_edEmail.text.toString()
        val code = reg_edCode.rawText
        val pass = reg_edPassword.text.toString()
        val pass2 = reg_edPassword2.text.toString()
        reg_txPassLength.isVisible = pass.length < 6
        reg_edPassword.setBackgroundResource(
                if (pass.length < 6 || pass != pass2) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        reg_txPassDontMatch.isVisible = pass != pass2
        reg_edPassword2.setBackgroundResource(
                if (pass != pass2) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        val name = reg_edName.text.toString()
        reg_edName.setBackgroundResource(
                if (name.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        val lastName = reg_edLastName.text.toString()
        reg_edLastName.setBackgroundResource(
                if (lastName.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        val phone = reg_edPhone.text.toString()
        reg_edPhone.setBackgroundResource(
                if (phone.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        Utils.hideSoftKeyboard(reg_edPhone)
        requestFocus.requestFocus()
        if (pass.length >= 6 && pass == pass2 && name.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty()){
            reg_edPassword.isEnabled = false
            reg_edPassword2.isEnabled = false
            reg_edName.isEnabled = false
            reg_edLastName.isEnabled = false
            reg_edPhone.isEnabled = false
            reg_btRegister.hide()
            reg_progressBar.show()
            CodingChallengeAPI.SHARED_INSTANCE.register(email, code, pass, name, lastName, phone, object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    if (data.isResultOK){
                        Prefs.sharedInstance.userEmail = email
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }else{
                        reg_edPassword.isEnabled = true
                        reg_edPassword2.isEnabled = true
                        reg_edName.isEnabled = true
                        reg_edLastName.isEnabled = true
                        reg_edPhone.isEnabled = true
                        reg_progressBar.hide()
                        reg_btRegister.show()
                        AlertFactory.showMessage(this@RegisterActivity, data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    reg_edPassword.isEnabled = true
                    reg_edPassword2.isEnabled = true
                    reg_edName.isEnabled = true
                    reg_edLastName.isEnabled = true
                    reg_edPhone.isEnabled = true
                    reg_progressBar.hide()
                    reg_btRegister.show()
                    AlertFactory.showMessage(this@RegisterActivity,R.string.error_conexion)
                }
            })
        }
    }
}