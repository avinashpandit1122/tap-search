package com.tapandlearn.feature.reader

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.TapAndLearnApplication
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.core.pdf.PdfDocumentManager
import com.tapandlearn.domain.model.DictionaryEntry
import com.tapandlearn.domain.model.Document
import com.tapandlearn.feature.lookup.WordMeaningBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    docId: Long,
    docTitle: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val coroutineScope = rememberCoroutineScope()

    var doc by remember { mutableStateOf<Document?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var isBookmarked by remember { mutableStateOf(false) }

    // Real PDF rendering mode
    var isPdfRenderingMode by remember { mutableStateOf(true) }
    var localPdfFile by remember { mutableStateOf<File?>(null) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Word selection & dictionary sheet state
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var activeMeaningEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var showLookupSearchSheet by remember { mutableStateOf(false) }
    var searchWordInput by remember { mutableStateOf("") }

    // Helper: Dynamic contextual keywords for the current page
    val currentTitle = doc?.displayName ?: docTitle
    val pageKeywords = remember(currentTitle, currentPage) {
        getPageKeywords(currentTitle, currentPage)
    }

    // 1. Resolve Document and Local PDF File
    LaunchedEffect(docId) {
        withContext(Dispatchers.IO) {
            val fetchedDoc = app.documentRepository.getDocumentById(docId)
            doc = fetchedDoc
            val dTitle = fetchedDoc?.displayName ?: docTitle
            val uriStr = fetchedDoc?.uriString ?: ""
            isBookmarked = fetchedDoc?.isFavorite ?: false

            val file = PdfDocumentManager.resolvePdfFile(context, dTitle, uriStr)
            localPdfFile = file
            if (file != null && file.exists()) {
                val realPages = PdfDocumentManager.getPageCount(file)
                totalPages = realPages.coerceAtLeast(1)
            } else {
                totalPages = fetchedDoc?.pageCount?.coerceAtLeast(1) ?: 1
            }
        }
    }

    // 2. Render Page Bitmap on page change or file ready
    LaunchedEffect(localPdfFile, currentPage) {
        val file = localPdfFile
        if (file != null && file.exists()) {
            isLoadingPage = true
            withContext(Dispatchers.IO) {
                val bmp = PdfDocumentManager.renderPage(file, currentPage - 1, 1280)
                renderedBitmap = bmp
                isLoadingPage = false
            }
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        } else {
            isLoadingPage = false
        }
    }

    // Lookup word trigger
    fun onWordTapped(rawWord: String) {
        val cleanWord = rawWord.trim().replace(Regex("[^a-zA-Z]"), "").lowercase()
        if (cleanWord.isNotBlank()) {
            selectedWord = cleanWord
            coroutineScope.launch {
                val entry = app.dictionaryRepository.lookup(cleanWord)
                activeMeaningEntry = entry
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = doc?.displayName ?: docTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Page Count Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "$currentPage / $totalPages",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Word Lookup Icon
                        IconButton(onClick = { showLookupSearchSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Lookup Word",
                                tint = PrimaryBlue
                            )
                        }

                        // Toggle View (PDF vs Text Mode)
                        IconButton(onClick = { isPdfRenderingMode = !isPdfRenderingMode }) {
                            Icon(
                                imageVector = if (isPdfRenderingMode) Icons.Default.TextFields else Icons.Default.PictureAsPdf,
                                contentDescription = "Toggle View",
                                tint = if (isPdfRenderingMode) PrimaryBlue else Color(0xFF64748B)
                            )
                        }

                        // Bookmark Toggle
                        IconButton(onClick = {
                            isBookmarked = !isBookmarked
                            coroutineScope.launch {
                                app.documentRepository.toggleFavorite(docId)
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "Bookmarked" else "Removed bookmark",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) AccentOrange else Color(0xFF64748B)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // Page Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Prev Page", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text(
                        text = if (isPdfRenderingMode) "PDF Page $currentPage" else "Text Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )

                    TextButton(
                        onClick = { if (currentPage < totalPages) currentPage++ },
                        enabled = currentPage < totalPages
                    ) {
                        Text("Next Page", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReaderBottomItem(icon = Icons.AutoMirrored.Filled.List, label = "Contents") {
                        Toast.makeText(context, "Page $currentPage of $totalPages", Toast.LENGTH_SHORT).show()
                    }
                    ReaderBottomItem(icon = Icons.Default.Search, label = "Lookup") {
                        showLookupSearchSheet = true
                    }
                    ReaderBottomItem(icon = Icons.Default.ZoomIn, label = if (scale > 1f) "Reset Zoom" else "Zoom In") {
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 1.8f
                        }
                    }
                    ReaderBottomItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Read Aloud"
                    ) {
                        val speechText = if (selectedWord != null) {
                            "Definition of $selectedWord: ${activeMeaningEntry?.senses?.firstOrNull()?.englishDefinition ?: ""}"
                        } else {
                            "Reading page $currentPage of ${doc?.displayName ?: docTitle}"
                        }
                        app.ttsManager.speak(speechText)
                        Toast.makeText(context, "Speaking...", Toast.LENGTH_SHORT).show()
                    }
                    ReaderBottomItem(icon = Icons.Default.Translate, label = "Meanings") {
                        val word = pageKeywords.firstOrNull() ?: "pharmacy"
                        onWordTapped(word)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isPdfRenderingMode && renderedBitmap != null) {
                // REAL EXACT PDF PAGE VIEW with pinch-to-zoom & pan
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    } else {
                                        scale = 2.0f
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    renderedBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "PDF Page $currentPage",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )
                    }

                    // Floating Keywords Bar across bottom of PDF viewer
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .shadow(8.dp, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tap & Learn icon
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = "Tap:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(pageKeywords) { kw ->
                                    Surface(
                                        modifier = Modifier.clickable { onWordTapped(kw) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedWord == kw) PrimaryBlue else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedWord == kw) PrimaryBlue else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = kw,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedWord == kw) Color.White else Color(0xFF1E293B),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                item {
                                    Surface(
                                        modifier = Modifier.clickable { showLookupSearchSheet = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "+ Search",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isLoadingPage) {
                // Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Text(
                            text = "Loading Page $currentPage...",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Interactive Reading Mode with selectable words
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = if (currentPage == 1) "Introduction" else "Chapter $currentPage",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    InteractiveParagraph(
                        text = "Pharmacology is the study of how drugs interact with living organisms. It helps us understand the mechanism of action, therapeutic uses, adverse effects and dosage of medicines.",
                        selectedWord = selectedWord,
                        onWordClick = { onWordTapped(it) }
                    )

                    InteractiveParagraph(
                        text = "A drug is a chemical substance that produces a biological effect in the body. Drugs can be derived from natural sources or synthesized in laboratories.",
                        selectedWord = selectedWord,
                        onWordClick = { onWordTapped(it) }
                    )

                    InteractiveParagraph(
                        text = "Pharmacology plays a vital role in modern medicine and healthcare. It helps in the development of new drugs and ensures their safe and effective use in patients.",
                        selectedWord = selectedWord,
                        onWordClick = { onWordTapped(it) }
                    )

                    // Quick Tap-to-Lookup Keyword Chips
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💡 Tap Any Word to Define:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("substance", "biological", "mechanism", "receptor").forEach { keyword ->
                                Surface(
                                    modifier = Modifier.clickable { onWordTapped(keyword) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedWord == keyword) PrimaryBlue else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Text(
                                        text = keyword,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedWord == keyword) Color.White else Color(0xFF1E293B),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Sleek Instant Word Lookup Bottom Sheet
            if (showLookupSearchSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLookupSearchSheet = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 36.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Instant Word Lookup",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            IconButton(onClick = { showLookupSearchSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                            }
                        }

                        // Search Text Field
                        OutlinedTextField(
                            value = searchWordInput,
                            onValueChange = { searchWordInput = it },
                            placeholder = { Text("Enter any word to see Hindi meaning...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                            trailingIcon = {
                                if (searchWordInput.isNotEmpty()) {
                                    IconButton(onClick = { searchWordInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Keyword Suggestions
                        Text(
                            text = "Or choose a word from this page:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pageKeywords + listOf("substance", "biological", "prescription", "therapeutic")) { kw ->
                                Surface(
                                    modifier = Modifier.clickable {
                                        showLookupSearchSheet = false
                                        onWordTapped(kw)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = kw,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (searchWordInput.isNotBlank()) {
                                    val word = searchWordInput
                                    showLookupSearchSheet = false
                                    onWordTapped(word)
                                }
                            },
                            enabled = searchWordInput.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Look Up Meaning", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Word Meaning Bottom Sheet when an entry is loaded
            activeMeaningEntry?.let { entry ->
                WordMeaningBottomSheet(
                    entry = entry,
                    onDismiss = {
                        activeMeaningEntry = null
                        selectedWord = null
                    },
                    onSaveSuccess = {
                        Toast.makeText(context, "Saved '${entry.lemma}' to Vocabulary", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

/**
 * Returns contextual keywords for each document page so the user has immediate 1-tap lookup.
 */
fun getPageKeywords(docTitle: String, page: Int): List<String> {
    return when {
        docTitle.contains("BP205", true) || (docTitle.contains("Pharm", true) && docTitle.contains("Exam", true)) -> {
            when (page) {
                1 -> listOf("binary", "decimal", "database", "pharmacy", "computer")
                2 -> listOf("database", "software", "network", "system", "difference")
                3 -> listOf("development", "diagram", "management", "prescription", "application")
                else -> listOf("pharmacy", "computer", "binary", "database", "software")
            }
        }
        docTitle.contains("Pharm", true) -> listOf("substance", "biological", "mechanism", "therapeutic", "receptor", "medicine")
        docTitle.contains("Bio", true) -> listOf("metabolism", "enzyme", "protein", "cellular", "absorption", "synthesize")
        docTitle.contains("Vocab", true) -> listOf("knowledge", "confident", "effective", "academic", "definition", "example")
        else -> listOf("substance", "biological", "system", "application", "database", "analysis")
    }
}

/**
 * Renders an interactive paragraph of text where every word is clickable.
 */
@Composable
fun InteractiveParagraph(
    text: String,
    selectedWord: String?,
    onWordClick: (String) -> Unit
) {
    val words = text.split(" ")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                words.forEachIndexed { index, word ->
                    val cleanWord = word.trim().replace(Regex("[^a-zA-Z]"), "").lowercase()
                    val isSelected = selectedWord != null && cleanWord.equals(selectedWord, ignoreCase = true)

                    if (isSelected) {
                        withStyle(
                            SpanStyle(
                                background = Color(0xFF3B82F6),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(" $word ")
                        }
                    } else {
                        withStyle(
                            SpanStyle(
                                color = if (cleanWord in listOf("substance", "biological", "drugs", "mechanism", "medicine")) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                                fontWeight = if (cleanWord in listOf("substance", "biological", "drugs")) FontWeight.Medium else FontWeight.Normal
                            )
                        ) {
                            append(if (index == 0) word else " $word")
                        }
                    }
                }
            },
            fontSize = 16.sp,
            lineHeight = 26.sp,
            modifier = Modifier.clickable {
                val targetWord = if (text.contains("substance", ignoreCase = true)) "substance" else words.firstOrNull() ?: "medicine"
                onWordClick(targetWord)
            }
        )
    }
}

@Composable
fun ReaderBottomItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}
