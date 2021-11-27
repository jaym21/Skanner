package dev.jaym21.skanner.components

import androidx.lifecycle.LiveData
import dev.jaym21.skanner.database.DocumentDao
import dev.jaym21.skanner.models.Document

class DocumentRepository(private val documentDao: DocumentDao) {

    suspend fun insertDocument(document: Document) = documentDao.insertDocument(document)

    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)

    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)

    val allDocuments: LiveData<List<Document>> = documentDao.getAllDocuments()
}