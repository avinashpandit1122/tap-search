package com.tapandlearn.feature.library

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.TapAndLearnApplication
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.core.pdf.PdfDocumentManager
import com.tapandlearn.domain.model.Document
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    onOpenDocument: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val coroutineScope = rememberCoroutineScope()
    val documents by app.documentRepository.documents.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val displayDocs = remember(documents, searchQuery, selectedFilter) {
        documents.filter { doc ->
            (searchQuery.isEmpty() || doc.displayName.contains(searchQuery, ignoreCase = true)) &&
            when (selectedFilter) {
                "Favorites" -> doc.isFavorite
                else -> true
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            coroutineScope.launch {
                try {
                    val (internalFile, fileName) = PdfDocumentManager.importUriToInternalStorage(context, pickedUri)
                    val realPages = PdfDocumentManager.getPageCount(internalFile)
                    val bytes = internalFile.length()
                    val fileSize = if (bytes > 1024 * 1024) {
                        "${(bytes / (1024 * 1024)).coerceAtLeast(1)} MB"
                    } else {
                        "${(bytes / 1024).coerceAtLeast(1)} KB"
                    }

                    val newId = app.documentRepository.addDocument(fileName, Uri.fromFile(internalFile), realPages, fileSize)
                    Toast.makeText(context, "Document imported: $fileName", Toast.LENGTH_SHORT).show()
                    onOpenDocument(newId, fileName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Text(
                    text = "My Library",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Import PDF", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search documents...", color = TextMuted, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All (${displayDocs.size})", "Recent", "Favorites")) { filter ->
                    val filterName = filter.substringBefore(" (")
                    val isSelected = selectedFilter == filterName || (selectedFilter == "All" && filter.startsWith("All"))
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = filterName },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Documents List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayDocs) { doc ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDocument(doc.id, doc.displayName) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Document Thumbnail
                            Surface(
                                modifier = Modifier.size(50.dp, 62.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxSize().padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .background(PrimaryBlue)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(2.dp)
                                                .background(Color(0xFFCBD5E1))
                                        )
                                    }
                                }
                            }

                            // Document Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${doc.pageCount} pages • ${doc.fileSizeFormatted}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = doc.lastOpenedFormatted,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            // Favorite Star
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    app.documentRepository.toggleFavorite(doc.id)
                                }
                            }) {
                                Icon(
                                    imageVector = if (doc.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (doc.isFavorite) AccentOrange else TextMuted
                                )
                            }

                            // 3-Dots Menu
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = TextSecondary
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open Document") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PrimaryBlue) },
                                        onClick = {
                                            menuExpanded = false
                                            onOpenDocument(doc.id, doc.displayName)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (doc.isFavorite) "Remove Favorite" else "Add to Favorites") },
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = AccentOrange) },
                                        onClick = {
                                            menuExpanded = false
                                            coroutineScope.launch {
                                                app.documentRepository.toggleFavorite(doc.id)
                                            }
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Delete Document", color = AccentRed) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed) },
                                        onClick = {
                                            menuExpanded = false
                                            coroutineScope.launch {
                                                app.documentRepository.deleteDocument(doc.id)
                                                Toast.makeText(context, "Deleted ${doc.displayName}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}
