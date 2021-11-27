package dev.jaym21.skanner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dev.jaym21.skanner.components.DocumentRepository
import dev.jaym21.skanner.database.DocumentDatabase
import dev.jaym21.skanner.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application): AndroidViewModel(application) {

    val allDocuments: LiveData<List<Document>>
    private val repository: DocumentRepository

    init {
        val dao = DocumentDatabase.getDatabase(application).getDocumentDAO()
        repository = DocumentRepository(dao)
        allDocuments = repository.allDocuments
    }

    fun addDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertDocument(document)
    }

    fun removeDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteDocument(document)
    }

    fun updateDocument(document: Document) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateDocument(document)
    }
}