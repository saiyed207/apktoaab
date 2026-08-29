package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MetricStatTile
import com.example.ui.components.formatByteSize
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryCyan
import com.example.ui.viewmodel.MainViewModel

data class PlayStep(
    val id: Int,
    val title: String,
    val description: String,
    val checklistItems: List<String>,
    val tips: String
)

data class PlayErrorTroubleshoot(
    val errorName: String,
    val cause: String,
    val resolution: String
)

@Composable
fun PlayPublishingGuideScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Checklist", "Data Safety", "AAB Splits", "Troubleshoot")
    val report by viewModel.inspectionReport.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> PublishingChecklistTab()
            1 -> DataSafetyTab(report = report)
            2 -> AabSplitsSimulatorTab(report = report)
            3 -> TroubleshootTab()
        }
    }
}

@Composable
private fun PublishingChecklistTab() {
    val completedItems = remember { mutableStateMapOf<String, Boolean>() }

    val playSteps = remember {
        listOf(
            PlayStep(
                id = 1,
                title = "1. Create App on Play Console",
                description = "Set up initial app metadata and default language on play.google.com/console",
                checklistItems = listOf(
                    "Enter App Name & Default Language",
                    "Choose App or Game category",
                    "Select Free or Paid pricing model",
                    "Accept Developer Program Policies & US Export Laws"
                ),
                tips = "Tip: Choose a distinct app name that matches your app's core branding."
            ),
            PlayStep(
                id = 2,
                title = "2. Store Presence & Graphic Assets",
                description = "Upload mandatory store listings, icons, and visual graphics",
                checklistItems = listOf(
                    "App Icon: Exactly 512 x 512 px PNG (up to 1MB)",
                    "Feature Graphic: Exactly 1024 x 500 px JPEG/PNG",
                    "Phone Screenshots: Minimum 2 (16:9 or 9:16 aspect ratio)",
                    "Short Description: Max 80 characters",
                    "Full Description: Max 4000 characters with feature highlights"
                ),
                tips = "Tip: High-contrast screenshot mockups boost conversion rates by up to 25%."
            ),
            PlayStep(
                id = 3,
                title = "3. Policy & App Content Declarations",
                description = "Complete mandatory compliance questionnaires before release",
                checklistItems = listOf(
                    "Provide a valid HTTPS Privacy Policy URL",
                    "Complete Content Rating IARC questionnaire",
                    "Declare Target Audience (e.g. 13+, 18+)",
                    "Declare Ads status (contains ads or no ads)",
                    "Submit Data Safety questionnaire using our auto-generator tab"
                ),
                tips = "Tip: If your app uses Location or Camera permissions, explicitly disclose them in Data Safety."
            ),
            PlayStep(
                id = 4,
                title = "4. Release Tracks & AAB Bundle Upload",
                description = "Upload the converted .aab bundle to internal, closed or production tracks",
                checklistItems = listOf(
                    "Enable Google Play App Signing (auto-manages key delivery)",
                    "Upload the converted .aab file from this app",
                    "Enter Release Name (e.g. 1.0.0 - Initial Production Release)",
                    "Write user-facing Release Notes / What's New",
                    "Start staged rollout (e.g. 10% -> 50% -> 100%) or full rollout"
                ),
                tips = "Tip: Always test on Internal Testing track first with 1-2 devices before sending to review."
            )
        )
    }

    val totalItems = playSteps.sumOf { it.checklistItems.size }
    val completedCount = completedItems.values.count { it }
    val progress = if (totalItems > 0) completedCount.toFloat() / totalItems else 0f

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play Launch Readiness",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedCount / $totalItems Done (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = AccentEmerald,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }

        items(playSteps) { step ->
            StepCard(
                step = step,
                completedMap = completedItems,
                onToggle = { key, isChecked -> completedItems[key] = isChecked }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepCard(
    step: PlayStep,
    completedMap: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    step.checklistItems.forEach { itemText ->
                        val isChecked = completedMap[itemText] == true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(itemText, !isChecked) }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onToggle(itemText, it) }
                            )
                            Text(
                                text = itemText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            text = step.tips,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryCyan,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DataSafetyTab(report: com.example.data.parser.ApkInspectionReport?) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Play Console Data Safety Generator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Google Play requires all apps to declare data collection, data sharing, and encryption practices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val permissions = report?.permissions ?: emptyList()

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Data Collection & Sharing Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Does your app collect or share any required user data? -> Select 'Yes' if you use analytics, ads, or account sync; otherwise 'No'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Is all user data encrypted in transit? -> Select 'Yes' (Mandatory HTTPS).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Do you provide a way for users to request data deletion? -> Select 'Yes' if user accounts exist.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Auto-Detected Permission Disclosures",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (permissions.any { it.contains("INTERNET") }) {
                        DataSafetyItem(
                            title = "Network / Internet Access",
                            status = "Required for API calls, image loading, or cloud data.",
                            category = "App Info & Performance -> Network Diagnostics"
                        )
                    }

                    if (permissions.any { it.contains("LOCATION") }) {
                        DataSafetyItem(
                            title = "Location (Approximate / Precise)",
                            status = "Declare under: Location -> Approximate/Precise Location (Purpose: App functionality).",
                            category = "Location Data"
                        )
                    }

                    if (permissions.any { it.contains("CAMERA") }) {
                        DataSafetyItem(
                            title = "Camera & Media",
                            status = "Declare under: Photos and Videos -> Photos (Purpose: In-app capture).",
                            category = "Media Content"
                        )
                    }

                    if (permissions.none { it.contains("LOCATION") || it.contains("CAMERA") }) {
                        Text(
                            text = "No high-risk sensitive privacy permissions detected in this build.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentEmerald
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DataSafetyItem(title: String, status: String, category: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PrimaryCyan)
            Text(text = "Category: $category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = status, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AabSplitsSimulatorTab(report: com.example.data.parser.ApkInspectionReport?) {
    val apkSize = report?.fileSizeBytes ?: 15_000_000L
    val arm64Split = (apkSize * 0.55).toLong()
    val armeabiSplit = (apkSize * 0.52).toLong()
    val savingsPercent = 35

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Google Play Dynamic Delivery Simulator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Google Play uses your .aab to generate tailored APK splits for each user's device architecture, screen density, and language, drastically cutting download sizes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatTile(
                    title = "Universal APK",
                    value = formatByteSize(apkSize),
                    icon = Icons.Default.Public,
                    accentColor = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricStatTile(
                    title = "User Split APK",
                    value = formatByteSize(arm64Split),
                    icon = Icons.Default.Speed,
                    accentColor = AccentEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Dynamic Delivery Split Breakdowns",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    SplitRow(device = "Flagship (Pixel / Galaxy arm64-v8a, xxhdpi, en)", originalSize = apkSize, splitSize = arm64Split, savings = 36)
                    SplitRow(device = "Budget (arm7a, hdpi, localized)", originalSize = apkSize, splitSize = armeabiSplit, savings = 38)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SplitRow(device: String, originalSize: Long, splitSize: Long, savings: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = device, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${formatByteSize(originalSize)} → ${formatByteSize(splitSize)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "-$savings% download size", style = MaterialTheme.typography.labelSmall, color = AccentEmerald, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TroubleshootTab() {
    var searchQuery by remember { mutableStateOf("") }

    val issues = listOf(
        PlayErrorTroubleshoot(
            errorName = "Target SDK Too Low (Requires API 34+)",
            cause = "Google Play Console rejects apps targeting older Android API versions.",
            resolution = "Set targetSdk = 34 or 35 in app/build.gradle.kts and re-run converter."
        ),
        PlayErrorTroubleshoot(
            errorName = "App is not 64-bit Compliant",
            cause = "APK has 32-bit native libraries (armeabi-v7a/x86) without corresponding 64-bit (arm64-v8a/x86_64) binaries.",
            resolution = "Compile native NDK/C++ libraries with ndk.abiFilters 'arm64-v8a', 'armeabi-v7a'."
        ),
        PlayErrorTroubleshoot(
            errorName = "App is Debuggable",
            cause = "android:debuggable='true' is present in AndroidManifest.xml or debug build type.",
            resolution = "Use release build type with debuggable=false."
        ),
        PlayErrorTroubleshoot(
            errorName = "Version Code Already Used",
            cause = "Play Console requires strictly increasing versionCode numbers for every upload.",
            resolution = "Increment versionCode in build.gradle.kts (e.g. 101 -> 102)."
        ),
        PlayErrorTroubleshoot(
            errorName = "Upload Key Certificate Mismatch",
            cause = "The AAB was signed with a keystore that does not match the Google Play App Signing upload key.",
            resolution = "Use the Signing Keys tab to generate/export the original upload certificate or request key reset on Play Console."
        )
    )

    val filtered = issues.filter {
        it.errorName.contains(searchQuery, ignoreCase = true) ||
                it.cause.contains(searchQuery, ignoreCase = true) ||
                it.resolution.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Play Console Errors...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null) }
            )
        }

        items(filtered) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.errorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentRose
                    )
                    Text(
                        text = "Cause: ${item.cause}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentEmerald.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Fix: ${item.resolution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentEmerald,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
