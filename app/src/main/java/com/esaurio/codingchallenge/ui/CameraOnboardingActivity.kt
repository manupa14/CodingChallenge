package com.esaurio.codingchallenge.ui

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.animation.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.model.PatientPicture
import com.esaurio.codingchallenge.utils.CheckmarBulletSpan
import com.esaurio.codingchallenge.utils.Utils
import com.esaurio.codingchallenge.utils.hide
import com.esaurio.codingchallenge.utils.show
import kotlinx.android.synthetic.main.activity_camera_onboarding.*

class CameraOnboardingActivity : BaseActivity() {

    private val pictureType : PatientPicture.Type by lazy {
        intent.getSerializableExtra(CameraActivity.PARAM_PICTURE_TYPE) as PatientPicture.Type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_camera_onboarding)
        val padding = object {
            val top = mainView.paddingTop
            val bottom = mainView.paddingBottom
            val left = mainView.paddingLeft
            val right = mainView.paddingRight
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(
                    padding.left,
                    padding.top + windowInsetsCompat.systemWindowInsetTop,
                    padding.right,
                    padding.bottom + windowInsetsCompat.systemWindowInsetBottom
            )
            windowInsetsCompat
        }

        onb_mainCard.startAnimation(AnimationUtils.loadAnimation(this,R.anim.in_from_bottom))

        onb_btSkip.setOnClickListener { goToCamera() }
        onb_btPrevious.setOnClickListener { showGeneralOnboarding() }
        onb_btNext.setOnClickListener {
            if (onb_btSkip.isVisible){
                showSpecificOnboarding()
            }else{
                goToCamera()
            }
        }

        if (pictureType != PatientPicture.Type.Type1 || intent.getBooleanExtra(CameraActivity.PARAM_IS_ADDITIONAL,false)){
            onb_btSkip.hide()
            onb_btNext.setText(R.string.continuar)
            loadSpecificOnboardingData()
        }else{
            onb_txInfo.text = formatText(R.array.onboarding_0)
            onb_imTop.setImageResource(R.drawable.onboarding_0)
        }
    }

    private fun showGeneralOnboarding(){
        onb_btPrevious.hide()
        onb_btSkip.show()
        onb_btNext.setText(R.string.next)
        onb_txInfo.startAnimation(AlphaAnimation(1f,0f).apply { duration = 100 })
        onb_imTop.startAnimation(AlphaAnimation(1f,0f).apply { duration = 100 })
        onb_imTop.postDelayed({
            onb_txInfo.text = formatText(R.array.onboarding_0)
            onb_imTop.setImageResource(R.drawable.onboarding_0)
            onb_txInfo.startAnimation(getInFromLeftAnimation())
            onb_imTop.startAnimation(getInFromLeftAnimation())
        },100)
    }

    private fun showSpecificOnboarding(){
        onb_btPrevious.show()
        onb_btSkip.hide()
        onb_btNext.setText(R.string.continuar)

        onb_txInfo.startAnimation(AlphaAnimation(1f,0f).apply { duration = 100 })
        onb_imTop.startAnimation(AlphaAnimation(1f,0f).apply { duration = 100 })
        onb_imTop.postDelayed({
            loadSpecificOnboardingData()
            onb_txInfo.startAnimation(getInFromRightAnimation())
            onb_imTop.startAnimation(getInFromRightAnimation())
        },100)
    }

    private fun loadSpecificOnboardingData() {
        when (pictureType) {
            PatientPicture.Type.Type1 -> {
                onb_txInfo.text = formatText(R.array.onboarding_1)
                onb_imTop.setImageResource(R.drawable.onboarding_1)
            }
            PatientPicture.Type.Type2 -> {
                onb_txInfo.text = formatText(R.array.onboarding_2)
                onb_imTop.setImageResource(R.drawable.onboarding_2)
            }
            PatientPicture.Type.Type3 -> {
                onb_txInfo.text = formatText(R.array.onboarding_3)
                onb_imTop.setImageResource(R.drawable.onboarding_3)
            }
            PatientPicture.Type.Type4 -> {
                onb_txInfo.text = formatText(R.array.onboarding_4_5)
                onb_imTop.setImageResource(R.drawable.onboarding_4)
            }
            PatientPicture.Type.Type5 -> {
                onb_txInfo.text = formatText(R.array.onboarding_4_5)
                onb_imTop.setImageResource(R.drawable.onboarding_5)
            }
        }
    }

    private fun formatText(resId : Int) : Spanned {
        val lines = resources.getStringArray(resId)
        val textSpan = SpannableStringBuilder()
        val color = ContextCompat.getColor(this, R.color.primary_dark)
        for (line in lines){
            if (textSpan.isNotEmpty())
                textSpan.append("\n\n")
            textSpan.append(line, CheckmarBulletSpan(Utils.dpToPx(20), color),0)
        }
        return textSpan
    }

    private fun getInFromLeftAnimation() : Animation {
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(AlphaAnimation(0f,1f).apply { duration = 200 })
        animationSet.setInterpolator(this,android.R.interpolator.decelerate_quad)
        animationSet.addAnimation(TranslateAnimation(
                Animation.RELATIVE_TO_SELF,-10f,Animation.RELATIVE_TO_SELF,0f,
                Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f).apply {
            duration = 400
        })
        animationSet.fillAfter = true
        return animationSet
    }

    private fun getInFromRightAnimation() : Animation {
        val animationSet = AnimationSet(true)
        animationSet.setInterpolator(this,android.R.interpolator.decelerate_quad)
        animationSet.addAnimation(AlphaAnimation(0f,1f).apply { duration = 200 })
        animationSet.addAnimation(TranslateAnimation(
                Animation.RELATIVE_TO_SELF,10f,Animation.RELATIVE_TO_SELF,0f,
                Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f).apply {
            duration = 400
        })
        return animationSet
    }

    private fun goToCamera(){
        onb_mainCard.startAnimation(AlphaAnimation(1f,0f).apply { duration = 200 })
        val cameraIntent = Intent(this, CameraActivity::class.java)
        intent.extras?.let {
            cameraIntent.putExtras(it)
        }
        startActivityForResult(cameraIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setResult(resultCode, data)
        finish()
    }
}