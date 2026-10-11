package com.example.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StationToolEntity
import com.example.ui.components.BadgeType
import com.example.ui.components.HighContrastBadge
import com.example.ui.components.TacticalHeader
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenContainer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ForensicCyanPrimary
import com.example.ui.theme.HighContrastBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ToolInventoryScreen(
    tools: List<StationToolEntity>,
    onToggleTool: (Long, Boolean) -> Unit,
    onUpdateToolDetails: ((StationToolEntity) -> Unit)? = null,
    onBack: () -> Unit
) {
    val activeCount = tools.count { it.isAvailable }
    val totalCount = tools.size

    // Dialog state for editing unavailability reason
    var editingTool by remember { mutableStateOf<StationToolEntity?>(null) }
    var reasonInput by remember { mutableStateOf("") }
    var expiryInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("tool_inventory_screen")
    ) {
        TacticalHeader(
            title = "Station Tool Inventory",
            subtitle = "$activeCount of $totalCount tools available",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── INFO BANNER ───────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, HighContrastBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ForensicCyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TOOL AVAILABILITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForensicCyanPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Only tools marked Available are considered by the decision engine when recommending an extraction method. Toggle a tool off to exclude it from future recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryDark
                        )
                    }
                }
            }

            // ── TOOL CARDS GROUPED BY CATEGORY ────────────────────────────────
            val grouped = tools.groupBy { it.category }
            grouped.forEach { (category, toolList) ->
                item {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondaryDark,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(toolList) { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("station_tool_row_${tool.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (tool.isAvailable) DarkSurfaceElevated else DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (tool.isAvailable) HighContrastBorder else DarkSurfaceHighlight
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Name + toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tool.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.capabilities,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ForensicCyanPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Switch(
                                    checked = tool.isAvailable,
                                    onCheckedChange = { onToggleTool(tool.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CompliantGreen,
                                        checkedTrackColor = CompliantGreenContainer,
                                        uncheckedThumbColor = TextSecondaryDark,
                                        uncheckedTrackColor = DarkSurfaceHighlight
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark,
                                lineHeight = 19.sp
                            )

                            // Unavailability reason
                            if (!tool.isAvailable && tool.unavailabilityReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tool.unavailabilityReason,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                            }

                            // Licence expiry
                            if (tool.licenseExpiryDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Licence expires: ${tool.licenseExpiryDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HighContrastBadge(
                                    text = if (tool.isAvailable) "AVAILABLE" else "UNAVAILABLE",
                                    type = if (tool.isAvailable) BadgeType.SUCCESS else BadgeType.NEUTRAL
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    HighContrastBadge(
                                        text = tool.licenseStatus,
                                        type = when (tool.licenseStatus) {
                                            "ACTIVE", "HARDWARE_READY", "DEPLOYED" -> BadgeType.PRIMARY
                                            "COMMUNITY_EDITION" -> BadgeType.WARNING
                                            "EXPIRED", "NOT_LICENSED" -> BadgeType.NEUTRAL
                                            else -> BadgeType.NEUTRAL
                                        }
                                    )
                                    // Edit details button (shown when onUpdateToolDetails provided)
                                    if (onUpdateToolDetails != null) {
                                        OutlinedButton(
                                            onClick = {
                                                editingTool = tool
                                                reasonInput = tool.unavailabilityReason
                                                expiryInput = tool.licenseExpiryDate
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            border = BorderStroke(1.dp, Color(0xFF334155))
                                        ) {
                                            Text("Details", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── TOOL DETAILS EDIT DIALOG ──────────────────────────────────────────────
    editingTool?.let { tool ->
        AlertDialog(
            onDismissRequest = { editingTool = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(tool.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            },
            text = {
                Column {
                    Text("UNAVAILABILITY REASON", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        placeholder = { Text("e.g. Sent for calibration, licence lapsed…", color = Color(0xFF475569)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("LICENCE EXPIRY DATE (YYYY-MM-DD)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = expiryInput,
                        onValueChange = { expiryInput = it },
                        placeholder = { Text("e.g. 2026-12-31", color = Color(0xFF475569)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateToolDetails?.invoke(
                            tool.copy(
                                unavailabilityReason = reasonInput.trim(),
                                licenseExpiryDate = expiryInput.trim()
                            )
                        )
                        editingTool = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingTool = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF131D31),
    unfocusedContainerColor = Color(0xFF131D31),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF2563EB),
    unfocusedBorderColor = Color(0xFF334155)
)
