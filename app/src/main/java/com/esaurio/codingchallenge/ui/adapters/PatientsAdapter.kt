package com.esaurio.codingchallenge.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esaurio.codingchallenge.MyApplication
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.Patient
import com.esaurio.codingchallenge.utils.toString
import kotlinx.android.synthetic.main.row_patient.view.*

class PatientsAdapter(
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items : MutableList<Patient> = mutableListOf()
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
            ItemHolder(layoutInflater.inflate(R.layout.row_patient,parent, false))
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
        private val txBirthdate = itemView.rpat_txBirthdate
        private val txPicCount = itemView.rpat_txPictureCount
        private val imMainPicture = itemView.rpat_imMainPicture
        private val swipeLayout = itemView.rpat_swipeLayout
        private val txStatus = itemView.rpat_txStatus

        init {
            itemView.rpat_layMain.setOnClickListener {
                items.getOrNull(adapterPosition)?.let { patient ->
                    swipeLayout.close()
                    listener?.onItemSelected(patient)
                }
            }
            itemView.rpat_btDelete.setOnClickListener {
                swipeLayout.close()
                items.getOrNull(adapterPosition)?.let { patient ->
                    listener?.onDeleteItem(patient)
                }
            }
        }

        fun bind(item : Patient){
            val picturePath = item.picture1
            if (picturePath != null){
                ImagesManager.sharedInstance.showImage(picturePath, imMainPicture, R.color.grey4)
            }else{
                imMainPicture.setImageResource(R.drawable.ic_person_circle)
            }
            txName.text = String.format("%s %s",item.lastName, item.name)
            val birthdate = item.birthdate?.toString("dd/MM/yyyy") ?: ""
            txBirthdate.text = MyApplication.instance.getString(R.string.birthdate_x, birthdate)
            txPicCount.text = MyApplication.instance.getString(R.string.pictures_count_x, item.picturesCount)
            txStatus.text = MyApplication.instance.getString(R.string.status_x, item.status)
        }

    }

    interface Listener {
        fun onItemSelected(item : Patient)
        fun onDeleteItem(item: Patient)
    }
}