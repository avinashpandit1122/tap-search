package com.tapandlearn.feature.flashcards

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.TapAndLearnApplication
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.domain.model.SavedWord
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TapAndLearnApplication
    val coroutineScope = rememberCoroutineScope()

    val cardList = remember {
        listOf(
            SavedWord(id = 1, lemma = "therapeutic", partOfSpeech = "adjective", phonetic = "/ˌθɛrəˈpjuːtɪk/", primaryDefinition = "Relating to the healing of disease or soothing effects.", hindiMeaning = "चिकित्सीय, स्वास्थ्यवर्धक", exampleSentence = "The warm bath had an immediate therapeutic effect."),
            SavedWord(id = 2, lemma = "mechanism", partOfSpeech = "noun", phonetic = "/ˈmɛkənɪzəm/", primaryDefinition = "A system of parts working together in a machine or process.", hindiMeaning = "कार्यप्रणाली, यंत्ररचना", exampleSentence = "Scientists studied the biological mechanism."),
            SavedWord(id = 3, lemma = "substance", partOfSpeech = "noun", phonetic = "/ˈsʌbstəns/", primaryDefinition = "A particular kind of matter with uniform properties.", hindiMeaning = "पदार्थ, द्रव्य", exampleSentence = "This medicine contains an active substance."),
            SavedWord(id = 4, lemma = "adverse", partOfSpeech = "adjective", phonetic = "/ˈædvɜːrs/", primaryDefinition = "Harmful, unfavorable or preventing success.", hindiMeaning = "प्रतिकूल, हानिकारक", exampleSentence = "The doctor warned against adverse reactions."),
            SavedWord(id = 5, lemma = "synthesize", partOfSpeech = "verb", phonetic = "/ˈsɪnθəsaɪz/", primaryDefinition = "To combine components to produce a whole.", hindiMeaning = "संश्लेषण करना", exampleSentence = "Plants synthesize carbohydrates directly.")
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    val currentCard = cardList.getOrNull(currentIndex) ?: cardList.first()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Flashcards",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress & Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentIndex + 1} / ${cardList.size}",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.clickable {}
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Review", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Flippable Flashcard
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(340.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                if (rotation <= 90f) {
                    // Front of Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Color(0xFFE2E8F0),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentCard.lemma,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                IconButton(onClick = { app.ttsManager.speak(currentCard.lemma) }) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speak",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Tap to see meaning",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Back of Card (Flipped)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = currentCard.hindiMeaning,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = HindiBlue,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = currentCard.primaryDefinition,
                                fontSize = 15.sp,
                                color = Color(0xFF334155),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            if (currentCard.exampleSentence.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Text(
                                    text = "\"${currentCard.exampleSentence}\"",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Rating Actions: "Not Known" (Red) & "Known" (Green)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Not Known
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                coroutineScope.launch {
                                    app.vocabularyRepository.recordReview(currentCard.id, false)
                                    isFlipped = false
                                    currentIndex = (currentIndex + 1) % cardList.size
                                }
                            },
                        shape = CircleShape,
                        color = Color(0xFFEF4444),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Not Known", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Text(text = "Not Known", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }

                // Known
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                coroutineScope.launch {
                                    app.vocabularyRepository.recordReview(currentCard.id, true)
                                    isFlipped = false
                                    currentIndex = (currentIndex + 1) % cardList.size
                                }
                            },
                        shape = CircleShape,
                        color = Color(0xFF22C55E),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Known", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Text(text = "Known", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }
            }
        }
    }
}
