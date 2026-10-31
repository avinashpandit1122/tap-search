package com.tapandlearn.feature.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.tapandlearn.domain.model.Document
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLibrary: () -> Unit,
    onOpenDocument: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val coroutineScope = rememberCoroutineScope()
    val documents by app.documentRepository.documents.collectAsState(initial = emptyList())

    var showProfileSheet by remember { mutableStateOf(false) }

    // SAF Document Picker with safe internal storage copy & metadata extraction
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            coroutineScope.launch {
                try {
                    val (internalFile, displayName) = PdfDocumentManager.importUriToInternalStorage(context, pickedUri)
                    val realPages = PdfDocumentManager.getPageCount(internalFile)
                    val bytes = internalFile.length()
                    val sizeFormatted = if (bytes > 1024 * 1024) {
                        "${(bytes / (1024 * 1024)).coerceAtLeast(1)} MB"
                    } else {
                        "${(bytes / 1024).coerceAtLeast(1)} KB"
                    }

                    val newId = app.documentRepository.addDocument(
                        displayName = displayName,
                        uri = Uri.fromFile(internalFile),
                        pageCount = realPages,
                        fileSize = sizeFormatted
                    )

                    Toast.makeText(context, "Imported: $displayName", Toast.LENGTH_SHORT).show()
                    onOpenDocument(newId, displayName)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Could not import PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo: Tap & Learn
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Black)) {
                            append("Tap")
                        }
                        withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Black)) {
                            append("&")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFFF59E0B), fontWeight = FontWeight.Black)) {
                            append("Learn")
                        }
                    },
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                )

                // Profile Avatar Button
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { showProfileSheet = true },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Featured Hero Banner: "Read Tap Learn Grow" with 3D Illustration
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6),
                                        Color(0xFF2563EB),
                                        Color(0xFF4338CA)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        // Soft decorative ambient clouds
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 30.dp, y = (-20).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-40).dp, y = 20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Text Block
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Read\nTap\nLearn\nGrow",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 28.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            // Right: 3D Reading Student & Book Graphic
                            Box(
                                modifier = Modifier
                                    .size(130.dp, 135.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Halo glow
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.18f))
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Student Avatar Head
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFFFDDB7), // Soft skin tone
                                        shadowElevation = 4.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            // Hair cap
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(20.dp)
                                                    .align(Alignment.TopCenter)
                                                    .background(Color(0xFF1E293B))
                                            )
                                            // Cute friendly eyes & smile
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(top = 10.dp)
                                            ) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF0F172A)))
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF0F172A)))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 3D Open Hardcover Book
                                    Surface(
                                        modifier = Modifier.size(86.dp, 44.dp),
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 10.dp, bottomEnd = 10.dp),
                                        color = Color.White,
                                        shadowElevation = 6.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left Page spread
                                                Column(
                                                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
                                                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFCBD5E1)))
                                                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(Color(0xFFCBD5E1)))
                                                }
                                                // Center Book Spine
                                                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color(0xFF94A3B8)))
                                                // Right Page spread
                                                Column(
                                                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
                                                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFCBD5E1)))
                                                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(Color(0xFFCBD5E1)))
                                                }
                                            }
                                            // Golden Ribbon Bookmark
                                            Box(
                                                modifier = Modifier
                                                    .width(6.dp)
                                                    .height(18.dp)
                                                    .align(Alignment.TopCenter)
                                                    .background(Color(0xFFF59E0B))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Action Cards: "Open PDF" and "My Library"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Open PDF Action Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(142.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0x262563EB))
                            .clickable { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 3D Blue Folder Graphic
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Open PDF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Import from device",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // My Library Action Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(142.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0x268B5CF6))
                            .clickable { onNavigateToLibrary() },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 3D Purple Open Book Graphic
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFAF5FF),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = AccentPurple,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "My Library",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "View documents",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. "Recent Reads" Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Reads",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "See All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToLibrary() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // 4. Recent Reads List Items
            items(documents) { doc ->
                var menuExpanded by remember { mutableStateOf(false) }

                // Realistic cover styling matching Mockup 05
                val (coverBg, accentColor) = when {
                    doc.displayName.contains("Pharm", ignoreCase = true) -> Color(0xFF881337) to Color(0xFFFDE047)
                    doc.displayName.contains("Bio", ignoreCase = true) -> Color(0xFF14532D) to Color(0xFF86EFAC)
                    doc.displayName.contains("Vocab", ignoreCase = true) -> Color(0xFF78350F) to Color(0xFFFDE68A)
                    else -> Color(0xFF1E3A8A) to Color(0xFF93C5FD)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0x140F172A))
                        .clickable { onOpenDocument(doc.id, doc.displayName) },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Stylized 3D Book Cover Thumbnail
                        Surface(
                            modifier = Modifier
                                .size(56.dp, 72.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = coverBg,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                // Inner embossed border
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Top header line
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(accentColor.copy(alpha = 0.8f))
                                        )

                                        // Center PDF Emblem
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        // Bottom decorative rule
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(1.5.dp)
                                                .background(accentColor.copy(alpha = 0.6f))
                                        )
                                    }
                                }

                                // Left Book Spine Highlight
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.25f))
                                )
                            }
                        }

                        // Title & Metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${doc.pageCount} pages • ${doc.lastOpenedFormatted}",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 3-Dots Menu with Dropdown Options
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFF94A3B8)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open Document", color = Color(0xFF0F172A), fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PrimaryBlue) },
                                    onClick = {
                                        menuExpanded = false
                                        onOpenDocument(doc.id, doc.displayName)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (doc.isFavorite) "Remove Favorite" else "Add to Favorites", color = Color(0xFF0F172A), fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = AccentOrange) },
                                    onClick = {
                                        menuExpanded = false
                                        coroutineScope.launch {
                                            app.documentRepository.toggleFavorite(doc.id)
                                        }
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                DropdownMenuItem(
                                    text = { Text("Delete Document", color = AccentRed, fontWeight = FontWeight.Medium) },
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
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Profile & Streak Bottom Sheet
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(44.dp))
                        }
                    }

                    Text(
                        text = "Reader Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔥 5 Days", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentOrange)
                            Text(text = "Reading Streak", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📚 ${documents.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                            Text(text = "Documents", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⏱️ 12.5 hrs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentGreen)
                            Text(text = "Reading Time", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showProfileSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
