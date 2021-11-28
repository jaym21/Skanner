package dev.jaym21.skanner.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.jaym21.skanner.R
import dev.jaym21.skanner.utils.ImagesDiffUtil


class ImagesRVAdapter(private var images: List<Bitmap>): RecyclerView.Adapter<ImagesRVAdapter.ImagesViewHolder>() {

    inner class ImagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivDocumentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        return ImagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_image_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {

    }

    fun updateList(newList: List<Bitmap>) {
        val diffUtilCallback = ImagesDiffUtil(images, newList)
        //calculates the list of update operations that can covert old list into new list
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        //adding the new updated list
        images = newList
        //updating the adapter
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return images.size
    }

}