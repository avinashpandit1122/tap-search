package com.tapandlearn.data.documents

import android.net.Uri
import com.tapandlearn.core.database.DatabaseHelper
import com.tapandlearn.domain.model.Document
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val dbHelper: DatabaseHelper
) {
    val documents: Flow<List<Document>> = dbHelper.documentsFlow

    fun getAllDocuments(): List<Document> = dbHelper.getAllDocuments()

    fun getDocumentById(id: Long): Document? = dbHelper.getDocumentById(id)

    fun addDocument(displayName: String, uri: Uri, pageCount: Int = 120, fileSize: String = "4.2 MB"): Long {
        return dbHelper.insertDocument(displayName, uri.toString(), pageCount, fileSize)
    }

    fun toggleFavorite(id: Long) {
        dbHelper.toggleDocumentFavorite(id)
    }

    fun deleteDocument(id: Long) {
        dbHelper.deleteDocument(id)
    }
}
