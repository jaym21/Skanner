package dev.jaym21.skanner.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.skanner.R


class ImagesRVAdapter(private var images: List<Bitmap>, private val listener: IImagesRVAdapter): RecyclerView.Adapter<ImagesRVAdapter.ImagesViewHolder>() {

    inner class ImagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivDocumentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val viewHolder =  ImagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_image_item_layout, parent, false))
        viewHolder.image.setOnClickListener {
            listener.onImageClick(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val currentItem = images[position]
        Glide.with(holder.itemView.context).load(currentItem).transform(CenterCrop(), RoundedCorners(16)).into(holder.image)
    }

    override fun getItemCount(): Int {
        return images.size
    }
}

interface IImagesRVAdapter {
    fun onImageClick(position: Int)
}