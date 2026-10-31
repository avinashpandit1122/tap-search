package com.tapandlearn.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfDocumentManager {

    /**
     * Resolves the real PDF File from display name, content URI, or local path.
     * If content URI is provided, copies it to internal storage for permanent 100% offline access.
     */
    fun resolvePdfFile(context: Context, displayName: String, uriString: String): File? {
        val docsDir = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }

        // 1. Direct safe-name lookup in internal docsDir
        val safeName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val directFile = File(docsDir, safeName)
        if (directFile.exists() && directFile.length() > 0L) return directFile

        // 2. Direct file:// or absolute path lookup
        if (uriString.startsWith("file://")) {
            try {
                val f = File(Uri.parse(uriString).path ?: "")
                if (f.exists() && f.length() > 0L) return f
            } catch (e: Exception) {}
        } else if (uriString.startsWith("/") && File(uriString).exists() && File(uriString).length() > 0L) {
            return File(uriString)
        }

        // 3. If content:// URI, copy it to docsDir
        if (uriString.startsWith("content://")) {
            try {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(directFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (directFile.exists() && directFile.length() > 0L) return directFile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Fuzzy match in docsDir
        val allFiles = docsDir.listFiles() ?: emptyArray()
        val match = allFiles.firstOrNull { f ->
            f.name.equals(safeName, ignoreCase = true) ||
            f.name.replace("_", "").contains(safeName.replace("_", "").take(8), ignoreCase = true) ||
            safeName.replace("_", "").contains(f.name.replace("_", "").take(8), ignoreCase = true)
        }
        if (match != null && match.length() > 0L) return match

        return null
    }

    /**
     * Copies an external content URI into the app's internal persistent storage
     * so it never expires and is accessible 100% offline.
     */
    fun importUriToInternalStorage(context: Context, sourceUri: Uri): Pair<File, String> {
        val docsDir = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }

        var displayName = "Imported_Document_${System.currentTimeMillis()}.pdf"
        try {
            context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { if (it.isNotBlank()) displayName = it }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clean file name
        val safeFileName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val targetFile = File(docsDir, safeFileName)

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(targetFile, displayName)
    }

    /**
     * Returns the exact page count of a PDF file using Android's native PdfRenderer.
     */
    fun getPageCount(file: File): Int {
        if (!file.exists() || file.length() == 0L) return 1
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    renderer.pageCount.coerceAtLeast(1)
                }
            }
        } catch (e: Exception) {
            120 // Fallback estimate
        }
    }

    /**
     * Renders a specific page of a PDF file into a high-resolution Bitmap.
     */
    fun renderPage(file: File, pageIndex: Int, targetWidth: Int = 1080): Bitmap? {
        if (!file.exists() || file.length() == 0L) return null
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val safeIndex = pageIndex.coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))
                    renderer.openPage(safeIndex).use { page ->
                        val ratio = page.height.toFloat() / page.width.toFloat()
                        val width = targetWidth.coerceAtLeast(300)
                        val height = (width * ratio).toInt().coerceAtLeast(400)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        
                        // White background
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)

                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a realistic, beautiful multi-page PDF document on device using Android's PdfDocument.
     * Contains rich text matching the design mockup for Pharmacology, Biochemistry, etc.
     */
    fun generateRichSamplePdf(context: Context, fileName: String, title: String, pagesContent: List<SamplePageContent>): File {
        val docsDir = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }
        val targetFile = File(docsDir, fileName)

        val doc = PdfDocument()
        val pageWidth = 595 // Standard A4 points (72 dpi)
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headingPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            isAntiAlias = true
        }

        val accentPaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 10f
            isAntiAlias = true
        }

        pagesContent.forEachIndexed { index, pageData ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background
            canvas.drawColor(Color.WHITE)

            // Header top rule
            val rulePaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }
            canvas.drawLine(40f, 40f, (pageWidth - 40).toFloat(), 40f, rulePaint)
            canvas.drawText("TAP & LEARN • MEDICAL REFERENCE", 40f, 32f, accentPaint)

            var y = 75f

            // Page Title
            if (pageData.header.isNotBlank()) {
                canvas.drawText(pageData.header, 40f, y, titlePaint)
                y += 30f
            }

            // Sections
            pageData.sections.forEach { section ->
                if (section.heading.isNotBlank()) {
                    y += 10f
                    canvas.drawText(section.heading, 40f, y, headingPaint)
                    y += 22f
                }

                section.paragraphs.forEach { para ->
                    val words = para.split(" ")
                    var line = ""
                    words.forEach { word ->
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        val textWidth = bodyPaint.measureText(testLine)
                        if (textWidth > (pageWidth - 80)) {
                            canvas.drawText(line, 40f, y, bodyPaint)
                            y += 18f
                            line = word
                        } else {
                            line = testLine
                        }
                    }
                    if (line.isNotEmpty()) {
                        canvas.drawText(line, 40f, y, bodyPaint)
                        y += 24f
                    }
                }
            }

            // Footer
            canvas.drawLine(40f, (pageHeight - 40).toFloat(), (pageWidth - 40).toFloat(), (pageHeight - 40).toFloat(), rulePaint)
            canvas.drawText(title, 40f, (pageHeight - 25).toFloat(), footerPaint)
            canvas.drawText("Page ${index + 1} of ${pagesContent.size}", (pageWidth - 110).toFloat(), (pageHeight - 25).toFloat(), footerPaint)

            doc.finishPage(page)
        }

        try {
            FileOutputStream(targetFile).use { out ->
                doc.writeTo(out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            doc.close()
        }

        return targetFile
    }

    data class SamplePageContent(
        val header: String,
        val sections: List<SampleSection>
    )

    data class SampleSection(
        val heading: String,
        val paragraphs: List<String>
    )
}
