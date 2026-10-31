package com.tapandlearn.feature.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.TapAndLearnApplication
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.domain.model.SavedWord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWordsScreen(
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val savedWordsFromDb by app.vocabularyRepository.savedWords.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Preloaded mock items matching Screen 10 if DB is empty
    val defaultSavedWords = remember {
        listOf(
            SavedWord(id = 1, lemma = "substance", partOfSpeech = "noun", phonetic = "/ˈsʌbstəns/", primaryDefinition = "A particular kind of matter...", hindiMeaning = "पदार्थ", exampleSentence = ""),
            SavedWord(id = 2, lemma = "mechanism", partOfSpeech = "noun", phonetic = "/ˈmɛkənɪzəm/", primaryDefinition = "A system of parts working...", hindiMeaning = "कार्यप्रणाली", exampleSentence = ""),
            SavedWord(id = 3, lemma = "therapeutic", partOfSpeech = "adjective", phonetic = "/ˌθɛrəˈpjuːtɪk/", primaryDefinition = "Relating to the treatment...", hindiMeaning = "चिकित्सीय", exampleSentence = ""),
            SavedWord(id = 4, lemma = "adverse", partOfSpeech = "adjective", phonetic = "/ˈædvɜːrs/", primaryDefinition = "Harmful or unwanted...", hindiMeaning = "प्रतिकूल", exampleSentence = ""),
            SavedWord(id = 5, lemma = "synthesize", partOfSpeech = "verb", phonetic = "/ˈsɪnθəsaɪz/", primaryDefinition = "To make or produce by...", hindiMeaning = "संश्लेषण", exampleSentence = ""),
            SavedWord(id = 6, lemma = "effective", partOfSpeech = "adjective", phonetic = "/ɪˈfɛktɪv/", primaryDefinition = "Producing the desired result...", hindiMeaning = "प्रभावी", exampleSentence = "")
        )
    }

    val displayList = remember(savedWordsFromDb, searchQuery, selectedFilter) {
        val baseList = if (savedWordsFromDb.isEmpty()) defaultSavedWords else savedWordsFromDb
        baseList.filter {
            searchQuery.isEmpty() || it.lemma.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Saved Words",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input
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
                        placeholder = { Text("Search saved words...", color = TextMuted, fontSize = 14.sp) },
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

            // Filter Pills & Revision Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(listOf("All", "Recent", "Bookmarked")) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier.clickable { selectedFilter = filter },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Quick Practice Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onNavigateToFlashcards,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Flashcards", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onNavigateToQuiz,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Saved Words List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayList) { word ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = word.lemma,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = word.partOfSpeech,
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = word.primaryDefinition,
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }

                            // TTS Speaker
                            IconButton(onClick = { app.ttsManager.speak(word.lemma) }) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = PrimaryBlue
                                )
                            }

                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = TextSecondary
                                )
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
