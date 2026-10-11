package com.example.ui.screens.record

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DecisionRecordEntity
import com.example.engine.CaseToolAllocator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DecisionRecordDetailScreen(
    recordId: Long,
    initialRecord: DecisionRecordEntity?,
    getRecordDirect: suspend (Long) -> DecisionRecordEntity?,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onUpdateSupervisor: (Long, String, String) -> Unit,
    canReview: Boolean
) {
    val context = LocalContext.current
    var record by remember(initialRecord) { mutableStateOf(initialRecord) }

    LaunchedEffect(recordId) {
        if (record == null) {
            record = getRecordDirect(recordId)
        }
    }

    if (record == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090E17))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading case…", color = Color(0xFF38BDF8), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(onClick = onBack) {
                Text("Return to Dashboard", color = Color.White)
            }
        }
        return
    }

    val currentRecord = record!!
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSupervisorDialog by remember { mutableStateOf(false) }
    var supervisorNotesInput by remember { mutableStateOf(currentRecord.supervisorNotes) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val timestampFormatted = dateFormat.format(Date(currentRecord.timestamp))

    // Dynamically calculate the tools that were allocated for this case
    val allocatedToolList = remember(currentRecord) {
        val isLocked = !currentRecord.lockStatus.contains("Unlocked", ignoreCase = true)
        CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = currentRecord.deviceType,
            crimeType = currentRecord.crimeType,
            isLocked = isLocked,
            cloudAuthorized = currentRecord.cloudScopeAuthorized
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .testTag("decision_record_detail_screen")
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
                        text = currentRecord.caseNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timestampFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row {
                IconButton(onClick = { shareCaseSummary(context, currentRecord) }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF38BDF8))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // RECOMMENDED METHOD & STATUS BANNER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(2.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECOMMENDATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (currentRecord.supervisorStatus == "APPROVED") Color(0xFF0B2920) else Color(0xFF2C200C),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (currentRecord.supervisorStatus == "APPROVED") Color(0xFF10B981) else Color(0xFFFBBF24),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentRecord.supervisorStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentRecord.supervisorStatus == "APPROVED") Color(0xFF10B981) else Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentRecord.recommendedMethod,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentRecord.primaryRationale,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // ALLOCATED TOOLS FOR THIS CASE (KEY REQUIREMENT)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECOMMENDED TOOLS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                            text = currentRecord.deviceType,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            items(allocatedToolList) { alloc ->
                val isSelectedForCase = currentRecord.selectedToolsCsv.contains(alloc.toolName, ignoreCase = true)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelectedForCase) Color(0xFF2563EB) else Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alloc.toolName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelectedForCase) Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSelectedForCase) "DEPLOYED" else "SUPPORTING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelectedForCase) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = alloc.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = alloc.allocationReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Capabilities: ${alloc.keyCapabilities}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // CASE & EVIDENCE SPECIFICATIONS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                            text = "CASE DETAILS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Crime Category", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(currentRecord.crimeType, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Target Evidence", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(currentRecord.deviceType, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Device Make & Model", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text("${currentRecord.deviceMake} ${currentRecord.deviceModel}", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lock / Encryption", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(currentRecord.lockStatus, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cloud Scope", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(if (currentRecord.cloudScopeAuthorized) "Authorized in Warrant" else "Excluded", color = if (currentRecord.cloudScopeAuthorized) Color(0xFF10B981) else Color(0xFFF87171), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Warrant Authority", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(currentRecord.warrantType, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // EXAMINER INFORMATION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                            text = "EXAMINER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lead Examiner", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text("${currentRecord.officerName} (${currentRecord.officerBadge})", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Station / Unit", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            Text(currentRecord.stationUnit, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // ACTION BUTTONS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canReview) {
                        Button(
                            onClick = { showSupervisorDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_supervisor_review"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        Text("Review")
                        }
                    }

                    OutlinedButton(
                        onClick = { shareCaseSummary(context, currentRecord) },
                        modifier = Modifier
                            .weight(if (canReview) 1f else 2f)
                            .height(48.dp)
                            .testTag("btn_share_case"),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // SUPERVISOR REVIEW DIALOG
    if (showSupervisorDialog) {
        AlertDialog(
            onDismissRequest = { showSupervisorDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Supervisor review", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Case #${currentRecord.caseNumber}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = supervisorNotesInput,
                        onValueChange = { supervisorNotesInput = it },
                        placeholder = { Text("Supervisor review remarks...", color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF131D31),
                            unfocusedContainerColor = Color(0xFF131D31),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSupervisor(currentRecord.id, "APPROVED", supervisorNotesInput)
                        record = currentRecord.copy(supervisorStatus = "APPROVED", supervisorNotes = supervisorNotesInput)
                        showSupervisorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Approve Case")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onUpdateSupervisor(currentRecord.id, "FLAGGED", supervisorNotesInput)
                        record = currentRecord.copy(supervisorStatus = "FLAGGED", supervisorNotes = supervisorNotesInput)
                        showSupervisorDialog = false
                    }
                ) {
                    Text("Flag Case", color = Color(0xFFEF4444))
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("Delete Case Record?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove Case ${currentRecord.caseNumber} from local SQLite storage.", color = Color(0xFFCBD5E1)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(currentRecord.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

private fun shareCaseSummary(context: Context, record: DecisionRecordEntity) {
    val text = """
        === ZCMFF FORENSIC CASE RECORD ===
        Case Number: ${record.caseNumber}
        Incident Ref: ${record.incidentReference}
        Crime: ${record.crimeType}
        Evidence Target: ${record.deviceType}
        Device: ${record.deviceMake} ${record.deviceModel} (IMEI: ${record.deviceImeiOrSerial})
        Lock Status: ${record.lockStatus}
        Warrant Authority: ${record.warrantType}
        Cloud Scope: ${if (record.cloudScopeAuthorized) "Authorized" else "Excluded"}
        
        Recommended Method: ${record.recommendedMethod}
        Allocated Tools: ${record.allocatedToolsCsv}
        Deployed Tools: ${record.selectedToolsCsv}
        Rationale: ${record.primaryRationale}
        
        Examiner: ${record.officerName} (${record.officerBadge})
        Status: ${record.supervisorStatus}
        Store: Local SQLite (zcmff_forensics.db)
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Forensic Case Record: ${record.caseNumber}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Export Case Record"))
}
