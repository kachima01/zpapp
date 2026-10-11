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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.example.engine.ToolAllocation
import com.example.ui.components.BadgeType
import com.example.ui.components.HighContrastBadge
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
        if (record == null && recordId > 0) {
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
            Text("Loading case dossier…", color = Color(0xFF38BDF8), style = MaterialTheme.typography.titleMedium)
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

    // Dynamic case-tailored tool allocations specifically evaluated for THIS case's evidence
    val caseAllocatedTools: List<ToolAllocation> = remember(currentRecord) {
        val isLocked = !currentRecord.lockStatus.contains("Unlocked", ignoreCase = true)
        CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = currentRecord.deviceType,
            crimeType = currentRecord.crimeType,
            isLocked = isLocked,
            cloudAuthorized = currentRecord.cloudScopeAuthorized
        )
    }

    val selectedToolsList = remember(currentRecord.selectedToolsCsv) {
        if (currentRecord.selectedToolsCsv.isBlank()) emptyList()
        else currentRecord.selectedToolsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
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
                        text = "$timestampFormatted • ${currentRecord.crimeType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row {
                IconButton(onClick = { shareStructuredReport(context, currentRecord) }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export Dossier", tint = Color(0xFF38BDF8))
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
            // RECOMMENDED METHOD BANNER
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
                                text = "RECOMMENDED METHOD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            HighContrastBadge(
                                text = currentRecord.supervisorStatus.replace('_', ' '),
                                type = when (currentRecord.supervisorStatus) {
                                    "APPROVED" -> BadgeType.SUCCESS
                                    "FLAGGED" -> BadgeType.ERROR
                                    else -> BadgeType.WARNING
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentRecord.recommendedMethod,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Rationale: ${currentRecord.primaryRationale}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentRecord.chainOfCustodyCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (currentRecord.chainOfCustodyCompleted) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentRecord.chainOfCustodyCompleted) "Chain of Custody verified (Isolation confirmed)" else "Custody checks pending isolation",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentRecord.chainOfCustodyCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // CASE-ALLOCATED FORENSIC TOOLS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CASE-ALLOCATED TOOLS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Tailored for ${currentRecord.deviceType.take(18)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // LIST OF ALLOCATED TOOLS SPECIFIC TO THIS CASE
            items(caseAllocatedTools) { alloc ->
                val isSelectedInRecord = selectedToolsList.any { it.contains(alloc.toolName, ignoreCase = true) || alloc.toolName.contains(it, ignoreCase = true) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("case_tool_${alloc.toolName.take(10)}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alloc.isPrimaryAllocation) Color(0xFF13223D) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (alloc.isPrimaryAllocation) Color(0xFF2563EB) else Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alloc.toolName,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (alloc.isPrimaryAllocation) Color(0xFF065F46) else Color(0xFF1E293B),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (alloc.isPrimaryAllocation) "PRIMARY TOOL" else "SUPPORTING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (alloc.isPrimaryAllocation) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = alloc.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = alloc.allocationReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Capabilities: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alloc.keyCapabilities,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // EVIDENCE & CASE SPECIFICATIONS CARD
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
                            Text("EVIDENCE & CASE SPECS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SpecRow("Device / Target", "${currentRecord.deviceMake} ${currentRecord.deviceModel}")
                        SpecRow("Evidence Type", currentRecord.deviceType)
                        SpecRow("Serial / IMEI", currentRecord.deviceImeiOrSerial)
                        SpecRow("OS & Version", "${currentRecord.osType} ${currentRecord.osVersion}")
                        SpecRow("Lock & Security", currentRecord.lockStatus)
                        SpecRow("Encryption", currentRecord.encryptionType)
                        SpecRow("Legal Access", currentRecord.warrantType)
                        SpecRow("Docket Ref", currentRecord.incidentReference)
                        SpecRow("Cloud Scope", if (currentRecord.cloudScopeAuthorized) "Authorized in Warrant" else "Excluded")
                        SpecRow("Investigator", "${currentRecord.officerName} (${currentRecord.officerBadge})")
                    }
                }
            }

            // SUPERVISOR REVIEW SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SUPERVISOR REVIEW", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            if (canReview) {
                                OutlinedButton(
                                    onClick = { showSupervisorDialog = true },
                                    modifier = Modifier.height(32.dp).testTag("btn_review_case")
                                ) {
                                    Text("Review", color = Color(0xFF38BDF8), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (currentRecord.supervisorNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notes: ${currentRecord.supervisorNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentRecord.supervisorStatus == "APPROVED") "Case approved for court submission." else "Pending supervisor sign-off.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
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

    // SUPERVISOR REVIEW DIALOG
    if (showSupervisorDialog) {
        AlertDialog(
            onDismissRequest = { showSupervisorDialog = false },
            title = { Text("Supervisor Review", color = Color.White) },
            text = {
                Column {
                    Text("Select approval status for court admissibility:", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = supervisorNotesInput,
                        onValueChange = { supervisorNotesInput = it },
                        label = { Text("Review Notes") },
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
                    Text("Flag for Revision", color = Color(0xFFEF4444))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // DELETE CONFIRMATION
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Case Record?") },
            text = { Text("This will permanently remove ${currentRecord.caseNumber} from local SQLite storage.") },
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
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

private fun shareStructuredReport(context: Context, record: DecisionRecordEntity) {
    val report = buildString {
        appendLine("==========================================")
        appendLine("ZAMBIA FORENSIC DECISION REPORT (ZCMFF)")
        appendLine("==========================================")
        appendLine("Case Number: ${record.caseNumber}")
        appendLine("Docket Ref: ${record.incidentReference}")
        appendLine("Crime Type: ${record.crimeType}")
        appendLine("Evidence Target: ${record.deviceMake} ${record.deviceModel} (${record.deviceType})")
        appendLine("Serial / IMEI: ${record.deviceImeiOrSerial}")
        appendLine("Security State: ${record.lockStatus} | ${record.encryptionType}")
        appendLine("------------------------------------------")
        appendLine("RECOMMENDED METHOD: ${record.recommendedMethod}")
        appendLine("RATIONALE: ${record.primaryRationale}")
        appendLine("ALLOCATED TOOLS: ${record.allocatedToolsCsv}")
        appendLine("STATUS: ${record.supervisorStatus}")
        appendLine("EXAMINER: ${record.officerName} (${record.officerBadge})")
        appendLine("==========================================")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Forensic Dossier: ${record.caseNumber}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Share Case Dossier"))
}
