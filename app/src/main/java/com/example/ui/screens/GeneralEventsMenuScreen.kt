package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.EventEntity
import com.example.data.model.MatchEntity
import com.example.ui.theme.ScoutBackground
import com.example.ui.theme.ScoutDivider
import com.example.ui.theme.ScoutErrorDanger
import com.example.ui.theme.ScoutPrimaryAction
import com.example.ui.theme.ScoutSuccessLive
import com.example.ui.theme.ScoutSurface
import com.example.ui.theme.ScoutTeamSwatches
import com.example.ui.theme.ScoutTextHint
import com.example.ui.theme.ScoutTextPrimary
import com.example.ui.theme.ScoutTextSecondary
import com.example.ui.theme.ScoutTypographyBodyL
import com.example.ui.theme.ScoutTypographyTitleXL
import com.example.ui.theme.ScoutTypographyLabelL
import com.example.ui.theme.ScoutTypographyLabelM
import com.example.ui.theme.ScoutTypographyTitleL
import com.example.ui.theme.ScoutWarning
import com.example.ui.viewmodel.ScoutViewModel
import java.io.File

enum class GeneralMenuScreenType {
    MAIN_MENU,
    MATCH_STATUS,
    SUBSTITUTION,
    INJURY,
    ADDED_TIME,
    CONDITIONS,
    ATTENDANCE,
    INTERRUPTION_REASONS,
    UNDO,
    TEAM_COLORS,
    REQUEST_ASSISTANCE
}

data class GeneralMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val screenType: GeneralMenuScreenType? = null
)

