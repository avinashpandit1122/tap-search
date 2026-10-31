package com.tapandlearn.feature.lookup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.TapAndLearnApplication
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.domain.model.DictionaryEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMeaningBottomSheet(
    entry: DictionaryEntry,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val coroutineScope = rememberCoroutineScope()
    var isSaved by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Word, Audio & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = entry.lemma,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )

                    // Audio Pronunciation TTS Button
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { app.ttsManager.speak(entry.lemma) },
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDismiss() },
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Phonetic IPA
            Text(
                text = entry.phonetic,
                fontSize = 16.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )

            // Part of Speech
            Text(
                text = entry.partOfSpeech,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )

            // Numbered Senses & Hindi Meanings
            entry.senses.forEach { sense ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${sense.senseNumber}.  ${sense.englishDefinition}",
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "     ${sense.hindiMeaning}",
                        fontSize = 15.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Example Card
            if (entry.exampleSentence.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "Example",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        val annotatedSentence = buildAnnotatedString {
                            val pattern = Regex(Regex.escape(entry.lemma), RegexOption.IGNORE_CASE)
                            val match = pattern.find(entry.exampleSentence)
                            if (match != null) {
                                val start = match.range.first
                                val end = match.range.last + 1
                                append(entry.exampleSentence.substring(0, start))
                                withStyle(SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                                    append(entry.exampleSentence.substring(start, end))
                                }
                                append(entry.exampleSentence.substring(end))
                            } else {
                                append(entry.exampleSentence)
                            }
                        }

                        Text(
                            text = annotatedSentence,
                            fontSize = 14.sp,
                            color = Color(0xFF334155),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row: Favorite, Save Word
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Favorite Button
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { isFavorite = !isFavorite },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFavorite) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isFavorite) Color(0xFFFECDD3) else Color(0xFFE2E8F0))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Primary "Save Word" Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable {
                            coroutineScope.launch {
                                app.vocabularyRepository.saveWord(
                                    lemma = entry.lemma,
                                    partOfSpeech = entry.partOfSpeech,
                                    phonetic = entry.phonetic,
                                    definition = entry.senses.firstOrNull()?.englishDefinition ?: "",
                                    hindiMeaning = entry.senses.firstOrNull()?.hindiMeaning ?: "",
                                    example = entry.exampleSentence
                                )
                                isSaved = true
                                onSaveSuccess()
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSaved) AccentGreen else PrimaryBlue
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isSaved) "Saved to Vocabulary" else "Save Word",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // View More Outlined Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text(text = "View More", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
