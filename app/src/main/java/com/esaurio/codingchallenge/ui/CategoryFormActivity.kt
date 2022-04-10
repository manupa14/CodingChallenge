package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.ViewCompat
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.Category
import com.esaurio.codingchallenge.data.model.SaveCategoryResultTO
import com.esaurio.codingchallenge.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_form_category.*
import kotlinx.android.synthetic.main.activity_info_category.*
import java.io.*
import java.util.*
import java.util.jar.Manifest

class CategoryFormActivity : BaseActivity() {

    private var category: Category? = null

    val REQUEST_IMAGE_GET = 1
    private var file: File? = null
    lateinit var imagen: ImageView

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                selectFile()
            } else {
                Toast.makeText(applicationContext, "Access to files denied", Toast.LENGTH_LONG)
            }
        }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialize()
    }

    private fun initialize() {
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_form_category)
        setSupportActionBar(formToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        ViewCompat.setOnApplyWindowInsetsListener(fpat_scrollView) { v, windowInsetCompat ->
            v.setPadding(0, 0, 0, windowInsetCompat.systemWindowInsetBottom)
            windowInsetCompat
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imagen = findViewById(R.id.imagen)

        if (intent.hasExtra("category"))
            category = Gson().fromJson(intent.getStringExtra("category"), Category::class.java)

        supportActionBar?.title = if (category != null)
            getString(R.string.edit_category)
        else
            getString(R.string.new_category)

        category?.let { loadForm(it) }

        configureForm()

        formRequestFocus.requestFocus()
    }

    private fun checkExternalStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                selectFile()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                //Snackbar.make(, "Se necesita el permiso de acceso para asociar imagenes a las categorias", Snackbar.LENGTH_INDEFINITE)
                formRequestFocus.showSnackbar(
                    formRequestFocus,
                    "Permiso necesario para agregar imagenes",
                    Snackbar.LENGTH_INDEFINITE,
                    "Ok"
                ) {
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun loadForm(category: Category) {
        fpat_edName.setText(category.name)
        if(!category.image.isNullOrEmpty()) {
            val imageFile = File(this.applicationContext.getExternalFilesDir(null), category.image)
            ImagesManager.sharedInstance.showImage(imageFile, imagen)
        }
    }

    private fun configureForm() {
        fpat_progressBar.hide()

        fpat_btImage.setOnClickListener(View.OnClickListener {
            checkExternalStoragePermission()
        })

        fpat_btSave.setOnClickListener { save() }
    }

    private fun selectFile() {
        val pickPhoto = Intent(Intent.ACTION_GET_CONTENT)
        pickPhoto.type = "image/*"
        pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        pickPhoto.addCategory(Intent.CATEGORY_OPENABLE)
        pickPhoto.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data?.data
            this.file = null
            if (fullPhotoUri != null) {
                val filePath = FilePath.getPath(this, fullPhotoUri)
                if (filePath != null) {
                    val file = File(filePath)
                    this.file = file
                    ImagesManager.sharedInstance.showImage(file, imagen)
                }
            }
        }
    }

    private fun save() {
        val name = fpat_edName.text.toString()

        fpat_edName.setBackgroundResource(
            if (name.isEmpty()) R.drawable.edit_text_error else R.drawable.edit_text_background
        )
        formRequestFocus.requestFocus()

        if (name.isNotEmpty()) {
            fpat_btSave.hide()
            fpat_progressBar.show()
            fpat_edName.isEnabled = false

            val file = this.file
            val listener = object : CodingChallengeAPI.DataListener<SaveCategoryResultTO> {
                override fun onResponse(data: SaveCategoryResultTO) {
                    if (data.isResultOK) {
                        val intent = Intent()
                        data.id?.let { intent.putExtra("id", it) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        enableForm()
                        AlertFactory.showMessage(
                            this@CategoryFormActivity,
                            data.resultMessage ?: getString(R.string.error_proceso)
                        )
                    }
                }

                override fun onError(code: Int, message: String?) {
                    enableForm()
                    AlertFactory.showMessage(this@CategoryFormActivity, R.string.error_conexion)
                }

                private fun enableForm() {
                    fpat_btSave.show()
                    fpat_progressBar.hide()
                    fpat_edName.isEnabled = true
                }
            }
            if (file != null) {
                CodingChallengeAPI.SHARED_INSTANCE.uploadFile(category?.id, name, file, listener)
            } else {
                CodingChallengeAPI.SHARED_INSTANCE.saveCategory(
                    category?.id,
                    name,
                    category?.image,
                    listener
                )
            }
        }
    }
}