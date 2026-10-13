package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.RwandaFootballData
import com.example.ui.components.ScoutButton
import com.example.ui.components.ScoutButtonStyle
import com.example.ui.components.ScoutTextField
import com.example.ui.components.ScoutTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScoutViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewMatchScreen(
    viewModel: ScoutViewModel,
    onNavigateBack: () -> Unit,
    onMatchCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var homeTeam by remember { mutableStateOf("APR FC") }
    var awayTeam by remember { mutableStateOf("Rayon Sports FC") }
    var competition by remember { mutableStateOf(RwandaFootballData.COMPETITIONS.first()) }
    var venue by remember { mutableStateOf(RwandaFootballData.VENUES.first()) }

    var competitionExpanded by remember { mutableStateOf(false) }
    var venueExpanded by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    var matchDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var kickoffTimeMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                matchDateMillis = c.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = matchDateMillis
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                kickoffTimeMillis = c.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoutBackground)
    ) {
        ScoutTopBar(
            title = "New Match",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Home Team Field
            ScoutTextField(
                value = homeTeam,
                onValueChange = {
                    homeTeam = it
                    errorMessage = null
                },
                label = "Home Team",
                placeholder = "e.g. APR FC",
                leadingIcon = Icons.Default.SportsSoccer,
                testTag = "home_team_input"
            )

            // Quick team selector chips for Rwanda League
            Text(
                text = "Quick select Rwandan club:",
                style = ScoutTypographyCaption,
                color = ScoutTextSecondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("APR FC", "Rayon Sports FC", "Police FC", "AS Kigali", "SC Kiyovu").forEach { team ->
                    Surface(
                        onClick = {
                            if (homeTeam != team) homeTeam = team
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (homeTeam == team) ScoutPrimaryAction else ScoutSurfaceVariant,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = team,
                                style = ScoutTypographyCaption.copy(
                                    color = if (homeTeam == team) Color.White else ScoutTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Away Team Field
            ScoutTextField(
                value = awayTeam,
                onValueChange = {
                    awayTeam = it
                    errorMessage = null
                },
                label = "Away Team",
                placeholder = "e.g. Rayon Sports FC",
                leadingIcon = Icons.Default.SportsSoccer,
                testTag = "away_team_input"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Rayon Sports FC", "Mukura VS", "Musanze FC", "Gorilla FC", "Gasogi Utd").forEach { team ->
                    Surface(
                        onClick = {
                            if (awayTeam != team) awayTeam = team
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (awayTeam == team) ScoutPrimaryAction else ScoutSurfaceVariant,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = team,
                                style = ScoutTypographyCaption.copy(
                                    color = if (awayTeam == team) Color.White else ScoutTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Competition Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Competition",
                    style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = competitionExpanded,
                    onExpandedChange = { competitionExpanded = !competitionExpanded }
                ) {
                    OutlinedTextField(
                        value = competition,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .menuAnchor()
                            .testTag("competition_dropdown"),
                        textStyle = ScoutTypographyBodyL,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Sports,
                                contentDescription = null,
                                tint = ScoutPrimaryAction,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = competitionExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScoutPrimaryAction,
                            unfocusedBorderColor = ScoutDivider,
                            focusedContainerColor = ScoutSurface,
                            unfocusedContainerColor = ScoutSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = competitionExpanded,
                        onDismissRequest = { competitionExpanded = false },
                        modifier = Modifier.background(ScoutSurface)
                    ) {
                        RwandaFootballData.COMPETITIONS.forEach { comp ->
                            DropdownMenuItem(
                                text = { Text(comp, style = ScoutTypographyBodyM) },
                                onClick = {
                                    competition = comp
                                    competitionExpanded = false
                                },
                                modifier = Modifier.height(56.dp)
                            )
                        }
                    }
                }
            }

            // Venue Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Venue (Stadium)",
                    style = ScoutTypographyLabelM.copy(color = ScoutTextSecondary),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = venueExpanded,
                    onExpandedChange = { venueExpanded = !venueExpanded }
                ) {
                    OutlinedTextField(
                        value = venue,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .menuAnchor()
                            .testTag("venue_dropdown"),
                        textStyle = ScoutTypographyBodyL,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ScoutPrimaryAction,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = venueExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScoutPrimaryAction,
                            unfocusedBorderColor = ScoutDivider,
                            focusedContainerColor = ScoutSurface,
                            unfocusedContainerColor = ScoutSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = venueExpanded,
                        onDismissRequest = { venueExpanded = false },
                        modifier = Modifier.background(ScoutSurface)
                    ) {
                        RwandaFootballData.VENUES.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v, style = ScoutTypographyBodyM) },
                                onClick = {
                                    venue = v
                                    venueExpanded = false
                                },
                                modifier = Modifier.height(56.dp)
                            )
                        }
                    }
                }
            }

            // Date Picker (50dp interactive button)
            val formattedDate = DateFormat.format("EEEE, d MMMM yyyy", Date(matchDateMillis)).toString()
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Match Date",
                    style = ScoutTypographyLabelM.copy(
                        color = ScoutTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Surface(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(10.dp),
                    color = ScoutSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScoutDivider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("date_picker_button")
                        .semantics { contentDescription = "Select match date. Current: $formattedDate" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedDate,
                            style = ScoutTypographyBodyL.copy(fontSize = 14.5.sp),
                            color = ScoutTextPrimary
                        )
                    }
                }
            }

            // Kickoff Time Picker (50dp interactive button)
            val formattedTime = DateFormat.format("HH:mm", Date(kickoffTimeMillis)).toString()
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kickoff Time",
                    style = ScoutTypographyLabelM.copy(
                        color = ScoutTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Surface(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(10.dp),
                    color = ScoutSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScoutDivider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("time_picker_button")
                        .semantics { contentDescription = "Select kickoff time. Current: $formattedTime" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$formattedTime CAT",
                            style = ScoutTypographyBodyL.copy(fontSize = 14.5.sp),
                            color = ScoutTextPrimary
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = ScoutTypographyBodyM.copy(color = ScoutErrorDanger, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button 64dp filled #0B7EB8
            ScoutButton(
                text = "Save Match",
                icon = Icons.Default.Check,
                style = ScoutButtonStyle.PRIMARY,
                onClick = {
                    if (homeTeam.isBlank() || awayTeam.isBlank()) {
                        errorMessage = "Please enter both Home and Away team names."
                    } else if (homeTeam.trim().equals(awayTeam.trim(), ignoreCase = true)) {
                        errorMessage = "Home and Away teams must be different."
                    } else {
                        viewModel.createMatch(
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            competition = competition,
                            venue = venue,
                            dateMillis = matchDateMillis,
                            kickoffMillis = kickoffTimeMillis,
                            onCreated = onMatchCreated
                        )
                    }
                },
                testTag = "save_match_button"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
