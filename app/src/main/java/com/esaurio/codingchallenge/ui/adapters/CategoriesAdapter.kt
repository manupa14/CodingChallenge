package com.esaurio.codingchallenge.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.Category
import kotlinx.android.synthetic.main.row_category.view.*

class CategoriesAdapter(
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items : MutableList<Category> = mutableListOf()
    var loading : Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener : Listener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return if (position < items.size) items[position].id.toLong() else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0)
            ItemHolder(layoutInflater.inflate(R.layout.row_category,parent, false))
        else
            LoadingHolder(layoutInflater.inflate(R.layout.row_loading, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder){
            holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int {
        return if (loading) items.size + 1 else items.size
    }

    class LoadingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ItemHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val txName = itemView.rpat_txName
        private val imMainPicture = itemView.rpat_imMainPicture
        private val swipeLayout = itemView.rpat_swipeLayout

        init {
            itemView.rpat_layMain.setOnClickListener {
                items.getOrNull(adapterPosition)?.let { category ->
                    swipeLayout.close()
                    listener?.onItemSelected(category)
                }
            }
            itemView.rpat_btDelete.setOnClickListener {
                swipeLayout.close()
                items.getOrNull(adapterPosition)?.let { category ->
                    listener?.onDeleteItem(category)
                }
            }
        }

        fun bind(item : Category){
            val picturePath = item.image
            if (picturePath != null){
                ImagesManager.sharedInstance.showImage(picturePath, imMainPicture, R.color.grey4)
            }else{
                imMainPicture.setImageResource(R.drawable.ic_person_circle)
            }
            txName.text = String.format("%s", item.name)
        }

    }

    interface Listener {
        fun onItemSelected(item : Category)
        fun onDeleteItem(item: Category)
    }
}