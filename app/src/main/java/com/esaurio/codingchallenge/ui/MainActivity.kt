package com.esaurio.codingchallenge.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.Category
import com.esaurio.codingchallenge.ui.adapters.CategoriesAdapter
import com.esaurio.codingchallenge.utils.*
import kotlinx.android.synthetic.main.activity_info_category.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import java.io.File
import kotlin.math.roundToInt

class MainActivity : BaseActivity(), CategoriesAdapter.Listener {
    private var scrollListener : EndlessRecyclerViewScrollListener? = null
    private var lastLoadedPage = 0
    private var turn : Int = 0

    private val adapter : CategoriesAdapter by lazy {
        CategoriesAdapter(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.applyEdgeToEdgeConfig(window)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, windowInsetsCompat ->
            v.setPadding(0, windowInsetsCompat.systemWindowInsetTop, 0, 0)
            windowInsetsCompat
        }
        val padding = object {
            val top = pats_recyclerView.paddingTop
            val bottom = pats_recyclerView.paddingBottom
            val left = pats_recyclerView.paddingLeft
            val right = pats_recyclerView.paddingRight
        }
        ViewCompat.setOnApplyWindowInsetsListener(pats_recyclerView) { v, windowInsetsCompat ->
            v.setPadding(
                padding.left,
                padding.top,
                padding.right,
                padding.bottom + windowInsetsCompat.systemWindowInsetBottom
            )
            windowInsetsCompat
        }
        ViewCompat.setOnApplyWindowInsetsListener(pats_btAdd) { v, windowInsetsCompat ->
            val layoutParams = v.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin,
                layoutParams.rightMargin,
                (windowInsetsCompat.systemWindowInsetBottom + resources.getDimension(R.dimen.vertical_margin)).roundToInt()
            )
            v.layoutParams = layoutParams
            windowInsetsCompat
        }
        pats_recyclerView.adapter = adapter
        pats_refreshLayout.setOnRefreshListener {
            loadData()
        }
        pats_edSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                Utils.hideSoftKeyboard(v)
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        pats_edSearch.addTextChangedListener {
            val search = it.toString()
            pats_edSearch.postDelayed({
                if (pats_edSearch.text.toString() == search){
                    loadData()
                }
            },600)
        }
        val layoutManager = LinearLayoutManager(this)
        pats_recyclerView.layoutManager = layoutManager
        val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if (!adapter.loading) {
                    pats_recyclerView.post {
                        loadData(lastLoadedPage + 1)
                    }
                }
            }
        }
        pats_recyclerView.addOnScrollListener(scrollListener)
        this.scrollListener = scrollListener

        pats_btAdd.setOnClickListener {
            startActivityForResult(Intent(this, CategoryFormActivity::class.java), RC_NEW_CATEGORY)
        }
        adapter.listener = this

        pats_layNoData.hide()
        loadData()
    }

    private fun loadData(page : Int = 0){
        turn ++
        val myTurn = turn
        if (page == 0){
            adapter.items.clear()
        }
        adapter.loading = true
        pats_refreshLayout.isRefreshing = false

        CodingChallengeAPI.SHARED_INSTANCE.getCategories(pats_edSearch.text.toString(),page,object : CodingChallengeAPI.DataListener<List<Category>>{
            override fun onResponse(data: List<Category>) {
                if (turn == myTurn){
                    lastLoadedPage = page
                    adapter.items.addAll(data)
                    adapter.loading = false
                    val noData = data.isEmpty() && adapter.items.isEmpty()
                    pats_layNoData.isVisible = noData
                    pats_recyclerView.isVisible = !noData
                }
            }

            override fun onError(code: Int, message: String?) {
                if (turn == myTurn)
                    adapter.loading = false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout){
            AlertFactory.showQuestion(this, getString(R.string.confirm_logout), DialogInterface.OnClickListener { _, _ ->
                Prefs.sharedInstance.logout()
                resetApp()
            })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RC_VIEW_CATEGORY -> loadData()
            RC_NEW_CATEGORY -> {
                loadData()
                val id = data?.getIntExtra("id",0)
                if (id != null && id != 0){
                    CategoryActivity.start(this, id, RC_VIEW_CATEGORY)
                }
            }
        }
    }

    companion object {
        private const val RC_NEW_CATEGORY = 1
        private const val RC_VIEW_CATEGORY = 2
    }

    override fun onItemSelected(item: Category) {
        CategoryActivity.start(this, item.id, RC_VIEW_CATEGORY)
    }

    override fun onDeleteItem(item: Category) {
        AlertFactory.showQuestion(this, getString(R.string.confirm_delete_category), DialogInterface.OnClickListener { _, _ ->
            CodingChallengeAPI.SHARED_INSTANCE.deleteCategory(item.id)
            adapter.items.remove(item)
            adapter.notifyDataSetChanged()
            val noData = adapter.items.isEmpty()
            pats_layNoData.isVisible = noData
            pats_recyclerView.isVisible = !noData
        })
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
    }
}