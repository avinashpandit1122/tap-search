package com.tapandlearn.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.tapandlearn.domain.model.Document
import com.tapandlearn.domain.model.SavedWord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _documentsFlow = MutableStateFlow<List<Document>>(emptyList())
    val documentsFlow: Flow<List<Document>> = _documentsFlow.asStateFlow()

    private val _savedWordsFlow = MutableStateFlow<List<SavedWord>>(emptyList())
    val savedWordsFlow: Flow<List<SavedWord>> = _savedWordsFlow.asStateFlow()

    init {
        seedInitialDocumentsIfEmpty()
        ensureSampleDocumentsSeeded()
        refreshDocuments()
        refreshSavedWords()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                displayName TEXT NOT NULL,
                uriString TEXT NOT NULL,
                pageCount INTEGER DEFAULT 120,
                lastPageRead INTEGER DEFAULT 1,
                progressPercent REAL DEFAULT 0.0,
                fileSizeFormatted TEXT DEFAULT '2.4 MB',
                lastOpenedFormatted TEXT DEFAULT 'Today',
                isFavorite INTEGER DEFAULT 0,
                importedAt INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE saved_words (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                lemma TEXT NOT NULL,
                partOfSpeech TEXT,
                phonetic TEXT,
                primaryDefinition TEXT NOT NULL,
                hindiMeaning TEXT,
                exampleSentence TEXT,
                sourceDocumentName TEXT,
                sourcePage INTEGER,
                savedAt INTEGER,
                isBookmarked INTEGER DEFAULT 0,
                reviewCount INTEGER DEFAULT 0,
                intervalDays INTEGER DEFAULT 1,
                easeFactor REAL DEFAULT 2.5,
                nextReviewAt INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS documents")
        db.execSQL("DROP TABLE IF EXISTS saved_words")
        onCreate(db)
    }

    private fun seedInitialDocumentsIfEmpty() {
        val existing = getAllDocuments()
        if (existing.isNotEmpty()) return

        val docsDir = File(context.filesDir, "documents")
        if (!docsDir.exists()) docsDir.mkdirs()

        // 1. Pharmacology Notes with rich text matching Screen 07
        val pharmFile = com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
            context = context,
            fileName = "Pharmacology_Notes.pdf",
            title = "Pharmacology Notes",
            pagesContent = listOf(
                com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                    header = "Introduction",
                    sections = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                            heading = "",
                            paragraphs = listOf(
                                "Pharmacology is the study of how drugs interact with living organisms. It helps us understand the mechanism of action, therapeutic uses, adverse effects and dosage of medicines.",
                                "A drug is a chemical substance that produces a biological effect in the body. Drugs can be derived from natural sources or synthesized in laboratories.",
                                "Pharmacology plays a vital role in modern medicine and healthcare. It helps in the development of new drugs and ensures their safe and effective use in patients."
                            )
                        ),
                        com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                            heading = "Pharmacodynamics & Receptors",
                            paragraphs = listOf(
                                "Pharmacodynamics investigates the biochemical and physiologic effects of drugs on organ systems. Most drugs bind to regulatory macromolecules termed receptors.",
                                "An agonist activates receptor signaling cascades, while an antagonist binds without activating and prevents agonist binding.",
                                "Dose-response relationships quantify the biological magnitude of clinical response relative to plasma concentration."
                            )
                        )
                    )
                ),
                com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                    header = "Pharmacokinetics (ADME)",
                    sections = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                            heading = "Absorption and Bioavailability",
                            paragraphs = listOf(
                                "Absorption represents the movement of unchanged drug molecules from the site of administration into systemic circulation.",
                                "Bioavailability is the fractional extent of an administered dose that reaches the active site unchanged.",
                                "First-pass hepatic metabolism can significantly reduce oral bioavailability before reaching systemic capillaries."
                            )
                        )
                    )
                )
            )
        )

        // 2. Biochemistry
        val bioFile = com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
            context = context,
            fileName = "Biochemistry.pdf",
            title = "Biochemistry & Cellular Energy",
            pagesContent = listOf(
                com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                    header = "Cellular Metabolism",
                    sections = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                            heading = "Glycolysis & ATP Synthesis",
                            paragraphs = listOf(
                                "Glycolysis converts glucose into pyruvate through a series of ten enzyme-catalyzed cytoplasmic reactions.",
                                "Adenosine triphosphate acts as universal currency transferring high-energy phosphoryl groups between catabolic and anabolic pathways.",
                                "Oxidative phosphorylation across the inner mitochondrial membrane drives aerobic ATP regeneration via proton gradients."
                            )
                        )
                    )
                )
            )
        )

        // 3. English Vocabulary
        val vocabFile = com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
            context = context,
            fileName = "English_Vocabulary.pdf",
            title = "Essential Academic Vocabulary",
            pagesContent = listOf(
                com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                    header = "Academic Lexicon",
                    sections = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                            heading = "Core Scientific Terminology",
                            paragraphs = listOf(
                                "Proficiency in nuanced technical vocabulary enhances comprehension of research literature and clinical protocols.",
                                "Cognitive acquisition of root morphemes, affixes, and contextual connotations accelerates reading speed.",
                                "Active recall and spaced repetition reinforce persistent long-term lexical memory pathways."
                            )
                        )
                    )
                )
            )
        )

        insertDocument(
            displayName = "Pharmacology Notes.pdf",
            uriString = Uri.fromFile(pharmFile).toString(),
            pageCount = 120,
            fileSize = "5 MB"
        )
        insertDocument(
            displayName = "Biochemistry.pdf",
            uriString = Uri.fromFile(bioFile).toString(),
            pageCount = 230,
            fileSize = "12 MB"
        )
        insertDocument(
            displayName = "English Vocabulary.pdf",
            uriString = Uri.fromFile(vocabFile).toString(),
            pageCount = 85,
            fileSize = "8 MB"
        )
    }

    private fun ensureSampleDocumentsSeeded() {
        val existing = getAllDocuments()
        val titles = existing.map { it.displayName }
        val docsDir = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }

        if (!titles.any { it.contains("Pharm", true) }) {
            val file = File(docsDir, "Pharmacology_Notes.pdf")
            if (!file.exists()) {
                com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
                    context = context,
                    fileName = "Pharmacology_Notes.pdf",
                    title = "Pharmacology Notes",
                    pagesContent = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                            header = "Introduction",
                            sections = listOf(
                                com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                                    heading = "",
                                    paragraphs = listOf(
                                        "Pharmacology is the study of how drugs interact with living organisms. It helps us understand the mechanism of action, therapeutic uses, adverse effects and dosage of medicines.",
                                        "A drug is a chemical substance that produces a biological effect in the body. Drugs can be derived from natural sources or synthesized in laboratories.",
                                        "Pharmacology plays a vital role in modern medicine and healthcare. It helps in the development of new drugs and ensures their safe and effective use in patients."
                                    )
                                )
                            )
                        )
                    )
                )
            }
            insertDocument("Pharmacology Notes.pdf", Uri.fromFile(file).toString(), 120, "5 MB")
        }
        if (!titles.any { it.contains("Bio", true) }) {
            val file = File(docsDir, "Biochemistry.pdf")
            if (!file.exists()) {
                com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
                    context = context,
                    fileName = "Biochemistry.pdf",
                    title = "Biochemistry",
                    pagesContent = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                            header = "Cellular Metabolism",
                            sections = listOf(
                                com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                                    heading = "Glycolysis & ATP Synthesis",
                                    paragraphs = listOf(
                                        "Glycolysis converts glucose into pyruvate through a series of ten enzyme-catalyzed cytoplasmic reactions.",
                                        "Adenosine triphosphate acts as universal currency transferring high-energy phosphoryl groups."
                                    )
                                )
                            )
                        )
                    )
                )
            }
            insertDocument("Biochemistry.pdf", Uri.fromFile(file).toString(), 230, "12 MB")
        }
        if (!titles.any { it.contains("Vocab", true) }) {
            val file = File(docsDir, "English_Vocabulary.pdf")
            if (!file.exists()) {
                com.tapandlearn.core.pdf.PdfDocumentManager.generateRichSamplePdf(
                    context = context,
                    fileName = "English_Vocabulary.pdf",
                    title = "English Vocabulary",
                    pagesContent = listOf(
                        com.tapandlearn.core.pdf.PdfDocumentManager.SamplePageContent(
                            header = "Essential Lexicon",
                            sections = listOf(
                                com.tapandlearn.core.pdf.PdfDocumentManager.SampleSection(
                                    heading = "Core Terminology",
                                    paragraphs = listOf(
                                        "Proficiency in nuanced technical vocabulary enhances comprehension of research literature and clinical protocols."
                                    )
                                )
                            )
                        )
                    )
                )
            }
            insertDocument("English Vocabulary.pdf", Uri.fromFile(file).toString(), 85, "8 MB")
        }
    }

    fun getDocumentById(id: Long): Document? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM documents WHERE id = ? LIMIT 1", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                return Document(
                    id = it.getLong(it.getColumnIndexOrThrow("id")),
                    displayName = it.getString(it.getColumnIndexOrThrow("displayName")),
                    uriString = it.getString(it.getColumnIndexOrThrow("uriString")),
                    pageCount = it.getInt(it.getColumnIndexOrThrow("pageCount")),
                    lastPageRead = it.getInt(it.getColumnIndexOrThrow("lastPageRead")),
                    progressPercent = it.getFloat(it.getColumnIndexOrThrow("progressPercent")),
                    fileSizeFormatted = it.getString(it.getColumnIndexOrThrow("fileSizeFormatted")),
                    lastOpenedFormatted = it.getString(it.getColumnIndexOrThrow("lastOpenedFormatted")),
                    isFavorite = it.getInt(it.getColumnIndexOrThrow("isFavorite")) == 1,
                    importedAt = it.getLong(it.getColumnIndexOrThrow("importedAt"))
                )
            }
        }
        return null
    }

    fun getAllDocuments(): List<Document> {
        val list = mutableListOf<Document>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM documents ORDER BY importedAt DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Document(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        displayName = it.getString(it.getColumnIndexOrThrow("displayName")),
                        uriString = it.getString(it.getColumnIndexOrThrow("uriString")),
                        pageCount = it.getInt(it.getColumnIndexOrThrow("pageCount")),
                        lastPageRead = it.getInt(it.getColumnIndexOrThrow("lastPageRead")),
                        progressPercent = it.getFloat(it.getColumnIndexOrThrow("progressPercent")),
                        fileSizeFormatted = it.getString(it.getColumnIndexOrThrow("fileSizeFormatted")),
                        lastOpenedFormatted = it.getString(it.getColumnIndexOrThrow("lastOpenedFormatted")),
                        isFavorite = it.getInt(it.getColumnIndexOrThrow("isFavorite")) == 1,
                        importedAt = it.getLong(it.getColumnIndexOrThrow("importedAt"))
                    )
                )
            }
        }
        return list
    }

    fun insertDocument(displayName: String, uriString: String, pageCount: Int, fileSize: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("displayName", displayName)
            put("uriString", uriString)
            put("pageCount", pageCount)
            put("fileSizeFormatted", fileSize)
            put("lastOpenedFormatted", "Today")
            put("importedAt", System.currentTimeMillis())
        }
        val id = db.insert("documents", null, values)
        refreshDocuments()
        return id
    }

    fun toggleDocumentFavorite(id: Long) {
        val db = writableDatabase
        db.execSQL("UPDATE documents SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = ?", arrayOf(id))
        refreshDocuments()
    }

    fun deleteDocument(id: Long) {
        val db = writableDatabase
        db.delete("documents", "id = ?", arrayOf(id.toString()))
        refreshDocuments()
    }

    fun getAllSavedWords(): List<SavedWord> {
        val list = mutableListOf<SavedWord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM saved_words ORDER BY savedAt DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SavedWord(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        lemma = it.getString(it.getColumnIndexOrThrow("lemma")),
                        partOfSpeech = it.getString(it.getColumnIndexOrThrow("partOfSpeech")) ?: "noun",
                        phonetic = it.getString(it.getColumnIndexOrThrow("phonetic")) ?: "",
                        primaryDefinition = it.getString(it.getColumnIndexOrThrow("primaryDefinition")),
                        hindiMeaning = it.getString(it.getColumnIndexOrThrow("hindiMeaning")) ?: "",
                        exampleSentence = it.getString(it.getColumnIndexOrThrow("exampleSentence")) ?: "",
                        sourceDocumentName = it.getString(it.getColumnIndexOrThrow("sourceDocumentName")),
                        sourcePage = it.getInt(it.getColumnIndexOrThrow("sourcePage")),
                        savedAt = it.getLong(it.getColumnIndexOrThrow("savedAt")),
                        isBookmarked = it.getInt(it.getColumnIndexOrThrow("isBookmarked")) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertSavedWord(
        lemma: String,
        partOfSpeech: String,
        phonetic: String,
        definition: String,
        hindiMeaning: String,
        example: String,
        documentName: String? = null,
        page: Int? = null
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("lemma", lemma)
            put("partOfSpeech", partOfSpeech)
            put("phonetic", phonetic)
            put("primaryDefinition", definition)
            put("hindiMeaning", hindiMeaning)
            put("exampleSentence", example)
            put("sourceDocumentName", documentName)
            put("sourcePage", page)
            put("savedAt", System.currentTimeMillis())
        }
        val id = db.insert("saved_words", null, values)
        refreshSavedWords()
        return id
    }

    fun toggleWordBookmark(id: Long) {
        val db = writableDatabase
        db.execSQL("UPDATE saved_words SET isBookmarked = CASE WHEN isBookmarked = 1 THEN 0 ELSE 1 END WHERE id = ?", arrayOf(id))
        refreshSavedWords()
    }

    fun deleteSavedWord(id: Long) {
        val db = writableDatabase
        db.delete("saved_words", "id = ?", arrayOf(id.toString()))
        refreshSavedWords()
    }

    fun recordReview(id: Long, known: Boolean) {
        val interval = if (known) 3 else 1
        val nextReview = System.currentTimeMillis() + (interval * 24 * 60 * 60 * 1000L)
        val db = writableDatabase
        db.execSQL(
            "UPDATE saved_words SET reviewCount = reviewCount + 1, nextReviewAt = ?, intervalDays = ? WHERE id = ?",
            arrayOf<Any>(nextReview, interval, id)
        )
        refreshSavedWords()
    }

    fun refreshDocuments() {
        _documentsFlow.value = getAllDocuments()
    }

    fun refreshSavedWords() {
        _savedWordsFlow.value = getAllSavedWords()
    }

    companion object {
        const val DATABASE_NAME = "tap_and_learn_user.db"
        const val DATABASE_VERSION = 1
    }
}
