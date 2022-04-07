package com.esaurio.codingchallenge.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.esaurio.codingchallenge.R
import com.esaurio.codingchallenge.data.api.ImagesManager
import com.esaurio.codingchallenge.data.model.PatientPicture
import com.esaurio.codingchallenge.utils.Utils
import com.esaurio.codingchallenge.utils.hide
import com.esaurio.codingchallenge.utils.show
import kotlinx.android.synthetic.main.row_picture.view.*

class PicturesAdapter(
        private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<PicturesAdapter.PictureHolder>() {
    var items : List<PatientPicture> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var listener : Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        return PictureHolder(layoutInflater.inflate(R.layout.row_picture, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        if (position < items.size)
            holder.bind(items[position])
        else
            holder.bindItemCreation()
    }

    inner class PictureHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val imCamera = itemView.rpic_imCamera
        private val imPicture = itemView.rpic_imPicture
        private val txName = itemView.rpic_txName
        private val layAction = itemView.rpic_layActions
        private val imCreate = itemView.rpic_imCreate

        init {
            itemView.rpic_btItem.setOnClickListener {
                val item = items.getOrNull(adapterPosition)
                if (item != null) {
                    if (item.filePath == null)
                        listener?.takePicture(item)
                    else
                        listener?.viewPicture(item)
                }else{
                    listener?.takeNewPicture()
                }
            }
            itemView.rpic_btView.setOnClickListener {
                items.getOrNull(adapterPosition)?.let{
                    listener?.deletePicture(it)
                }
            }
            itemView.rpic_btRetakePicture.setOnClickListener {
                items.getOrNull(adapterPosition)?.let{
                    listener?.takePicture(it)
                }
            }
        }

        fun bind(picture : PatientPicture){
            imCreate.hide()
            if (picture.name!=null || picture.type == null){
                txName.text = picture.name
            }else{
                txName.setText(
                    when(picture.type){
                        PatientPicture.Type.Type1 -> R.string.picture_name_1
                        PatientPicture.Type.Type2 -> R.string.picture_name_2
                        PatientPicture.Type.Type3 -> R.string.picture_name_3
                        PatientPicture.Type.Type4 -> R.string.picture_name_4
                        PatientPicture.Type.Type5 -> R.string.picture_name_5
                    }
                )
            }
            val picturePath = picture.filePath
            when {
                picturePath != null -> {
                    imPicture.scaleType = ImageView.ScaleType.CENTER_CROP
                    imPicture.setPadding(0)
                    ImagesManager.sharedInstance.showImage(picturePath, imPicture, R.color.grey4)
                }
                picture.type != null -> {
                    imPicture.scaleType = ImageView.ScaleType.FIT_CENTER
                    imPicture.setPadding(Utils.dpToPx(12))
                    imPicture.setImageResource(
                        when(picture.type){
                            PatientPicture.Type.Type1 -> R.drawable.thumb_picture_1
                            PatientPicture.Type.Type2 -> R.drawable.thumb_picture_2
                            PatientPicture.Type.Type3 -> R.drawable.thumb_picture_3
                            PatientPicture.Type.Type4 -> R.drawable.thumb_picture_4
                            PatientPicture.Type.Type5 -> R.drawable.thumb_picture_5
                        }
                    )
                }
                else -> {
                    imPicture.setImageDrawable(null)
                }
            }
            imCamera.isVisible = picturePath == null
            layAction.isVisible = picturePath != null
        }

        fun bindItemCreation(){
            imCreate.show()
            imPicture.setImageDrawable(null)
            txName.text = null
            imCamera.hide()
            layAction.hide()
        }
    }

    interface Listener {
        fun takeNewPicture()
        fun takePicture(item : PatientPicture)
        fun viewPicture(item : PatientPicture)
        fun deletePicture(item : PatientPicture)
    }
}