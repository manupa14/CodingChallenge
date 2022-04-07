package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.util.Pair
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.LoginResultTO
import com.esaurio.codingchallenge.utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    /** Email que fue validado en el server y por lo tanto pertenece a un usuario. */
    private var validEmail : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_login)


        login_mainCard.startAnimation(AnimationUtils.loadAnimation(this,R.anim.in_from_top))

        //configuramos para solicitar solo el email
        login_txSessionExpired.hide()
        login_btLogout.hide()
        login_progressBar.hide()
        login_viewPassword.hide()
        login_txError.hide()
        login_btLogin.setText(R.string.continuar)

        login_btLogin.setOnClickListener {
            login_txError.hide()
            if (login_viewPassword.isVisible){
                //ya hemos validado que el email existe
                login()
            }else{
                //procedemos a validar que el email ingresado exista
                validateEmail()
            }
        }

        login_edEmail.addTextChangedListener {
            if (it?.toString() != validEmail){
                if (login_viewPassword.isVisible){
                    login_txEnterMail.show()
                    login_viewPassword.hide()
                    login_btLogin.setText(R.string.continuar)
                }
            }else{
                if (!login_viewPassword.isVisible){
                    login_txEnterMail.hide()
                    login_viewPassword.show()
                    login_btLogin.setText(R.string.login)
                }
            }
        }

        val loggedUserEmail = Prefs.sharedInstance.userEmail
        if (!loggedUserEmail.isNullOrEmpty()){
            validEmail = loggedUserEmail
            login_txEnterMail.hide()
            login_txSessionExpired.show()
            login_edEmail.setText(loggedUserEmail)
            login_edEmail.isEnabled = false
            login_viewPassword.show()
            login_btLogout.show()
            login_btLogin.setText(R.string.login)
            login_btLogout.setOnClickListener {
                AlertFactory.showQuestion(this, getString(R.string.confirm_logout), DialogInterface.OnClickListener { _, _ ->
                    Prefs.sharedInstance.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                })
            }
        }
    }

    private fun login(){
        val email = login_edEmail.text.toString()
        val pass = login_edPassword.text.toString()
        login_edEmail.setBackgroundResource(
                if (email.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        login_edPassword.setBackgroundResource(
                if (pass.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        if (email.isNotEmpty() && pass.isNotEmpty()){
            login_edPassword.isEnabled = false
            login_edEmail.isEnabled = false
            login_progressBar.show()
            login_btLogin.hide()
            login_btLogout.hide()
            CodingChallengeAPI.SHARED_INSTANCE.login(email, pass, object : CodingChallengeAPI.DataListener<LoginResultTO> {
                override fun onResponse(data: LoginResultTO) {
                    if (data.isResultOK){
                        Prefs.sharedInstance.userEmail = email
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        val pair1 : Pair<View?, String?>? = ViewCompat.getTransitionName(login_imLogo)?.let { Pair(login_imLogo, it) }
                        val options = if (pair1 != null){
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity, pair1)
                        }else
                            null
                        ActivityCompat.startActivity(this@LoginActivity, intent, options?.toBundle())
                    }else{
                        showError(data.resultMessage ?: getString(R.string.invalid_login))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    showError(getString(R.string.error_conexion))
                }

                private fun showError(errorMessage: String) {
                    login_txError.text = errorMessage
                    login_progressBar.hide()
                    login_btLogin.show()
                    login_txError.show()
                    login_btLogout.isVisible = Prefs.sharedInstance.userEmail != null
                    login_edPassword.isEnabled = true
                    login_edEmail.isEnabled = true
                }
            })
        }
    }

    private fun validateEmail(){
        val email = login_edEmail.text.toString()
        if (email.isUsernameValid()){
            login_edEmail.setBackgroundResource(R.drawable.edit_text_background)
            login_edEmail.isEnabled = false
            Utils.hideSoftKeyboard(login_edEmail)
            login_edEmail.clearFocus()
            login_progressBar.show()
            login_btLogin.hide()
            CodingChallengeAPI.SHARED_INSTANCE.emailExists(email, object : CodingChallengeAPI.DataListener<Boolean> {
                override fun onResponse(data: Boolean) {
                    login_edEmail.isEnabled = true
                    login_progressBar.hide()
                    login_btLogin.show()
                    if (data){
                        validEmail = email
                        login_txEnterMail.hide()
                        login_viewPassword.show()
                        login_btLogin.setText(R.string.login)
                    }else{
                        login_edEmail.setBackgroundResource(R.drawable.edit_text_error)
                        login_txError.setText(R.string.username_invalido)
                        login_txError.show()
                    }
                }

                override fun onError(code: Int, message: String?) {
                    login_edEmail.isEnabled = true
                    login_progressBar.hide()
                    login_btLogin.show()
                    login_txError.setText(R.string.error_conexion)
                    login_txError.show()
                }
            })

        }else{
            login_edEmail.setBackgroundResource(R.drawable.edit_text_error)
            login_txError.setText(R.string.username_invalido)
            login_txError.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_RECOVER_PASS && resultCode == Activity.RESULT_OK){
            data?.getStringExtra("email")?.let { email ->
                validEmail = email
                login_edEmail.setText(email)
                login_viewPassword.show()
                login_btLogin.setText(R.string.login)
            }
        }
    }

    companion object {
        const val RC_RECOVER_PASS = 1
    }

}