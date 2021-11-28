package dev.jaym21.skanner.adapters

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jaym21.skanner.models.Document

class DocumentsRVAdapter(): ListAdapter<Document,DocumentsRVAdapter.DocumentsViewHolder>() {

    inner class DocumentsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }
}