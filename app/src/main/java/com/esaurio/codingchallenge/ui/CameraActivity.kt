package com.esaurio.codingchallenge.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.view.TextureView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.ApiResult
import com.esaurio.codingchallenge.data.model.PatientPicture
import com.esaurio.codingchallenge.utils.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.dialog_edittext.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class CameraActivity : BaseActivity(), TextureView.SurfaceTextureListener {
    companion object {
        const val PARAM_PICTURE_TYPE = "pictureType"
        const val PARAM_PICTURE_ID = "pictureId"
        const val PARAM_PATIENT_ID = "patientId"
        const val PARAM_IS_ADDITIONAL = "isAdditional"
        const val RC_PERMISSION_SETTINGS = 1
    }

    private var camera: Camera? = null
    private val mTextureView: TextureView by lazy { TextureView(this) }
    private val pictureType: PatientPicture.Type? by lazy {
        if(intent.hasExtra(PARAM_PICTURE_TYPE))
            intent.getSerializableExtra(PARAM_PICTURE_TYPE) as PatientPicture.Type
        else
            null
    }
    private var currentFile : File? = null
    private val patientId : Int by lazy { intent.getIntExtra(PARAM_PATIENT_ID, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            window.statusBarColor = Color.BLACK
        setContentView(R.layout.activity_camera)
        setSupportActionBar(toolbar)
        title = getString(
            when(pictureType){
                PatientPicture.Type.Type1 -> R.string.picture_name_1
                PatientPicture.Type.Type2 -> R.string.picture_name_2
                PatientPicture.Type.Type3 -> R.string.picture_name_3
                PatientPicture.Type.Type4 -> R.string.picture_name_4
                PatientPicture.Type.Type5 -> R.string.picture_name_5
                null -> R.string.nueva_foto
            }
        )
        ViewCompat.setOnApplyWindowInsetsListener(appbarLayout) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val padding = object {
            val top = cam_layBottom.paddingTop
            val bottom = cam_layBottom.paddingBottom
            val left = cam_layBottom.paddingLeft
            val right = cam_layBottom.paddingRight
        }
        ViewCompat.setOnApplyWindowInsetsListener(cam_layBottom){ v, windowInsetsCompat ->
            v.setPadding(
                    padding.left,
                    padding.top,
                    padding.right,
                    padding.bottom + windowInsetsCompat.systemWindowInsetBottom
            )
            windowInsetsCompat
        }
        cam_btTakePicture.setOnClickListener {
            takePicture()
        }
        cam_btRetakePicture.setOnClickListener {
            currentFile?.delete()
            currentFile = null
            mTextureView.show()
            loadCamera(mTextureView.surfaceTexture)
            showTakePictureUI()
        }
        cam_btConfirmPicture.setOnClickListener {
            uploadPicture()
        }
        cam_progressBar.hide()
        cam_btTakePicture.isInvisible = true
        loadCameraSurface()
        showTakePictureUI()
    }

    private fun showTakePictureUI(){
        cam_btConfirmPicture.hide()
        cam_btRetakePicture.hide()

        val type = pictureType
        if (type != null){
            cam_imOverlay.setImageResource(
                when(type){
                    PatientPicture.Type.Type1 -> R.drawable.camera_overlay_1
                    PatientPicture.Type.Type2 -> R.drawable.camera_overlay_2
                    PatientPicture.Type.Type3 -> R.drawable.camera_overlay_3
                    PatientPicture.Type.Type4 -> R.drawable.camera_overlay_4
                    PatientPicture.Type.Type5 -> R.drawable.camera_overlay_5
                }
            )
        }else{
            cam_imOverlay.setImageDrawable(null)
        }
    }

    private fun loadCameraSurface(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            return
        }
        mTextureView.surfaceTextureListener = this

        cam_frameLayout.addView(mTextureView,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCameraSurface()
            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                    //el usuario marcó la opcion: "no volver a preguntar"
                    AlertDialog.Builder(this)
                            .setMessage(R.string.enable_permission_in_settings)
                            .setPositiveButton(R.string.habilitar) { _, _ ->
                                Utils.openAppsSettings(this, RC_PERMISSION_SETTINGS)
                            }
                            .setNegativeButton(R.string.cancelar) { _, _ ->
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }.show()
                }else{
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_PERMISSION_SETTINGS){
            loadCameraSurface()
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        loadCamera(surface)
    }

    private fun loadCamera(surface: SurfaceTexture? = null) {
        val mCamera = Camera.open()
        mCamera.setDisplayOrientation(90)
        camera = mCamera
        val previewSize: Camera.Size = mCamera.parameters.previewSize
        val params = cam_frameLayout.layoutParams as? ConstraintLayout.LayoutParams
        if (params != null){
            params.dimensionRatio = "w,${previewSize.height}:${previewSize.width}"
            cam_frameLayout.layoutParams = params
            cam_frameLayout.requestLayout()
        }
        try {
            mCamera.setPreviewTexture(surface)
        } catch (t: IOException) {
        }
        mCamera.startPreview()

        cam_btTakePicture.show()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // Ignored, the Camera does all the work for us
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        camera?.stopPreview()
        camera?.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Update your view here!
    }

    private fun takePicture(){
        cam_btTakePicture.isInvisible = true
        cam_progressBar.show()
        camera?.takePicture(null, null, { data, camera ->
            camera.stopPreview()
            camera.release()
            this.camera = null
            GlobalScope.launch(Dispatchers.Main){
                val result = processImage(data)
                val pictureFile = result.first
                cam_progressBar.hide()
                if (pictureFile!=null){
                    currentFile = pictureFile
                    mTextureView.isInvisible = true
                    Picasso.get().load(pictureFile).into(cam_imOverlay)
                    cam_btConfirmPicture.show()
                    cam_btRetakePicture.show()
                }else{
                    result.second?.let { errorMessage ->
                        Snackbar.make(cam_frameLayout,errorMessage,Snackbar.LENGTH_SHORT).show()
                    }
                    cam_btTakePicture.show()
                    loadCamera(mTextureView.surfaceTexture)
                }
            }
        })
    }

    private suspend fun processImage(data : ByteArray) : Pair<File?, String?> {
        return GlobalScope.async(Dispatchers.IO) {
            val pictureFile = File(cacheDir,"picture-${Date().toString("yyyyMMdd-HHmmss")}.jpg")

            try {
                var bitmap = BitmapFactory.decodeByteArray(data,0,data.size)
                if (bitmap != null){
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0,
                        bitmap.width, bitmap.height, matrix,
                        true
                    )
                    bitmap.recycle()
                    bitmap = rotatedBitmap

                    bitmap = ThumbnailUtils.extractThumbnail(bitmap,1152, 2048,ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
                    val fos = FileOutputStream(pictureFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG,90,fos)
                    fos.close()
                    return@async Pair<File?, String?>(pictureFile, null)
                }else{
                    return@async Pair<File?, String?>(null, getString(R.string.error_taking_picture_bitmap_creation))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return@async Pair<File?, String?>(null, getString(R.string.error_taking_picture_file_not_found))
            } catch (e: IOException) {
                e.printStackTrace()
                return@async Pair<File?, String?>(null, getString(R.string.error_taking_picture_io))
            }
        }.await()
    }

    private fun uploadPicture(pictureName : String? = null){
        val file = currentFile
        if (file!=null && file.exists()){
            if (intent.getBooleanExtra(PARAM_IS_ADDITIONAL, false) && pictureName.isNullOrEmpty()){
                requestPictureName()
            }else{
                cam_btRetakePicture.hide()
                cam_btConfirmPicture.hide()
                cam_progressBar.show()
                val pictureId : Int? = if (intent.hasExtra(PARAM_PICTURE_ID)) intent.getIntExtra(
                    PARAM_PICTURE_ID, 0) else null
                CodingChallengeAPI.SHARED_INSTANCE.uploadFile(patientId, pictureType, pictureId, pictureName, file, object : CodingChallengeAPI.DataListener<ApiResult>{
                    override fun onResponse(data: ApiResult) {
                        if (data.isResultOK){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }else{
                            Snackbar.make(cam_frameLayout,data.resultMessage ?: getString(R.string.error_proceso),Snackbar.LENGTH_SHORT).show()
                            cam_btRetakePicture.show()
                            cam_btConfirmPicture.show()
                            cam_progressBar.hide()
                        }
                    }

                    override fun onError(code: Int, message: String?) {
                        Snackbar.make(cam_frameLayout,R.string.error_conexion,Snackbar.LENGTH_SHORT).show()
                        cam_btRetakePicture.show()
                        cam_btConfirmPicture.show()
                        cam_progressBar.hide()
                    }
                })
            }
        }else{
            cam_btTakePicture.show()
            loadCamera(mTextureView.surfaceTexture)
            Snackbar.make(cam_frameLayout,R.string.picture_no_longer_available,Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun requestPictureName(){
        val view = layoutInflater.inflate(R.layout.dialog_edittext, null, false)
        AlertDialog.Builder(this)
            .setTitle(R.string.enter_picture_name)
            .setView(view)
            .setPositiveButton(R.string.continuar) { _, _ ->
                val name = view.dial_edText.text?.toString()
                uploadPicture(name)
            }
            .setNegativeButton(R.string.cancelar, null)
            .create()
            .show()
    }
}