package dev.jaym21.skanner.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_table")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val path: String,
    val pageCount: Int
)
