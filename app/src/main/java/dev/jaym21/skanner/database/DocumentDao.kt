package dev.jaym21.skanner.database

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.jaym21.skanner.models.Document

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Update
    suspend fun updateDocument(document: Document)

    @Query("SELECT * FROM document_table")
    fun getAllDocuments(): LiveData<List<Document>>
}