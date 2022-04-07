package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.Patient
import com.esaurio.codingchallenge.data.model.SavePatientResultTO
import com.esaurio.codingchallenge.utils.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_form_patient.*
import java.util.*

class PatientFormActivity : BaseActivity(){

    private var patient : Patient? = null
    private var birthdate : Date? = null
    private var gender : String? = null
    private var creationDate : Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_form_patient)
        setSupportActionBar(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        ViewCompat.setOnApplyWindowInsetsListener(fpat_scrollView){ v, windowInsetCompat ->
            v.setPadding(0, 0, 0, windowInsetCompat.systemWindowInsetBottom)
            windowInsetCompat
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra("patient"))
            patient = Gson().fromJson(intent.getStringExtra("patient"), Patient::class.java)

        supportActionBar?.title = if (patient!=null)
            getString(R.string.edit_patient)
        else
            getString(R.string.new_patient)

        fpat_txCreationDate.text = creationDate.toString("dd/MM/yyyy")

        patient?.let { loadForm(it) }

        requestFocus.requestFocus()

        configureForm()
    }

    private fun loadForm(patient : Patient){
        fpat_edName.setText(patient.name)
        fpat_edLastName.setText(patient.lastName)
        fpat_txGender.text = getString(if (patient.gender == "M") R.string.gender_masculine else R.string.gender_femenine)
        fpat_edNotes.setText(patient.notes)
        birthdate = patient.birthdate
        gender = patient.gender
        creationDate = patient.createdAt ?: Date()
        fpat_txBirthdate.text = birthdate?.toString("dd/MM/yyyy")
        fpat_txCreationDate.text = creationDate.toString("dd/MM/yyyy")
    }

    private fun configureForm(){
        fpat_progressBar.hide()
        fpat_btGender.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            val genders = arrayOf(
                    getString(R.string.gender_masculine),
                    getString(R.string.gender_femenine)
            )
            val gendersCodes = arrayOf("M","F")
            alert.setItems(genders) { _, which ->
                fpat_txGender.text = genders[which]
                gender = gendersCodes[which]
            }
            alert.show()
        }
        fpat_btBirthdate.setOnClickListener {
            val cal = Calendar.getInstance()
            val currentBirthdate = birthdate
            if (currentBirthdate!=null)
                cal.time = currentBirthdate
            else
                cal.add(Calendar.YEAR, -18)

            DatePickerDialog(ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog), DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                birthdate = cal.time
                fpat_txBirthdate.text = birthdate?.toString("dd/MM/yyyy")
            },cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show()
        }
        fpat_btCreationDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = creationDate
            DatePickerDialog(ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog), DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                creationDate = cal.time
                fpat_txCreationDate.text = creationDate.toString("dd/MM/yyyy")
            },cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show()
        }
        fpat_btSave.setOnClickListener { save() }
    }

    private fun save(){
        val name = fpat_edName.text.toString()
        val lastName = fpat_edLastName.text.toString()
        val birthdate = this.birthdate
        val gender = this.gender
        val notes = fpat_edNotes.text.toString()
        fpat_edName.setBackgroundResource(
                if (name.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        fpat_edLastName.setBackgroundResource(
                if (lastName.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        fpat_btBirthdate.setBackgroundResource(
                if (birthdate == null) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        fpat_btGender.setBackgroundResource(
                if (gender.isNullOrEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        requestFocus.requestFocus()
        Utils.hideSoftKeyboard(fpat_edNotes)
        if ( name.isNotEmpty() && lastName.isNotEmpty() && birthdate != null && !gender.isNullOrEmpty() ){
            fpat_btSave.hide()
            fpat_progressBar.show()
            fpat_edNotes.isEnabled = false
            fpat_edName.isEnabled = false
            fpat_edLastName.isEnabled = false
            fpat_btBirthdate.isEnabled = false
            fpat_btGender.isEnabled = false
            fpat_btCreationDate.isEnabled = false

            CodingChallengeAPI.SHARED_INSTANCE.savePatient(patient?.id, name, lastName, gender, birthdate, creationDate, notes,object : CodingChallengeAPI.DataListener<SavePatientResultTO>{
                override fun onResponse(data: SavePatientResultTO) {
                    if (data.isResultOK){
                        val intent = Intent()
                        data.id?.let { intent.putExtra("id",it) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }else{
                        enableForm()
                        AlertFactory.showMessage(this@PatientFormActivity, data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    enableForm()
                    AlertFactory.showMessage(this@PatientFormActivity, R.string.error_conexion)
                }

                private fun enableForm(){
                    fpat_btSave.show()
                    fpat_progressBar.hide()
                    fpat_edNotes.isEnabled = true
                    fpat_edName.isEnabled = true
                    fpat_edLastName.isEnabled = true
                    fpat_btBirthdate.isEnabled = true
                    fpat_btGender.isEnabled = true
                    fpat_btCreationDate.isEnabled = true
                }
            })
        }
    }

}