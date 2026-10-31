package com.tapandlearn.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapandlearn.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Appearance
            item {
                SettingsSection(title = "Appearance") {
                    SettingsRowItem(
                        icon = Icons.Default.Brightness4,
                        label = "Theme",
                        value = "System"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowItem(
                        icon = Icons.Default.FormatSize,
                        label = "Text Size",
                        value = "Medium"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowItem(
                        icon = Icons.Default.MenuBook,
                        label = "Reading Mode",
                        value = "Page Scroll"
                    )
                }
            }

            // Section 2: Language
            item {
                SettingsSection(title = "Language") {
                    SettingsRowItem(
                        icon = Icons.Default.Translate,
                        label = "Dictionary Language",
                        value = "English → Hindi"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        label = "App Language",
                        value = "English"
                    )
                }
            }

            // Section 3: Audio
            item {
                SettingsSection(title = "Audio") {
                    SettingsRowItem(
                        icon = Icons.Default.VolumeUp,
                        label = "Voice",
                        value = "Default (Offline)"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowItem(
                        icon = Icons.Default.Speed,
                        label = "Read Aloud Speed",
                        value = "Normal"
                    )
                }
            }

            // Section 4: Data & Storage
            item {
                SettingsSection(title = "Data & Storage") {
                    SettingsRowItem(
                        icon = Icons.Default.FolderShared,
                        label = "Manage Documents",
                        value = ""
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SettingsRowItem(
                        icon = Icons.Default.FileDownload,
                        label = "Export Vocabulary (CSV/JSON)",
                        value = ""
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF0F172A)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(22.dp))
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF0F172A))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value.isNotEmpty()) {
                Text(text = value, fontSize = 13.sp, color = TextSecondary)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
