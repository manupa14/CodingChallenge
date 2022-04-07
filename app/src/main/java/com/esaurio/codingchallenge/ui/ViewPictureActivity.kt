package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.PatientPicture
import com.esaurio.codingchallenge.utils.AlertFactory
import com.esaurio.codingchallenge.utils.Utils
import com.esaurio.codingchallenge.utils.hide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_view_picture.*

class ViewPictureActivity : AppCompatActivity(){
    companion object {
        const val PARAM_PICTURE = "picture"
    }

    private val picture : PatientPicture by lazy {
        Gson().fromJson(intent.getStringExtra(PARAM_PICTURE), PatientPicture::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            window.statusBarColor = Color.BLACK
        setContentView(R.layout.activity_view_picture)

        picture_btRetakePicture.setOnClickListener {
            val intent = Intent()
            if (picture.id != null){
                intent.putExtra("retakePictureId", picture.id as Int)
            }
            if (picture.type != null)
                intent.putExtra("retakePicture",picture.type)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        loadImage()
    }

    private fun loadImage(){
        picture_btRetakePicture.hide()
        val picturePath = picture.filePath
        if (picturePath!=null){
            ImagesManager.sharedInstance.showImage(picturePath, picture_view,completionHandler = { success ->
                if (success){
                    picture_btRetakePicture.show()
                    picture_progressBar.hide()
                }else{
                    AlertFactory.showMessage(this, R.string.error_conexion).setOnDismissListener { finish() }
                }
            })
        }else{
            AlertFactory.showMessage(this, R.string.foto_no_disponible).setOnDismissListener { finish() }
        }
    }

}