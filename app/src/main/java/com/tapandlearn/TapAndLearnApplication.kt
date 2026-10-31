package com.tapandlearn

import android.app.Application
import com.tapandlearn.core.database.DatabaseHelper
import com.tapandlearn.core.tts.TtsManager
import com.tapandlearn.data.dictionary.DictionaryRepository
import com.tapandlearn.data.documents.DocumentRepository
import com.tapandlearn.data.vocabulary.VocabularyRepository

class TapAndLearnApplication : Application() {

    lateinit var databaseHelper: DatabaseHelper
        private set
    lateinit var documentRepository: DocumentRepository
        private set
    lateinit var vocabularyRepository: VocabularyRepository
        private set
    lateinit var dictionaryRepository: DictionaryRepository
        private set
    lateinit var ttsManager: TtsManager
        private set

    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
        documentRepository = DocumentRepository(databaseHelper)
        vocabularyRepository = VocabularyRepository(databaseHelper)
        dictionaryRepository = DictionaryRepository()
        ttsManager = TtsManager(this)
    }

    override fun onTerminate() {
        ttsManager.shutdown()
        super.onTerminate()
    }
}
