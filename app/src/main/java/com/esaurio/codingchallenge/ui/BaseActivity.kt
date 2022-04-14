package com.esaurio.codingchallenge.ui

import android.content.*
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.utils.AlertFactory
import com.esaurio.codingchallenge.utils.Utils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(resetAppBroadcastReceiver, IntentFilter(Utils.ACTION_RESET_APP))
    }

    protected fun configureDrawer() {
        var drawerLayout = findViewById<DrawerLayout>(R.id.mainDrawerLayout)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.navAbrir, R.string.navCerrar)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        var navigationView = this.findViewById<NavigationView>(R.id.mainNavigationView)

        navigationView.setNavigationItemSelectedListener { it: MenuItem ->
            when (it.itemId) {
                R.id.mainNavigationCategories -> openCategories()
                R.id.mainNavigationLogout-> logout()
                else -> {
                    true
                }
            }
        }

        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun openCategories(): Boolean {
        startActivity(Intent(this,CategoriesActivity::class.java))

        return true
    }

    protected fun logout(): Boolean {
        AlertFactory.showQuestion(this, getString(R.string.confirm_logout), DialogInterface.OnClickListener { _, _ ->
            Prefs.sharedInstance.logout()
            resetApp()
        })

        return true
    }

    override fun onDestroy() {
        unregisterReceiver(resetAppBroadcastReceiver)
        super.onDestroy()
    }

    private val resetAppBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            resetApp()
        }
    }

    fun resetApp(){
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        } else if (item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun View.showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }
}