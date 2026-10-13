package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: ScoutViewModel,
    onSampleMatchCreated: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State collections
    val allMatches by viewModel.allMatches.collectAsState()
    val totalEvents by viewModel.totalEventsCount.collectAsState()
    val storageSize by viewModel.storageSizeMb.collectAsState()

    val scoutNamePref by viewModel.scoutName.collectAsState()
    var scoutNameInput by remember { mutableStateOf("") }
    LaunchedEffect(scoutNamePref) {
        scoutNameInput = scoutNamePref
    }

    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val largerText by viewModel.largerText.collectAsState()
    val isAiSummaryEnabled by viewModel.isAiSummaryEnabled.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
            .testTag("settings_screen")
    ) {
        ScoutTopBar(title = "Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            // ═══════════════════════════════════════════════
            // SECTION: Scout Info
            // ═══════════════════════════════════════════════
            SectionHeader(title = "Scout Info")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_scout_info"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label "Scout Name"
                Text(
                    text = "Scout Name",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoutTextPrimary,
                    modifier = Modifier.testTag("scout_name_label")
                )

                // Input 50dp
                OutlinedTextField(
                    value = scoutNameInput,
                    onValueChange = {
                        scoutNameInput = it
                        viewModel.setScoutName(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("scout_name_input"),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = ScoutTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ScoutSurface,
                        unfocusedContainerColor = ScoutSurface,
                        focusedBorderColor = ScoutPrimaryAction,
                        unfocusedBorderColor = ScoutDivider
                    ),
                    placeholder = {
                        Text(
                            text = "Enter scout full name",
                            fontSize = 14.sp,
                            color = ScoutTextHint
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                // Helper "Used in reports and exports"
                Text(
                    text = "Used in reports and exports",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = ScoutTextHint,
                    modifier = Modifier.testTag("scout_name_helper")
                )
            }

            // ═══════════════════════════════════════════════
            // SECTION: Accessibility
            // ═══════════════════════════════════════════════
            SectionHeader(title = "Accessibility")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_accessibility"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                border = BorderStroke(1.dp, ScoutDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingSwitchRow(
                        title = "Reduce motion",
                        helper = "Minimize animations",
                        checked = reduceMotion,
                        onCheckedChange = { viewModel.setReduceMotion(it) },
                        testTag = "reduce_motion_switch"
                    )

                    HorizontalDivider(color = ScoutDivider, thickness = 1.dp)

                    SettingSwitchRow(
                        title = "High contrast mode",
                        helper = "Bolder borders, pure black text on white",
                        checked = highContrast,
                        onCheckedChange = { viewModel.setHighContrast(it) },
                        testTag = "high_contrast_switch"
                    )

                    HorizontalDivider(color = ScoutDivider, thickness = 1.dp)

                    SettingSwitchRow(
                        title = "Larger text",
                        helper = "Increase all text by 20%",
                        checked = largerText,
                        onCheckedChange = { viewModel.setLargerText(it) },
                        testTag = "larger_text_switch"
                    )
                }
            }

            // ═══════════════════════════════════════════════
            // SECTION: AI Summary
            // ═══════════════════════════════════════════════
            SectionHeader(title = "AI Summary")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_ai_summary"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                border = BorderStroke(1.dp, ScoutDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SettingSwitchRow(
                        title = "Enable AI Summary",
                        helper = "Requires internet. Off by default.",
                        checked = isAiSummaryEnabled,
                        onCheckedChange = { viewModel.setAiSummaryEnabled(it) },
                        testTag = "enable_ai_summary_switch"
                    )
                }
            }

            // ═══════════════════════════════════════════════
            // SECTION: Data Management
            // ═══════════════════════════════════════════════
            SectionHeader(title = "Data Management")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_data_management"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                border = BorderStroke(1.dp, ScoutDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DataInfoRow(label = "Total Matches", value = "${allMatches.size}", testTag = "total_matches_info")
                    HorizontalDivider(color = ScoutDivider, thickness = 1.dp)

                    DataInfoRow(label = "Total Events", value = "$totalEvents", testTag = "total_events_info")
                    HorizontalDivider(color = ScoutDivider, thickness = 1.dp)

                    DataInfoRow(label = "Storage Used", value = storageSize, testTag = "storage_used_info")

                    HorizontalDivider(color = ScoutDivider, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    // Buttons stacked 48dp:
                    // 1. "Export All Data (JSON)" outlined
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val json = viewModel.exportAllDataJson()
                                    val file = File(context.cacheDir, "RwandaScout_Backup_${System.currentTimeMillis()}.json")
                                    file.writeText(json)

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "Rwanda Football Scout Backup")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export All Data (JSON)"))
                                    Toast.makeText(context, "Exported ${allMatches.size} matches", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_all_json_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, ScoutPrimaryAction),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ScoutPrimaryAction)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export All Data",
                                tint = ScoutPrimaryAction,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Export All Data (JSON)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScoutPrimaryAction
                            )
                        }
                    }

                    // 2. "Import Data (JSON)" outlined
                    OutlinedButton(
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("import_data_json_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, ScoutPrimaryAction),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ScoutPrimaryAction)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Import Data",
                                tint = ScoutPrimaryAction,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Import Data (JSON)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScoutPrimaryAction
                            )
                        }
                    }

                    // 3. "Delete All Data" filled
                    Button(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_all_data_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScoutErrorDanger,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete All Data",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Delete All Data",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════
            // SECTION: About
            // ═══════════════════════════════════════════════
            SectionHeader(title = "About")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("section_about"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                border = BorderStroke(1.dp, ScoutDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Rwanda Football Scout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScoutTextPrimary,
                        modifier = Modifier.testTag("about_app_name")
                    )

                    Text(
                        text = "Version 1.0.0",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = ScoutTextSecondary,
                        modifier = Modifier.testTag("about_app_version")
                    )

                    Text(
                        text = "Local-only app. Data stays on this phone.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ScoutTextSecondary,
                        modifier = Modifier.testTag("about_local_note")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // "Privacy" 64dp outlined → opens simple text dialog explaining no data leaves the device
                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("about_privacy_btn"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, Color(0xFF4A5568)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A5568))
                    ) {
                        Text(
                            text = "Privacy",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A5568)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ═══════════════════════════════════════════════
    // DELETE ALL DATA CONFIRM DIALOG
    // - Title 32sp Bold "Delete all data?"
    // - Body 20sp "This will delete all matches and free up storage. Cannot be undone."
    // - [CANCEL outlined] [DELETE ALL filled #C53030] 64dp each
    // - On confirm: Room.clearAllTables() → toast "All data deleted"
    // ═══════════════════════════════════════════════
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete all data?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E),
                    modifier = Modifier.testTag("delete_all_dialog_title")
                )
            },
            text = {
                Text(
                    text = "This will delete all matches and free up storage. Cannot be undone.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568),
                    lineHeight = 28.sp,
                    modifier = Modifier.testTag("delete_all_dialog_body")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDialog = false
                        viewModel.clearAllData {
                            Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .testTag("dialog_confirm_delete_all_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC53030),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "DELETE ALL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAllDialog = false },
                    modifier = Modifier
                        .height(64.dp)
                        .testTag("dialog_cancel_delete_all_btn"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, Color(0xFF4A5568)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A5568))
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A5568)
                    )
                }
            }
        )
    }

    // ═══════════════════════════════════════════════
    // PRIVACY POLICY DIALOG
    // ═══════════════════════════════════════════════
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Privacy Notice",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E),
                    modifier = Modifier.testTag("privacy_dialog_title")
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rwanda Football Scout is an offline-first match logging application designed for local privacy.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF0F1A2E),
                        lineHeight = 28.sp
                    )
                    Text(
                        text = "• All rosters, matches, tactical pitch taps, events, and scouting notes are stored strictly on this phone's Room database.",
                        fontSize = 18.sp,
                        color = Color(0xFF4A5568),
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "• No telemetry, user profiling, or personal data is collected or transmitted to external servers.",
                        fontSize = 18.sp,
                        color = Color(0xFF4A5568),
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "• When AI Summary is explicitly enabled by you in Settings, only non-personal match scores and event statistics are sent securely to generate the broadcast report.",
                        fontSize = 18.sp,
                        color = Color(0xFF4A5568),
                        lineHeight = 26.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("close_privacy_dialog_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B7EB8),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Close",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        )
    }

    // ═══════════════════════════════════════════════
    // IMPORT DATA JSON DIALOG
    // ═══════════════════════════════════════════════
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Import Data (JSON)",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F1A2E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Paste a JSON backup string previously exported from Rwanda Football Scout:",
                        fontSize = 18.sp,
                        color = Color(0xFF4A5568)
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("import_json_input"),
                        placeholder = { Text("Paste JSON data here...", fontSize = 18.sp) },
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isBlank()) {
                            Toast.makeText(context, "Please paste valid JSON", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.importDataJson(importJsonText) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) showImportDialog = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("confirm_import_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B7EB8),
                        contentColor = Color.White
                    )
                ) {
                    Text("Import Data", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showImportDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, Color(0xFF4A5568))
                ) {
                    Text("Cancel", fontSize = 20.sp, color = Color(0xFF4A5568))
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════
// REUSABLE COMPONENTS
// ═══════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = ScoutTextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
            .testTag("section_header_${title.lowercase().replace(" ", "_")}")
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    helper: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("row_$testTag"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScoutTextPrimary
            )
            Text(
                text = helper,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = ScoutTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ScoutPrimaryAction,
                checkedTrackColor = ScoutPrimaryAction.copy(alpha = 0.5f),
                uncheckedThumbColor = ScoutTextSecondary,
                uncheckedTrackColor = ScoutDivider
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DataInfoRow(label: String, value: String, testTag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = ScoutTextSecondary
        )
        Text(
            text = value,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = ScoutTextPrimary
        )
    }
}