@Composable
fun GeneralEventsContainer(
    viewModel: ScoutViewModel,
    match: MatchEntity,
    events: List<EventEntity>,
    onDismiss: () -> Unit,
    onTriggerInterruption: (String) -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(GeneralMenuScreenType.MAIN_MENU) }

    // Camera snapshot launcher
    val snapshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val dir = File(context.filesDir, "snapshots")
                if (!dir.exists()) dir.mkdirs()
                val photoFile = File(dir, "snapshot_${System.currentTimeMillis()}.jpg")
                photoFile.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.logSnapshot(photoFile.absolutePath) {
                    Toast.makeText(context, "Snapshot saved: ${photoFile.name}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving snapshot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            snapshotLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required for snapshots", Toast.LENGTH_LONG).show()
        }
    }

    BackHandler {
        if (currentScreen != GeneralMenuScreenType.MAIN_MENU) {
            currentScreen = GeneralMenuScreenType.MAIN_MENU
        } else {
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScoutBackground
    ) {
        when (currentScreen) {
            GeneralMenuScreenType.MAIN_MENU -> {
                GeneralEventsMainMenu(
                    match = match,
                    onDismiss = onDismiss,
                    onSelectScreen = { screenType ->
                        currentScreen = screenType
                    },
                    onTriggerSnapshot = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            snapshotLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onToggleSides = {
                        viewModel.toggleSides { newLeft ->
                            Toast.makeText(context, "Sides swapped: Left is now $newLeft", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                )
            }
            GeneralMenuScreenType.MATCH_STATUS -> {
                MatchStatusSubMenu(
                    currentPeriod = match.currentPeriod,
                    onStatusSelected = { option ->
                        viewModel.updateMatchStatusPeriod(option) {
                            Toast.makeText(context, "Match status updated: $option", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.SUBSTITUTION -> {
                SubstitutionWorkflow(
                    homeTeamName = match.homeTeam,
                    awayTeamName = match.awayTeam,
                    onConfirmSub = { team, count, isDelayed ->
                        viewModel.logSubstitution(team, count, isDelayed) {
                            val subLabel = if (isDelayed) "Delayed Sub" else "Sub"
                            Toast.makeText(context, "$team $subLabel logged: $count player(s)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.INJURY -> {
                InjuryWorkflow(
                    homeTeamName = match.homeTeam,
                    awayTeamName = match.awayTeam,
                    onConfirmInjury = { team ->
                        viewModel.logInjury(team) {
                            Toast.makeText(context, "Injury recorded for $team", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onConfirmReturn = { team ->
                        viewModel.logInjuryReturn(team) {
                            Toast.makeText(context, "Injury return recorded for $team", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.ADDED_TIME -> {
                AddedTimeWorkflow(
                    currentPeriod = match.currentPeriod,
                    onSelectAddedTime = { minutes, isFirstHalf ->
                        viewModel.saveAddedTime(minutes, isFirstHalf) {
                            val half = if (isFirstHalf) "1st Half" else "2nd Half"
                            Toast.makeText(context, "+$minutes min added time set ($half)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.CONDITIONS -> {
                ConditionsWorkflow(
                    match = match,
                    onSaveConditions = { weather, intensity, fog, wind, pitch, homeCol, awayCol ->
                        viewModel.updateConditions(weather, intensity, fog, wind, pitch, homeCol, awayCol) {
                            Toast.makeText(context, "Match conditions updated", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.ATTENDANCE -> {
                AttendanceWorkflow(
                    currentAttendance = match.attendance,
                    onSaveAttendance = { num ->
                        viewModel.saveAttendance(num) {
                            Toast.makeText(context, "Attendance locked at $num spectators", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.INTERRUPTION_REASONS -> {
                InterruptionReasonMenu(
                    onReasonSelected = { reason ->
                        onTriggerInterruption(reason)
                        onDismiss()
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.UNDO -> {
                UndoWorkflow(
                    events = events,
                    onUndoEvent = { eventId ->
                        viewModel.deleteEvent(eventId) {
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.TEAM_COLORS -> {
                TeamColorsWorkflow(
                    currentHome = match.homeColor ?: "Blue",
                    currentAway = match.awayColor ?: "Red",
                    homeTeamName = match.homeTeam,
                    awayTeamName = match.awayTeam,
                    onSaveColors = { h, a ->
                        viewModel.updateTeamColors(h, a) {
                            Toast.makeText(context, "Team colors saved", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
            GeneralMenuScreenType.REQUEST_ASSISTANCE -> {
                RequestAssistanceWorkflow(
                    onConfirm = {
                        viewModel.logRequestAssistance {
                            Toast.makeText(context, "Saved locally", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    },
                    onBack = { currentScreen = GeneralMenuScreenType.MAIN_MENU }
                )
            }
        }
    }
}

@Composable
fun GeneralEventsMainMenu(
    match: MatchEntity,
    onDismiss: () -> Unit,
    onSelectScreen: (GeneralMenuScreenType) -> Unit,
    onTriggerSnapshot: () -> Unit,
    onToggleSides: () -> Unit
) {
    val menuItems = listOf(
        GeneralMenuItem("status", "1. Match status", Icons.Default.AccessTime, GeneralMenuScreenType.MATCH_STATUS),
        GeneralMenuItem("sub", "2. Substitution", Icons.Default.SwapCalls, GeneralMenuScreenType.SUBSTITUTION),
        GeneralMenuItem("injury", "3. Injury", Icons.Default.LocalHospital, GeneralMenuScreenType.INJURY),
        GeneralMenuItem("added_time", "4. Added/Injury time", Icons.Default.MoreTime, GeneralMenuScreenType.ADDED_TIME),
        GeneralMenuItem("snapshot", "5. Snapshot", Icons.Default.PhotoCamera, null),
        GeneralMenuItem("conditions", "6. Conditions", Icons.Default.WbSunny, GeneralMenuScreenType.CONDITIONS),
        GeneralMenuItem("attendance", "7. Attendance", Icons.Default.Groups, GeneralMenuScreenType.ATTENDANCE),
        GeneralMenuItem("interruption", "8. Temporary interruption", Icons.Default.PanTool, GeneralMenuScreenType.INTERRUPTION_REASONS),
        GeneralMenuItem("undo", "9. Undo", Icons.AutoMirrored.Filled.Undo, GeneralMenuScreenType.UNDO),
        GeneralMenuItem("colors", "10. Team colors", Icons.Default.Palette, GeneralMenuScreenType.TEAM_COLORS),
        GeneralMenuItem("sides", "11. Change sides (${match.leftTeamSide ?: "HOME"} on left)", Icons.Default.SwapHoriz, null),
        GeneralMenuItem("assistance", "12. Request assistance", Icons.Default.Help, GeneralMenuScreenType.REQUEST_ASSISTANCE)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Accessible Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("general_menu_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ScoutTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "General Events Menu",
                        style = ScoutTypographyTitleXL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = ScoutTextPrimary
                    )
                    Text(
                        text = "${match.homeTeam} vs ${match.awayTeam}",
                        style = ScoutTypographyBodyL.copy(color = ScoutTextSecondary)
                    )
                }
            }

            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                color = ScoutDivider,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ScoutTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Close", style = ScoutTypographyLabelM.copy(fontSize = 13.sp, color = ScoutTextPrimary, fontWeight = FontWeight.Bold))
                }
            }
        }

        // List of Menu Items (each 52dp tall, clean border, 10dp radius)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("general_menu_row_${item.id}")
                        .clickable {
                            when (item.id) {
                                "snapshot" -> onTriggerSnapshot()
                                "sides" -> onToggleSides()
                                else -> item.screenType?.let { onSelectScreen(it) }
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(1.dp, ScoutDivider),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.title,
                            style = ScoutTypographyBodyL.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ScoutTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open",
                            tint = ScoutTextHint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// 1. MATCH STATUS SUBMENU (context-aware)
// ═══════════════════════════════════════════════
@Composable
fun MatchStatusSubMenu(
    currentPeriod: String,
    onStatusSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val options = when (currentPeriod) {
        "FIRST_HALF" -> listOf("Halftime")
        "HALF_TIME" -> listOf("2nd Half")
        "SECOND_HALF" -> listOf("Awaiting OT", "Ended")
        "AWAITING_OT" -> listOf("1st Half OT")
        "FIRST_HALF_OT" -> listOf("Halftime OT")
        "HALF_TIME_OT" -> listOf("2nd Half OT")
        "SECOND_HALF_OT" -> listOf("Awaiting penalties", "Ended")
        "AWAITING_PENALTIES" -> listOf("Penalty shooting")
        "NOT_STARTED" -> listOf("1st Half")
        else -> listOf("Ended", "Restart 1st Half")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Match Status", onBack = onBack)

        Text(
            text = "Current Status: $currentPeriod",
            style = ScoutTypographyTitleL.copy(fontSize = 22.sp, color = ScoutTextSecondary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(options) { opt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .testTag("status_option_${opt.replace(" ", "_").lowercase()}")
                        .clickable { onStatusSelected(opt) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(2.dp, ScoutPrimaryAction)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = opt,
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = opt,
                            style = ScoutTypographyTitleL.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F1A2E)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// 2. SUBSTITUTION WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun SubstitutionWorkflow(
    homeTeamName: String,
    awayTeamName: String,
    onConfirmSub: (team: String, count: Int, isDelayed: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var selectedTeam by remember { mutableStateOf<String?>(null) }
    var selectedPlayerCount by remember { mutableIntStateOf(1) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Substitution", onBack = onBack)

        if (selectedTeam == null) {
            Text(
                text = "Select Team for Substitution:",
                style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 220dp cards for Home and Away
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Home Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(220.dp)
                        .testTag("sub_team_home_card")
                        .clickable { selectedTeam = "HOME" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(4.dp, Color(0xFF0B7EB8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = "Home", tint = Color(0xFF0B7EB8), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = homeTeamName,
                            style = ScoutTypographyTitleL.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B7EB8)),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("HOME TEAM", style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary))
                    }
                }

                // Away Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(220.dp)
                        .testTag("sub_team_away_card")
                        .clickable { selectedTeam = "AWAY" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    border = BorderStroke(4.dp, Color(0xFFC53030)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = "Away", tint = Color(0xFFC53030), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = awayTeamName,
                            style = ScoutTypographyTitleL.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC53030)),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("AWAY TEAM", style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary))
                    }
                }
            }
        } else {
            val teamName = if (selectedTeam == "HOME") homeTeamName else awayTeamName
            Text(
                text = "Team: $teamName ($selectedTeam)",
                style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Number of players to substitute (1 - 5):",
                style = ScoutTypographyBodyL.copy(fontSize = 20.sp, color = ScoutTextSecondary),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Number of players 1-5 (each 80dp button 28sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (1..5).forEach { count ->
                    val isSelected = selectedPlayerCount == count
                    Surface(
                        onClick = {
                            selectedPlayerCount = count
                            showConfirmDialog = true
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .testTag("sub_player_count_$count"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) ScoutPrimaryAction else ScoutSurface,
                        border = BorderStroke(3.dp, if (isSelected) ScoutPrimaryAction else ScoutDivider),
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$count",
                                style = ScoutTypographyTitleL.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF0F1A2E)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { selectedTeam = null },
                colors = ButtonDefaults.buttonColors(containerColor = ScoutDivider),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Change Team", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = ScoutTextPrimary))
            }
        }

        // Confirm Dialog [CANCEL] [DELAYED SUB #B7791F] [CONFIRM #1B7F3A]
        if (showConfirmDialog && selectedTeam != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "Confirm Substitution",
                        style = ScoutTypographyTitleL.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "Record substitution for ${if (selectedTeam == "HOME") homeTeamName else awayTeamName} ($selectedTeam) with $selectedPlayerCount player(s)?",
                        style = ScoutTypographyBodyL.copy(fontSize = 20.sp)
                    )
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                onConfirmSub(selectedTeam!!, selectedPlayerCount, true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7791F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("sub_delayed_btn")
                        ) {
                            Text("DELAYED SUB", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = Color.White))
                        }

                        Button(
                            onClick = {
                                showConfirmDialog = false
                                onConfirmSub(selectedTeam!!, selectedPlayerCount, false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B7F3A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("sub_confirm_btn")
                        ) {
                            Text("CONFIRM", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = Color.White))
                        }
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutDivider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("CANCEL", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = ScoutTextPrimary))
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════
// 3. INJURY WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun InjuryWorkflow(
    homeTeamName: String,
    awayTeamName: String,
    onConfirmInjury: (team: String) -> Unit,
    onConfirmReturn: (team: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTeam by remember { mutableStateOf("HOME") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isReturnFlow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Player Injury & Return", onBack = onBack)

        Text(
            text = "Select Team:",
            style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick = { selectedTeam = "HOME" },
                modifier = Modifier
                    .weight(1f)
                    .height(88.dp)
                    .testTag("injury_team_home"),
                shape = RoundedCornerShape(16.dp),
                color = if (selectedTeam == "HOME") Color(0xFFEBF8FF) else ScoutSurface,
                border = BorderStroke(3.dp, if (selectedTeam == "HOME") Color(0xFF0B7EB8) else ScoutDivider)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$homeTeamName (HOME)",
                        style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B7EB8))
                    )
                }
            }

            Surface(
                onClick = { selectedTeam = "AWAY" },
                modifier = Modifier
                    .weight(1f)
                    .height(88.dp)
                    .testTag("injury_team_away"),
                shape = RoundedCornerShape(16.dp),
                color = if (selectedTeam == "AWAY") Color(0xFFFFF5F5) else ScoutSurface,
                border = BorderStroke(3.dp, if (selectedTeam == "AWAY") Color(0xFFC53030) else ScoutDivider)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$awayTeamName (AWAY)",
                        style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action 1: Record Injury
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .testTag("record_injury_btn")
                .clickable {
                    isReturnFlow = false
                    showConfirmDialog = true
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
            border = BorderStroke(2.dp, ScoutWarning),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = "Injury", tint = ScoutWarning, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Record Player Injury", style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary))
                    Text("Logs INJURY event for $selectedTeam", style = ScoutTypographyBodyL.copy(color = ScoutTextSecondary))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 2: Return Flow (INJURY_RETURN)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .testTag("record_injury_return_btn")
                .clickable {
                    isReturnFlow = true
                    showConfirmDialog = true
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ScoutSurface),
            border = BorderStroke(2.dp, ScoutSuccessLive),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Check, contentDescription = "Return", tint = ScoutSuccessLive, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Player Returned to Pitch", style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary))
                    Text("Logs INJURY_RETURN event for $selectedTeam", style = ScoutTypographyBodyL.copy(color = ScoutTextSecondary))
                }
            }
        }

        if (showConfirmDialog) {
            val title = if (isReturnFlow) "Confirm Injury Return" else "Confirm Player Injury"
            val message = if (isReturnFlow) {
                "Confirm that the injured player from $selectedTeam has returned to play?"
            } else {
                "Record a player injury event for $selectedTeam?"
            }

            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(title, style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)) },
                text = { Text(message, style = ScoutTypographyBodyL.copy(fontSize = 20.sp)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            if (isReturnFlow) onConfirmReturn(selectedTeam) else onConfirmInjury(selectedTeam)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isReturnFlow) ScoutSuccessLive else ScoutWarning),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("injury_dialog_confirm_btn")
                    ) {
                        Text("CONFIRM", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = Color.White))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutDivider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("CANCEL", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = ScoutTextPrimary))
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════
// 4. ADDED TIME WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun AddedTimeWorkflow(
    currentPeriod: String,
    onSelectAddedTime: (minutes: Int, isFirstHalf: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val defaultIsFirstHalf = currentPeriod in listOf("FIRST_HALF", "HALF_TIME")
    var isFirstHalf by remember { mutableStateOf(defaultIsFirstHalf) }

    val options = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0) // 0 represents Unknown

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Added / Injury Time", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = { isFirstHalf = true },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("added_time_1st_half_tab"),
                shape = RoundedCornerShape(14.dp),
                color = if (isFirstHalf) ScoutPrimaryAction else ScoutSurface,
                border = BorderStroke(2.dp, if (isFirstHalf) ScoutPrimaryAction else ScoutDivider)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "1st Half Added Time",
                        style = ScoutTypographyTitleL.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isFirstHalf) Color.White else ScoutTextPrimary)
                    )
                }
            }

            Surface(
                onClick = { isFirstHalf = false },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("added_time_2nd_half_tab"),
                shape = RoundedCornerShape(14.dp),
                color = if (!isFirstHalf) ScoutPrimaryAction else ScoutSurface,
                border = BorderStroke(2.dp, if (!isFirstHalf) ScoutPrimaryAction else ScoutDivider)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "2nd Half Added Time",
                        style = ScoutTypographyTitleL.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (!isFirstHalf) Color.White else ScoutTextPrimary)
                    )
                }
            }
        }

        // List of +1 to +10 and Unknown, each 80dp row 24sp
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(options) { mins ->
                val label = if (mins == 0) "Unknown" else "+$mins min"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("added_time_row_$mins")
                        .clickable { onSelectAddedTime(mins, isFirstHalf) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(2.dp, ScoutDivider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreTime,
                            contentDescription = label,
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = ScoutTypographyTitleL.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F1A2E)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// 6. CONDITIONS WORKFLOW (weather/wind/pitch/colors)
// ═══════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConditionsWorkflow(
    match: MatchEntity,
    onSaveConditions: (weather: String, intensity: Int, fog: Boolean, wind: String, pitch: String, homeColor: String, awayColor: String) -> Unit,
    onBack: () -> Unit
) {
    var weather by remember { mutableStateOf(match.weather ?: "No rain") }
    var weatherIntensity by remember { mutableFloatStateOf((match.weatherIntensity ?: 50).toFloat()) }
    var fog by remember { mutableStateOf(match.fog) }
    var wind by remember { mutableStateOf(match.wind ?: "None") }
    var pitchCondition by remember { mutableStateOf(match.pitchCondition ?: "Firm") }
    var homeColor by remember { mutableStateOf(match.homeColor ?: "Blue") }
    var awayColor by remember { mutableStateOf(match.awayColor ?: "Red") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Match Conditions", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Weather
            Text("Weather:", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary))
            val weatherOptions = listOf(
                Pair("No rain", Icons.Default.WbSunny),
                Pair("Rain", Icons.Default.Grain),
                Pair("Snow", Icons.Default.AcUnit),
                Pair("Sleet", Icons.Default.Cloud),
                Pair("Hail", Icons.Default.Thunderstorm)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weatherOptions.forEach { (w, icon) ->
                    val isSelected = weather.equals(w, ignoreCase = true)
                    Surface(
                        onClick = { weather = w },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ScoutPrimaryAction else ScoutSurface,
                        border = BorderStroke(2.dp, if (isSelected) ScoutPrimaryAction else ScoutDivider),
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .testTag("cond_weather_$w")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = w, tint = if (isSelected) Color.White else ScoutPrimaryAction, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(w, style = ScoutTypographyLabelM.copy(fontSize = 14.sp, color = if (isSelected) Color.White else ScoutTextPrimary))
                        }
                    }
                }
            }

            // Intensity slider
            Text("Weather Intensity: ${weatherIntensity.toInt()}%", style = ScoutTypographyBodyL.copy(fontSize = 18.sp, color = ScoutTextSecondary))
            Slider(
                value = weatherIntensity,
                onValueChange = { weatherIntensity = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = ScoutPrimaryAction, activeTrackColor = ScoutPrimaryAction),
                modifier = Modifier.testTag("cond_intensity_slider")
            )

            // Fog checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { fog = !fog }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = fog,
                    onCheckedChange = { fog = it },
                    colors = CheckboxDefaults.colors(checkedColor = ScoutPrimaryAction),
                    modifier = Modifier.testTag("cond_fog_checkbox")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fog present on pitch", style = ScoutTypographyBodyL.copy(fontSize = 20.sp, color = ScoutTextPrimary))
            }

            // Wind
            Text("Wind:", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary))
            val windOptions = listOf("None", "Light", "Strong")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                windOptions.forEach { w ->
                    val isSelected = wind.equals(w, ignoreCase = true)
                    Surface(
                        onClick = { wind = w },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ScoutPrimaryAction else ScoutSurface,
                        border = BorderStroke(2.dp, if (isSelected) ScoutPrimaryAction else ScoutDivider),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("cond_wind_$w")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(w, style = ScoutTypographyTitleL.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else ScoutTextPrimary))
                        }
                    }
                }
            }

            // Pitch
            Text("Pitch Condition:", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary))
            val pitchOptions = listOf("Firm", "Soft", "Wet", "Artificial")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pitchOptions.forEach { p ->
                    val isSelected = pitchCondition.equals(p, ignoreCase = true)
                    Surface(
                        onClick = { pitchCondition = p },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ScoutPrimaryAction else ScoutSurface,
                        border = BorderStroke(2.dp, if (isSelected) ScoutPrimaryAction else ScoutDivider),
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                            .testTag("cond_pitch_$p")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(p, style = ScoutTypographyTitleL.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else ScoutTextPrimary))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveConditions(weather, weatherIntensity.toInt(), fog, wind, pitchCondition, homeColor, awayColor)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ScoutSuccessLive),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("cond_save_btn")
        ) {
            Text("SAVE CONDITIONS", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
    }
}

// ═══════════════════════════════════════════════
// 7. ATTENDANCE WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun AttendanceWorkflow(
    currentAttendance: Int?,
    onSaveAttendance: (Int) -> Unit,
    onBack: () -> Unit
) {
    var attendanceInput by remember { mutableStateOf(currentAttendance?.toString() ?: "") }
    val isLocked = currentAttendance != null && currentAttendance > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Match Attendance", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        if (isLocked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attendance_locked_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                border = BorderStroke(3.dp, ScoutSuccessLive),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = ScoutSuccessLive, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Attendance Locked", style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ScoutSuccessLive))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$currentAttendance Spectators",
                        style = ScoutTypographyTitleXL.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Attendance has been officially confirmed and locked for this match.", style = ScoutTypographyBodyL.copy(color = ScoutTextSecondary), textAlign = TextAlign.Center)
                }
            }
        } else {
            Text(
                text = "Enter Official Attendance:",
                style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Numeric input 64dp 20sp
            OutlinedTextField(
                value = attendanceInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        attendanceInput = input
                    }
                },
                placeholder = { Text("e.g. 25000", fontSize = 20.sp, color = ScoutTextHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("attendance_input_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScoutPrimaryAction,
                    unfocusedBorderColor = ScoutDivider,
                    focusedTextColor = ScoutTextPrimary,
                    unfocusedTextColor = ScoutTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val count = attendanceInput.toIntOrNull() ?: 0
                    if (count > 0) {
                        onSaveAttendance(count)
                    }
                },
                enabled = attendanceInput.isNotBlank() && (attendanceInput.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("attendance_save_btn")
            ) {
                Text("SAVE & LOCK ATTENDANCE", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

// ═══════════════════════════════════════════════
// 8. INTERRUPTION REASONS WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun InterruptionReasonMenu(
    onReasonSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val reasons = listOf(
        "Weather / Severe Storm",
        "Crowd / Pitch Invasion",
        "Medical Emergency",
        "Floodlight / Equipment Failure",
        "Referee Consultation / Incident"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Temporary Interruption", onBack = onBack)

        Text(
            text = "Select Interruption Cause (5 Options):",
            style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Reason selection (5 options 88dp each)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reasons) { reason ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .testTag("interruption_reason_${reason.take(8).lowercase().replace(" ", "_")}")
                        .clickable { onReasonSelected(reason) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScoutSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(2.dp, ScoutErrorDanger)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = reason,
                            tint = ScoutErrorDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = reason,
                            style = ScoutTypographyTitleL.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F1A2E)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// RED HAND OVERLAY FOR TEMPORARY INTERRUPTION
// ═══════════════════════════════════════════════
@Composable
fun RedHandInterruptionOverlay(
    reason: String,
    onResumeMatch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onResumeMatch() }
            .testTag("interruption_red_hand_overlay"),
        color = Color(0xF21A0505) // Rich dark-red accessible backdrop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Red Hand 96dp icon
            Icon(
                imageVector = Icons.Default.PanTool,
                contentDescription = "Match Interrupted",
                tint = Color(0xFFFF3333),
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MATCH TEMPORARILY INTERRUPTED",
                style = ScoutTypographyTitleXL.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reason,
                style = ScoutTypographyTitleL.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFD1D1)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ScoutSuccessLive,
                modifier = Modifier.height(64.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAP ANYWHERE TO RESUME MATCH",
                        style = ScoutTypographyTitleL.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// 9. UNDO WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun UndoWorkflow(
    events: List<EventEntity>,
    onUndoEvent: (Long) -> Unit,
    onBack: () -> Unit
) {
    val last10Events = events.sortedByDescending { it.createdAtMillis }.take(10)
    var eventToUndo by remember { mutableStateOf<EventEntity?>(null) }

    val confirmedNonUndoableTypes = listOf(
        "GOAL",
        "CARD",
        "CORNER",
        "PENALTY",
        "YELLOW_CARD",
        "RED_CARD"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        SubMenuHeader(title = "Undo Event", onBack = onBack)

        Text(
            text = "Last 10 Logged Events:",
            style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (last10Events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No events recorded yet", style = ScoutTypographyBodyL.copy(fontSize = 22.sp, color = ScoutTextSecondary))
            }
        } else {
            // List of last 10 events, each 80dp row with icon 32dp + label 20sp
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(last10Events) { ev ->
                    val isConfirmed = confirmedNonUndoableTypes.any { it.equals(ev.eventType, ignoreCase = true) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("undo_event_row_${ev.id}")
                            .clickable(enabled = !isConfirmed) {
                                eventToUndo = ev
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConfirmed) Color(0xFFF0F3F7) else ScoutSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isConfirmed) 0.dp else 2.dp),
                        border = BorderStroke(1.5.dp, if (isConfirmed) ScoutDivider else ScoutPrimaryAction)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isConfirmed) Icons.Default.Lock else Icons.AutoMirrored.Filled.Undo,
                                contentDescription = ev.eventType,
                                tint = if (isConfirmed) ScoutTextHint else ScoutPrimaryAction,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${ev.matchTimeDisplay}  ${ev.playerName}",
                                    style = ScoutTypographyTitleL.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isConfirmed) ScoutTextHint else Color(0xFF0F1A2E)
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Type: ${ev.eventType} (${ev.team})",
                                    style = ScoutTypographyBodyL.copy(
                                        fontSize = 15.sp,
                                        color = if (isConfirmed) ScoutTextHint else ScoutTextSecondary
                                    )
                                )
                            }

                            if (isConfirmed) {
                                Text(
                                    text = "Cannot undo",
                                    style = ScoutTypographyLabelM.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScoutTextHint
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (eventToUndo != null) {
            val ev = eventToUndo!!
            AlertDialog(
                onDismissRequest = { eventToUndo = null },
                title = { Text("Undo Event?", style = ScoutTypographyTitleL.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)) },
                text = {
                    Text(
                        "Are you sure you want to delete this event: ${ev.eventType} - ${ev.playerName} at ${ev.matchTimeDisplay}?",
                        style = ScoutTypographyBodyL.copy(fontSize = 20.sp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = ev.id
                            eventToUndo = null
                            onUndoEvent(id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutErrorDanger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("confirm_undo_dialog_btn")
                    ) {
                        Text("DELETE EVENT", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = Color.White))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { eventToUndo = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoutDivider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("CANCEL", style = ScoutTypographyLabelM.copy(fontSize = 18.sp, color = ScoutTextPrimary))
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════
// 10. TEAM COLORS WORKFLOW
// ═══════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamColorsWorkflow(
    currentHome: String,
    currentAway: String,
    homeTeamName: String,
    awayTeamName: String,
    onSaveColors: (home: String, away: String) -> Unit,
    onBack: () -> Unit
) {
    var homeColor by remember { mutableStateOf(currentHome) }
    var awayColor by remember { mutableStateOf(currentAway) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SubMenuHeader(title = "Team Kit Colors", onBack = onBack)

        Text(
            text = "$homeTeamName (HOME) Kit Color: $homeColor",
            style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // 64dp swatches for Home
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScoutTeamSwatches.forEach { (name, color) ->
                val isSelected = homeColor.equals(name, ignoreCase = true)
                Surface(
                    onClick = { homeColor = name },
                    shape = CircleShape,
                    color = color,
                    border = BorderStroke(
                        if (isSelected) 4.dp else 1.5.dp,
                        if (isSelected) Color(0xFF0F1A2E) else ScoutDivider
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("color_home_swatch_$name")
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$awayTeamName (AWAY) Kit Color: $awayColor",
            style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // 64dp swatches for Away
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScoutTeamSwatches.forEach { (name, color) ->
                val isSelected = awayColor.equals(name, ignoreCase = true)
                Surface(
                    onClick = { awayColor = name },
                    shape = CircleShape,
                    color = color,
                    border = BorderStroke(
                        if (isSelected) 4.dp else 1.5.dp,
                        if (isSelected) Color(0xFF0F1A2E) else ScoutDivider
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("color_away_swatch_$name")
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onSaveColors(homeColor, awayColor) },
            colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("save_team_colors_btn")
        ) {
            Text("SAVE TEAM COLORS", style = ScoutTypographyTitleL.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
    }
}

// ═══════════════════════════════════════════════
// 12. REQUEST ASSISTANCE WORKFLOW
// ═══════════════════════════════════════════════
@Composable
fun RequestAssistanceWorkflow(
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Help,
            contentDescription = "Assistance",
            tint = ScoutPrimaryAction,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Request Supervisor Assistance",
            style = ScoutTypographyTitleXL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ScoutTextPrimary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Do you want to log a request for scout supervisor or technical support? This request will be saved locally to the match log.",
            style = ScoutTypographyBodyL.copy(fontSize = 20.sp, color = ScoutTextSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = ScoutDivider),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("assistance_cancel_btn")
            ) {
                Text("CANCEL", style = ScoutTypographyLabelM.copy(fontSize = 20.sp, color = ScoutTextPrimary))
            }

            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ScoutPrimaryAction),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("assistance_confirm_btn")
            ) {
                Text("REQUEST", style = ScoutTypographyTitleL.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

// ═══════════════════════════════════════════════
// HELPER HEADER COMPONENT
// ═══════════════════════════════════════════════
@Composable
fun SubMenuHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(56.dp)
                .testTag("submenu_back_btn")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ScoutTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = ScoutTypographyTitleXL.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = ScoutTextPrimary
        )
    }
    HorizontalDivider(color = ScoutDivider, thickness = 1.5.dp)
}
