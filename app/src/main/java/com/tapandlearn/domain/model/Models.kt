package com.tapandlearn.domain.model

data class Document(
    val id: Long = 0,
    val displayName: String,
    val uriString: String,
    val pageCount: Int,
    val lastPageRead: Int = 1,
    val progressPercent: Float = 0f,
    val fileSizeFormatted: String = "2.4 MB",
    val lastOpenedFormatted: String = "Today",
    val isFavorite: Boolean = false,
    val importedAt: Long = System.currentTimeMillis()
)

data class DefinitionSense(
    val senseNumber: Int,
    val englishDefinition: String,
    val hindiMeaning: String
)

data class DictionaryEntry(
    val lemma: String,
    val partOfSpeech: String,
    val phonetic: String,
    val senses: List<DefinitionSense>,
    val exampleSentence: String,
    val relatedWords: List<String> = emptyList()
)

data class SavedWord(
    val id: Long = 0,
    val lemma: String,
    val partOfSpeech: String,
    val phonetic: String,
    val primaryDefinition: String,
    val hindiMeaning: String,
    val exampleSentence: String,
    val sourceDocumentName: String? = null,
    val sourcePage: Int? = null,
    val savedAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    // Spaced Repetition (SM-2)
    val reviewCount: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val nextReviewAt: Long = System.currentTimeMillis()
)

data class QuizQuestion(
    val id: Long,
    val word: String,
    val options: List<String>,
    val correctIndex: Int,
    val userSelectedIndex: Int? = null
)

data class ReadingStatistics(
    val booksReadCount: Int = 8,
    val wordsLearnedCount: Int = 124,
    val readingTimeFormatted: String = "12h 30m",
    val avgPerDayFormatted: String = "45 min",
    val weeklyMinutes: Map<String, Int> = mapOf(
        "Mon" to 30,
        "Tue" to 45,
        "Wed" to 35,
        "Thu" to 60,
        "Fri" to 55,
        "Sat" to 50,
        "Sun" to 40
    ),
    val topWordsLearned: List<Pair<String, Int>> = listOf(
        "mechanism" to 5,
        "therapeutic" to 4,
        "substance" to 4,
        "adverse" to 3,
        "synthesize" to 3
    )
)
