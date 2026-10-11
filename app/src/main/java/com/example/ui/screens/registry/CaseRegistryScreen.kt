package com.example.ui.screens.registry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecisionRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaseRegistryScreen(
    records: List<DecisionRecordEntity>,
    searchQuery: String,
    filterMethod: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onRecordClick: (Long) -> Unit,
    onStartNewAssessment: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .testTag("case_registry_screen")
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CASE REGISTRY",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${records.size} cases in local SQLite",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981)
                    )
                }
            }

            IconButton(onClick = onStartNewAssessment) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Case", tint = Color(0xFF38BDF8))
            }
        }

        // SEARCH BAR
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by Case #, IMEI, Device, Crime...", color = Color(0xFF64748B)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("registry_search_input"),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF38BDF8)
                )
            )
        }

        // FILTER CHIPS ROW
        val filterOptions = listOf(
            "ALL" to "All Cases",
            "MOBILE" to "Mobile / Phones",
            "COMPUTER" to "Computers / Servers",
            "CLOUD" to "Cloud Accounts",
            "RAM" to "Volatile RAM"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { (key, label) ->
                val isSelected = filterMethod == key
                Surface(
                    modifier = Modifier
                        .clickable { onFilterChange(key) }
                        .testTag("filter_chip_$key"),
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF0F172A),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // LIST OF CASES
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No cases matching '$searchQuery'" else "No cases stored in SQLite",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onStartNewAssessment,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Create New Case")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { record ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(record.timestamp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecordClick(record.id) }
                            .testTag("record_card_${record.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.caseNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${record.crimeType} • ${record.deviceType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Method: ${record.recommendedMethod}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = record.supervisorStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (record.supervisorStatus == "APPROVED") Color(0xFF10B981) else Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
