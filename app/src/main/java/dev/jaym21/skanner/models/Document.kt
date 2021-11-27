package dev.jaym21.skanner.models

import androidx.room.PrimaryKey

data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

)
