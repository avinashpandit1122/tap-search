package com.tapandlearn.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.core.designsystem.*
import com.tapandlearn.domain.model.QuizQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit
) {
    val questions = remember {
        listOf(
            QuizQuestion(
                id = 1,
                word = "mechanism",
                options = listOf(
                    "A harmful effect",
                    "A system of parts working together",
                    "A type of medicine",
                    "A feeling of confidence"
                ),
                correctIndex = 1
            ),
            QuizQuestion(
                id = 2,
                word = "substance",
                options = listOf(
                    "A particular kind of matter with uniform properties",
                    "An unexpected consequence",
                    "A biological process",
                    "A mental state"
                ),
                correctIndex = 0
            ),
            QuizQuestion(
                id = 3,
                word = "therapeutic",
                options = listOf(
                    "Very difficult to understand",
                    "Causing severe pain",
                    "Relating to the healing of disease",
                    "Artificial chemical compound"
                ),
                correctIndex = 2
            )
        )
    }

    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(1) } // Default to option B to match mockup
    val currentQuestion = questions[questionIndex]

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Quiz",
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Progress Counter & Bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${questionIndex + 1} / 10",
                        fontSize = 15.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (questionIndex + 1) / 10f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Title
                Text(
                    text = "What is the meaning of \"${currentQuestion.word}\"?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    lineHeight = 30.sp
                )

                // 4 Multiple Choice Options
                currentQuestion.options.forEachIndexed { index, optionText ->
                    val letter = when (index) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> "D"
                    }

                    val isChosen = selectedOption == index
                    val isCorrect = index == currentQuestion.correctIndex

                    val borderColor = when {
                        isChosen && isCorrect -> Color(0xFF22C55E)
                        isChosen && !isCorrect -> Color(0xFFEF4444)
                        else -> Color(0xFFE2E8F0)
                    }

                    val bgColor = when {
                        isChosen && isCorrect -> Color(0xFFF0FDF4)
                        isChosen && !isCorrect -> Color(0xFFFEF2F2)
                        else -> Color.White
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable { selectedOption = index },
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "$letter.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isChosen && isCorrect) Color(0xFF16A34A) else Color(0xFF0F172A)
                            )
                            Text(
                                text = optionText,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )
                            if (isChosen && isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Controls: Previous & Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                TextButton(
                    onClick = {
                        if (questionIndex > 0) {
                            questionIndex--
                            selectedOption = null
                        }
                    },
                    enabled = questionIndex > 0
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Previous", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (questionIndex < questions.size - 1) {
                            questionIndex++
                            selectedOption = null
                        } else {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.width(130.dp).height(50.dp)
                ) {
                    Text(
                        text = if (questionIndex == questions.size - 1) "Finish" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
