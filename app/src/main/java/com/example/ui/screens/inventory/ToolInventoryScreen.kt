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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onBack: () -> Unit
) {
    val activeCount = tools.count { it.isAvailable }
    val totalCount = tools.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("tool_inventory_screen")
    ) {
        TacticalHeader(
            title = "Station Tool Inventory",
            subtitle = "$activeCount of $totalCount forensic suites currently active",
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
                                text = "STATION LABORATORY READINESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForensicCyanPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toggle active licenses and hardware kits available at your station. The ZCMFF Decision Engine strictly checks this inventory before recommending physical, logical, or cloud pathways.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryDark
                        )
                    }
                }
            }

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

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HighContrastBadge(
                                    text = if (tool.isAvailable) "READY IN STATION" else "NOT AVAILABLE",
                                    type = if (tool.isAvailable) BadgeType.SUCCESS else BadgeType.NEUTRAL
                                )
                                HighContrastBadge(
                                    text = tool.licenseStatus,
                                    type = when (tool.licenseStatus) {
                                        "ACTIVE", "HARDWARE_READY" -> BadgeType.PRIMARY
                                        "COMMUNITY_EDITION" -> BadgeType.WARNING
                                        else -> BadgeType.NEUTRAL
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
