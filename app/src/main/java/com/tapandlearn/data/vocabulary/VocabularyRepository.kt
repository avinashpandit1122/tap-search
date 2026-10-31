package com.tapandlearn.data.vocabulary

import com.tapandlearn.core.database.DatabaseHelper
import com.tapandlearn.domain.model.SavedWord
import kotlinx.coroutines.flow.Flow

class VocabularyRepository(
    private val dbHelper: DatabaseHelper
) {
    val savedWords: Flow<List<SavedWord>> = dbHelper.savedWordsFlow

    fun saveWord(
        lemma: String,
        partOfSpeech: String,
        phonetic: String,
        definition: String,
        hindiMeaning: String,
        example: String,
        documentName: String? = null,
        page: Int? = null
    ): Long {
        return dbHelper.insertSavedWord(
            lemma = lemma,
            partOfSpeech = partOfSpeech,
            phonetic = phonetic,
            definition = definition,
            hindiMeaning = hindiMeaning,
            example = example,
            documentName = documentName,
            page = page
        )
    }

    fun toggleBookmark(id: Long) {
        dbHelper.toggleWordBookmark(id)
    }

    fun recordReview(id: Long, known: Boolean) {
        dbHelper.recordReview(id, known)
    }

    fun deleteWord(word: SavedWord) {
        dbHelper.deleteSavedWord(word.id)
    }
}
