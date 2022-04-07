package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.ApiResult
import com.esaurio.codingchallenge.data.model.Patient
import com.esaurio.codingchallenge.data.model.PatientPicture
import com.esaurio.codingchallenge.ui.adapters.PicturesAdapter
import com.esaurio.codingchallenge.utils.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_info_patient.*
import kotlinx.android.synthetic.main.dialog_picture_type.view.*

class PatientActivity : BaseActivity(), PicturesAdapter.Listener {

    private var patient : Patient? = null
    private val adapterPictures : PicturesAdapter by lazy {
        PicturesAdapter(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_info_patient)
        setSupportActionBar(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(pat_scrollView){ v, windowInsetsCompat ->
            v.setPadding(0,0,0, windowInsetsCompat.systemWindowInsetBottom)
            windowInsetsCompat
        }
        pat_btEdit.setOnClickListener {
            patient?.let { patient ->
                val intent = Intent(this, PatientFormActivity::class.java)
                intent.putExtra("patient", Gson().toJson(patient))
                startActivityForResult(intent, RC_EDIT_PATIENT)
            }
        }
        pat_btDelete.setOnClickListener {
            patient?.let { patient ->
                AlertFactory.showQuestion(this, getString(R.string.confirm_delete_patient), DialogInterface.OnClickListener { _, _ ->
                    deletePatient(patient)
                })
            }
        }
        pat_btSend.setOnClickListener {
            patient?.let { patient ->
                trySendPatient(patient)
            }
        }
        pat_recyclerViewPictures.isNestedScrollingEnabled = false
        pat_recyclerViewPictures.layoutManager = GridLayoutManager(this, 2)
        pat_recyclerViewPictures.adapter = adapterPictures

        adapterPictures.listener = this

        loadPatient()
    }

    private fun trySendPatient(patient: Patient) {
        if (patient.picture1 == null || patient.picture2 == null){
            AlertFactory.showMessage(this, R.string.share_needs_pictures)
        }else{
            AlertFactory.showQuestion(this, getString(R.string.confirm_share_patient), DialogInterface.OnClickListener { _, _ ->
                sendPatient(patient)
            })
        }
    }

    private fun deletePatient(patient: Patient) {
        pat_scrollView.hide()
        pat_progressBar.show()
        CodingChallengeAPI.SHARED_INSTANCE.deletePatient(patient.id, object : CodingChallengeAPI.DataListener<Boolean> {
            override fun onResponse(data: Boolean) {
                finish()
            }

            override fun onError(code: Int, message: String?) {
                AlertFactory.showMessage(this@PatientActivity, R.string.error_conexion).setOnDismissListener { finish() }
            }
        })
    }

    private fun sendPatient(patient: Patient){
        pat_txProgress.setText(R.string.enviando_ficha)
        pat_layFullScreenProgress.show()
        CodingChallengeAPI.SHARED_INSTANCE.sharePatient(patient.id, object : CodingChallengeAPI.DataListener<ApiResult>{
            override fun onResponse(data: ApiResult) {
                pat_layFullScreenProgress.hide()
                if (data.isResultOK){
                    val msg = data.resultMessage
                    if (!msg.isNullOrEmpty()){
                        AlertFactory.showMessage(this@PatientActivity, msg)
                    }
                    loadPatient()
                }else{
                    AlertFactory.showMessage(this@PatientActivity, data.resultMessage ?: getString(R.string.error_proceso))
                }
            }

            override fun onError(code: Int, message: String?) {
                pat_layFullScreenProgress.hide()
                AlertFactory.showMessage(this@PatientActivity, R.string.error_conexion)
            }
        })
    }

    private fun loadPatient() {
        pat_scrollView.hide()
        pat_progressBar.show()

        CodingChallengeAPI.SHARED_INSTANCE.getPatient(intent.getIntExtra("patientId",0),object : CodingChallengeAPI.DataListener<Patient> {
            override fun onResponse(data: Patient) {
                this@PatientActivity.patient = data
                bind(data)
            }

            override fun onError(code: Int, message: String?) {
                AlertFactory.showMessage(this@PatientActivity, R.string.error_conexion).setOnDismissListener { finish() }
            }
        })
    }

    private fun bind(patient : Patient){
        pat_progressBar.hide()
        pat_scrollView.show()
        pat_txName.text = String.format("%s %s", patient.lastName, patient.name)
        pat_txBirthdate.text = getString(R.string.birthdate_x, patient.birthdate?.toString("dd/MM/yyyy") ?: "")
        pat_txGender.text = getString(R.string.gender_x,
                getString(if (patient.gender == "M") R.string.gender_masculine else R.string.gender_femenine)
            )
        pat_txCreatedAt.text = getString(R.string.date_creation_x, patient.createdAt?.toString("dd/MM/yyyy") ?: "-")
        pat_txLastEdition.text = getString(R.string.last_edition_x, patient.lastEdition?.toString("dd/MM/yyyy") ?: "-")
        pat_txLastSent.text = getString(R.string.last_sent_x, patient.lastSubmissionDate?.toString("dd/MM/yyyy HH:mm") ?: "-")
        pat_txStatus.text = getString(R.string.status_x, patient.status)

        val lastPicDate = patient.lastPictureDate
        if (lastPicDate != null)
            pat_txLastPicDate.text = getString(R.string.last_picture_date_x, lastPicDate.toString("dd/MM/yyyy HH:mm"))
        pat_txLastPicDate.isVisible = lastPicDate != null
        pat_txNotes.text = getString(R.string.notes_x, patient.notes ?: "")
        pat_txNotes.isVisible = !patient.notes.isNullOrEmpty()
        val items = mutableListOf(
            PatientPicture(PatientPicture.Type.Type1, patient.picture1),
            PatientPicture(PatientPicture.Type.Type2, patient.picture2),
            PatientPicture(PatientPicture.Type.Type3, patient.picture3),
            PatientPicture(PatientPicture.Type.Type4, patient.picture4),
            PatientPicture(PatientPicture.Type.Type5, patient.picture5)
        )
        for (additionalPicture in patient.additionalPictures){
            val pictureType : PatientPicture.Type? = when(additionalPicture.pictureNumber){
                1 -> PatientPicture.Type.Type1
                2 -> PatientPicture.Type.Type2
                3 -> PatientPicture.Type.Type3
                4 -> PatientPicture.Type.Type4
                5 -> PatientPicture.Type.Type5
                else -> null
            }
            items.add(PatientPicture(pictureType, additionalPicture.picturePath, additionalPicture.name, additionalPicture.id))
        }
        adapterPictures.items = items
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_EDIT_PATIENT || requestCode == RC_TAKE_PICTURE) {
                loadPatient()
            }else if (requestCode == RC_VIEW_PICTURE){
                val type = data?.getSerializableExtra("retakePicture") as? PatientPicture.Type
                val pictureId = if (data?.hasExtra("retakePictureId") == true) data.getIntExtra("retakePictureId",0) else null

                takePicture(pictureId, type, pictureId != null)
            }
        }
    }

    companion object {
        const val RC_EDIT_PATIENT = 1
        const val RC_TAKE_PICTURE = 2
        const val RC_VIEW_PICTURE = 3

        fun start(ctx: Activity, patientId : Int, requestCode: Int? = null){
            val intent = Intent(ctx, PatientActivity::class.java)
            intent.putExtra("patientId", patientId)
            if (requestCode!=null)
                ctx.startActivityForResult(intent, requestCode)
            else
                ctx.startActivity(intent)
        }
    }

    override fun takeNewPicture() {
        val view = layoutInflater.inflate(R.layout.dialog_picture_type, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.seleccionar_plantilla)
            .setView(view)
            .setNegativeButton(R.string.cancelar, null)
            .create()

        view.dpt_btPicture1.setOnClickListener { dialog.dismiss(); takePicture(null, PatientPicture.Type.Type1, true) }
        view.dpt_btPicture2.setOnClickListener { dialog.dismiss(); takePicture(null, PatientPicture.Type.Type2, true) }
        view.dpt_btPicture3.setOnClickListener { dialog.dismiss(); takePicture(null, PatientPicture.Type.Type3, true) }
        view.dpt_btPicture4.setOnClickListener { dialog.dismiss(); takePicture(null, PatientPicture.Type.Type4, true) }
        view.dpt_btPicture5.setOnClickListener { dialog.dismiss(); takePicture(null, PatientPicture.Type.Type5, true) }
        view.dpt_btPictureNoType.setOnClickListener { dialog.dismiss(); takePicture(null, null, true) }

        dialog.show()
    }

    override fun takePicture(item: PatientPicture) {
        takePicture(item.id, item.type, item.id != null)
    }

    private fun takePicture(id : Int?, type : PatientPicture.Type?, isAdditional : Boolean){
        val intent = if (type != null) {
            val i = Intent(this, CameraOnboardingActivity::class.java)
            i.putExtra(CameraActivity.PARAM_PICTURE_TYPE,type)
            i
        }else
            Intent(this, CameraActivity::class.java)

        if (id!=null)
            intent.putExtra(CameraActivity.PARAM_PICTURE_ID, id)
        intent.putExtra(CameraActivity.PARAM_PATIENT_ID, patient?.id ?: 0)
        intent.putExtra(CameraActivity.PARAM_IS_ADDITIONAL, isAdditional)
        startActivityForResult(intent, RC_TAKE_PICTURE)
    }

    override fun viewPicture(item: PatientPicture) {
        val intent = Intent(this, ViewPictureActivity::class.java)
        intent.putExtra(ViewPictureActivity.PARAM_PICTURE, Gson().toJson(item))
        startActivityForResult(intent, RC_VIEW_PICTURE)
    }

    override fun deletePicture(item: PatientPicture) {
        val patientId = patient?.id ?: 0
        AlertFactory.showQuestion(this,getString(R.string.confirm_delete_picture), DialogInterface.OnClickListener { _, _ ->
            pat_txProgress.setText(R.string.deleting_picture)
            pat_layFullScreenProgress.show()
            CodingChallengeAPI.SHARED_INSTANCE.deletePatientPicture(patientId, item.type, item.id, object : CodingChallengeAPI.DataListener<ApiResult>{
                override fun onResponse(data: ApiResult) {
                    pat_layFullScreenProgress.hide()
                    if (data.isResultOK){
                        patient?.let { patient ->
                            if (item.id!=null){
                                patient.additionalPictures.removeAll { it.id == item.id }
                            }else{
                                when(item.type){
                                    PatientPicture.Type.Type1 -> patient.picture1 = null
                                    PatientPicture.Type.Type2 -> patient.picture2 = null
                                    PatientPicture.Type.Type3 -> patient.picture3 = null
                                    PatientPicture.Type.Type4 -> patient.picture4 = null
                                    PatientPicture.Type.Type5 -> patient.picture5 = null
                                }
                            }
                            bind(patient)
                        }
                        val msg = data.resultMessage
                        if (!msg.isNullOrEmpty()){
                            AlertFactory.showMessage(this@PatientActivity, msg)
                        }
                    }else{
                        AlertFactory.showMessage(this@PatientActivity, data.resultMessage ?: getString(R.string.error_proceso))
                    }
                }

                override fun onError(code: Int, message: String?) {
                    AlertFactory.showMessage(this@PatientActivity, R.string.error_conexion).setOnDismissListener { finish() }
                }
            })
        })
    }
}