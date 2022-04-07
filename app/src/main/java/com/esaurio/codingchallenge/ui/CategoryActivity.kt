package com.esaurio.codingchallenge.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.ViewCompat
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.Category
import com.esaurio.codingchallenge.utils.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_form_category.*
import kotlinx.android.synthetic.main.activity_info_category.*
import kotlinx.android.synthetic.main.activity_info_category.toolbar
import java.io.File

class CategoryActivity : BaseActivity() {

    private var category : Category? = null
    lateinit var imagen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_info_category)
        setSupportActionBar(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imagen = imageInfo

        ViewCompat.setOnApplyWindowInsetsListener(pat_scrollView){ v, windowInsetsCompat ->
            v.setPadding(0,0,0, windowInsetsCompat.systemWindowInsetBottom)
            windowInsetsCompat
        }
        pat_btEdit.setOnClickListener {
            category?.let { category ->
                val intent = Intent(this, CategoryFormActivity::class.java)
                intent.putExtra("category", Gson().toJson(category))
                startActivityForResult(intent, RC_EDIT_CATEGORY)
            }
        }
        pat_btDelete.setOnClickListener {
            category?.let { patient ->
                AlertFactory.showQuestion(this, getString(R.string.confirm_delete_category), DialogInterface.OnClickListener { _, _ ->
                    deleteCategory(patient)
                })
            }
        }
        loadCategory()
    }

     private fun deleteCategory(category: Category) {
        pat_scrollView.hide()
        pat_progressBar.show()
        CodingChallengeAPI.SHARED_INSTANCE.deleteCategory(category.id, object : CodingChallengeAPI.DataListener<Boolean> {
            override fun onResponse(data: Boolean) {
                finish()
            }

            override fun onError(code: Int, message: String?) {
                AlertFactory.showMessage(this@CategoryActivity, R.string.error_conexion).setOnDismissListener { finish() }
            }
        })
    }

    private fun loadCategory() {
        pat_scrollView.hide()
        pat_progressBar.show()

        CodingChallengeAPI.SHARED_INSTANCE.getCategory(intent.getIntExtra("categoryId",0),object : CodingChallengeAPI.DataListener<Category> {
            override fun onResponse(data: Category) {
                this@CategoryActivity.category = data

                bind(data)
            }

            override fun onError(code: Int, message: String?) {
                AlertFactory.showMessage(this@CategoryActivity, R.string.error_conexion).setOnDismissListener { finish() }
            }
        })
    }

    private fun bind(category : Category){
        pat_progressBar.hide()
        pat_scrollView.show()
        pat_txName.text = String.format("%s", category.name)
        ImagesManager.sharedInstance.showImage(category.image.toString(), imagen)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_EDIT_CATEGORY) {
                loadCategory()
            }
        }
    }

    companion object {
        const val RC_EDIT_CATEGORY = 1

        fun start(ctx: Activity, categoryId : Int, requestCode: Int? = null){
            val intent = Intent(ctx, CategoryActivity::class.java)
            intent.putExtra("categoryId", categoryId)
            if (requestCode!=null)
                ctx.startActivityForResult(intent, requestCode)
            else
                ctx.startActivity(intent)
        }
    }
}