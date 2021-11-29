package dev.jaym21.skanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jaym21.skanner.R
import dev.jaym21.skanner.models.Document

class DocumentsRVAdapter(private val listener: IDocumentAdapter): ListAdapter<Document,DocumentsRVAdapter.DocumentsViewHolder>(DocumentsDiffUtil()) {

    class DocumentsDiffUtil: DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }

    inner class DocumentsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvDocumentName)
        val pages: TextView = itemView.findViewById(R.id.tvDocumentPages)
        val root: RelativeLayout = itemView.findViewById(R.id.rlDocument)
        val options: ImageView = itemView.findViewById(R.id.ivMoreOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsViewHolder {
        val viewHolder =  DocumentsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_documents_item_layout, parent, false))
        viewHolder.root.setOnClickListener {
            listener.onDocumentClicked(getItem(viewHolder.adapterPosition))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.name.text = currentItem.name

        val noOfPages = if (currentItem.pageCount == 1)
            "${currentItem.pageCount} page"
        else
            "${currentItem.pageCount} pages"

        holder.pages.text = noOfPages

        holder.options.setOnClickListener {
            //creating a popup menu to show more menu options
            val popup = PopupMenu(holder.itemView.context, holder.options)
            //inflating popup menu with layout
            popup.inflate(R.menu.documents_option_menu)
        }
    }
}

interface IDocumentAdapter{
    fun onDocumentClicked(document: Document)
}