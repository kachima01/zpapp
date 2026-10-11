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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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

    // Parse stored rejected alternatives: "Method: Reason [Barrier] | …"
    val rejectedAlternatives = remember(currentRecord.rejectedAlternativesRaw) {
        if (currentRecord.rejectedAlternativesRaw.isBlank()) emptyList()
        else currentRecord.rejectedAlternativesRaw.split(" | ").mapNotNull { entry ->
            val colonIdx = entry.indexOf(':')
            val bracketStart = entry.lastIndexOf('[')
            val bracketEnd = entry.lastIndexOf(']')
            if (colonIdx < 0) null
            else Triple(
                entry.substring(0, colonIdx).trim(),
                if (bracketStart > 0) entry.substring(colonIdx + 1, bracketStart).trim() else entry.substring(colonIdx + 1).trim(),
                if (bracketStart >= 0 && bracketEnd > bracketStart) entry.substring(bracketStart + 1, bracketEnd).trim() else ""
            )
        }
    }

    // Parse cautions
    val cautions = remember(currentRecord.cautionaryGuidanceCsv) {
        if (currentRecord.cautionaryGuidanceCsv.isBlank()) emptyList()
        else currentRecord.cautionaryGuidanceCsv.split(" | ").map { it.trim() }.filter { it.isNotBlank() }
    }

    // Parse selected tools
    val selectedTools = remember(currentRecord.selectedToolsCsv) {
        if (currentRecord.selectedToolsCsv.isBlank()) emptyList()
        else currentRecord.selectedToolsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .testTag("decision_record_detail_screen")
    ) {
        // ── TOP APP BAR ────────────────────────────────────────────────────────
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
                IconButton(onClick = { shareStructuredReport(context, currentRecord) }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export", tint = Color(0xFF38BDF8))
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
            // ── RECOMMENDATION BANNER ─────────────────────────────────────────
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
                            StatusBadge(currentRecord.supervisorStatus)
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
                            text = "Tool Category: ${currentRecord.recommendedToolCategory}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Engine rationale
                        Text(
                            text = "DECISION RATIONALE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentRecord.primaryRationale,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )

                        // Chain-of-custody status
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentRecord.chainOfCustodyCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (currentRecord.chainOfCustodyCompleted) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentRecord.chainOfCustodyCompleted) "Chain-of-custody checks passed" else "Chain-of-custody checks incomplete",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentRecord.chainOfCustodyCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // ── DEPLOYED TOOLS ────────────────────────────────────────────────
            if (selectedTools.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        icon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) },
                        label = "DEPLOYED TOOLS",
                        labelColor = Color(0xFF38BDF8)
                    )
                }
                items(selectedTools.size) { idx ->
                    val tool = selectedTools[idx]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2563EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tool, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── CAUTIONARY GUIDANCE ───────────────────────────────────────────
            if (cautions.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp)) },
                        label = "CAUTIONARY GUIDANCE",
                        labelColor = Color(0xFFF59E0B)
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1200)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF78350F))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cautions.forEach { caution ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("⚠", color = Color(0xFFF59E0B), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(caution, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFDE68A))
                                }
                            }
                        }
                    }
                }
            }

            // ── REJECTED ALTERNATIVES ─────────────────────────────────────────
            if (rejectedAlternatives.isNotEmpty()) {
                item {
                    DetailSectionHeader(
                        icon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                        label = "REJECTED ALTERNATIVES",
                        labelColor = Color(0xFFEF4444)
                    )
                }
                items(rejectedAlternatives.size) { idx ->
                    val (method, reason, barrier) = rejectedAlternatives[idx]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A0A)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF7F1D1D))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (barrier.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF450A0A), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = barrier,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFEF4444),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // ── CASE & EVIDENCE SPECIFICATIONS ────────────────────────────────
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
                            Text("CASE & EVIDENCE DETAILS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailRow("Crime Category", currentRecord.crimeType)
                        DetailRow("Evidence Type", currentRecord.deviceType)
                        DetailRow("Device", "${currentRecord.deviceMake} ${currentRecord.deviceModel}")
                        if (currentRecord.deviceImeiOrSerial.isNotBlank() && currentRecord.deviceImeiOrSerial != "N/A") {
                            DetailRow("IMEI / Serial", currentRecord.deviceImeiOrSerial)
                        }
                        DetailRow("OS", "${currentRecord.osType} ${currentRecord.osVersion}".trim())
                        DetailRow("Lock Status", currentRecord.lockStatus)
                        DetailRow("Encryption", currentRecord.encryptionType)
                        DetailRow("Cloud Dependency", currentRecord.cloudDependency)
                        DetailRow("Cloud Scope", if (currentRecord.cloudScopeAuthorized) "Authorised in Warrant" else "Excluded")
                        if (currentRecord.incidentLocation.isNotBlank()) {
                            DetailRow("Incident Location", currentRecord.incidentLocation)
                        }
                        if (currentRecord.suspectInfo.isNotBlank()) {
                            DetailRow("Suspect Info", currentRecord.suspectInfo)
                        }
                    }
                }
            }

            // ── LEGAL AUTHORITY ───────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LEGAL AUTHORITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailRow("Warrant / Authority", currentRecord.warrantType)
                        if (currentRecord.warrantReference.isNotBlank() && currentRecord.warrantReference != "N/A") {
                            DetailRow("Reference Number", currentRecord.warrantReference)
                        }
                        DetailRow("Incident Reference", currentRecord.incidentReference)
                    }
                }
            }

            // ── EXAMINER INFORMATION ──────────────────────────────────────────
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
                            Text("EXAMINER", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailRow("Lead Examiner", "${currentRecord.officerName} (${currentRecord.officerBadge})")
                        DetailRow("Rank", currentRecord.officerRank)
                        DetailRow("Station / Unit", currentRecord.stationUnit)
                        DetailRow("Competence Level", currentRecord.competenceLevel)
                    }
                }
            }

            // ── SUPERVISOR REVIEW AUDIT TRAIL ─────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SUPERVISOR REVIEW", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailRow("Status", currentRecord.supervisorStatus)
                        if (currentRecord.supervisorReviewedAt > 0L) {
                            val reviewTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                .format(Date(currentRecord.supervisorReviewedAt))
                            DetailRow("Reviewed At", reviewTime)
                        }
                        if (currentRecord.supervisorNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Supervisor Notes", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentRecord.supervisorNotes, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }

            // ── ACTION BUTTONS ────────────────────────────────────────────────
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
                        onClick = { shareStructuredReport(context, currentRecord) },
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
                        Text("Export Report")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ── SUPERVISOR REVIEW DIALOG ───────────────────────────────────────────────
    if (showSupervisorDialog) {
        AlertDialog(
            onDismissRequest = { showSupervisorDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Supervisor Review", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Case #${currentRecord.caseNumber}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Method: ${currentRecord.recommendedMethod}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = supervisorNotesInput,
                        onValueChange = { supervisorNotesInput = it },
                        placeholder = { Text("Supervisor review remarks…", color = Color(0xFF64748B)) },
                        label = { Text("Review Notes", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF131D31),
                            unfocusedContainerColor = Color(0xFF131D31),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSupervisor(currentRecord.id, "APPROVED", supervisorNotesInput)
                        record = currentRecord.copy(
                            supervisorStatus = "APPROVED",
                            supervisorNotes = supervisorNotesInput,
                            supervisorReviewedAt = System.currentTimeMillis()
                        )
                        showSupervisorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onUpdateSupervisor(currentRecord.id, "FLAGGED", supervisorNotesInput)
                        record = currentRecord.copy(
                            supervisorStatus = "FLAGGED",
                            supervisorNotes = supervisorNotesInput,
                            supervisorReviewedAt = System.currentTimeMillis()
                        )
                        showSupervisorDialog = false
                    }
                ) {
                    Text("Flag for Review", color = Color(0xFFEF4444))
                }
            }
        )
    }

    // ── DELETE CONFIRMATION ────────────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("Delete Case Record?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove Case ${currentRecord.caseNumber} and its decision record.", color = Color(0xFFCBD5E1)) },
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

// ── Helper composables ─────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: String) {
    val (bg, border, text) = when (status) {
        "APPROVED" -> Triple(Color(0xFF0B2920), Color(0xFF10B981), Color(0xFF10B981))
        "FLAGGED" -> Triple(Color(0xFF2C0808), Color(0xFFEF4444), Color(0xFFEF4444))
        else -> Triple(Color(0xFF2C200C), Color(0xFFFBBF24), Color(0xFFFBBF24))
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailSectionHeader(
    icon: @Composable () -> Unit,
    label: String,
    labelColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.45f))
        Text(value, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.55f))
    }
}

// ── Structured decision report export ─────────────────────────────────────────

private fun shareStructuredReport(context: Context, record: DecisionRecordEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val createdAt = dateFormat.format(Date(record.timestamp))
    val reviewedAt = if (record.supervisorReviewedAt > 0L)
        dateFormat.format(Date(record.supervisorReviewedAt)) else "Not yet reviewed"

    val cautions = if (record.cautionaryGuidanceCsv.isBlank()) "None recorded"
    else record.cautionaryGuidanceCsv.split(" | ").joinToString("\n") { "  • $it" }

    val rejected = if (record.rejectedAlternativesRaw.isBlank()) "None"
    else record.rejectedAlternativesRaw.split(" | ").joinToString("\n") { entry ->
        val colonIdx = entry.indexOf(':')
        if (colonIdx < 0) "  • $entry"
        else {
            val method = entry.substring(0, colonIdx).trim()
            val rest = entry.substring(colonIdx + 1).trim()
            "  • $method\n      Reason: $rest"
        }
    }

    val report = """
══════════════════════════════════════════════════
ZAMBIA POLICE SERVICE — ZCMFF FORENSIC DECISION RECORD
══════════════════════════════════════════════════
Case Number   : ${record.caseNumber}
Docket Ref    : ${record.incidentReference}
Created At    : $createdAt
Supervisor    : ${record.supervisorStatus}
Reviewed At   : $reviewedAt

── INCIDENT ──────────────────────────────────────
Crime Category    : ${record.crimeType}
Incident Location : ${record.incidentLocation.ifBlank { "Not specified" }}
Suspect Info      : ${record.suspectInfo.ifBlank { "Not specified" }}

── EVIDENCE DEVICE ────────────────────────────────
Evidence Type  : ${record.deviceType}
Make & Model   : ${record.deviceMake} ${record.deviceModel}
IMEI / Serial  : ${record.deviceImeiOrSerial}
OS             : ${record.osType} ${record.osVersion}
Lock Status    : ${record.lockStatus}
Encryption     : ${record.encryptionType}
Cloud Dependency: ${record.cloudDependency}
Cloud Scope    : ${if (record.cloudScopeAuthorized) "Authorised in Warrant" else "Excluded"}

── LEGAL AUTHORITY ────────────────────────────────
Warrant / Authority : ${record.warrantType}
Warrant Reference   : ${record.warrantReference.ifBlank { "N/A" }}

── DECISION ENGINE OUTPUT ─────────────────────────
Recommended Method   : ${record.recommendedMethod}
Tool Category        : ${record.recommendedToolCategory}
Deployed Tools       : ${record.selectedToolsCsv.ifBlank { record.recommendedTools }}

Decision Rationale:
  ${record.primaryRationale}

Cautionary Guidance:
$cautions

Rejected Alternatives:
$rejected

── CHAIN OF CUSTODY ───────────────────────────────
Checks Completed     : ${if (record.chainOfCustodyCompleted) "YES (Faraday + Airplane Mode)" else "INCOMPLETE"}

── EXAMINER ───────────────────────────────────────
Lead Examiner  : ${record.officerName} (${record.officerBadge})
Rank           : ${record.officerRank}
Competence     : ${record.competenceLevel}
Station / Unit : ${record.stationUnit}

── SUPERVISOR REVIEW ──────────────────────────────
Status         : ${record.supervisorStatus}
Reviewed At    : $reviewedAt
Notes          : ${record.supervisorNotes.ifBlank { "None" }}

══════════════════════════════════════════════════
This record was generated by the ZCMFF Forensic Decision Support System.
It must be retained as part of the official case file.
══════════════════════════════════════════════════
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "ZCMFF Forensic Decision Record — ${record.caseNumber}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Export Forensic Decision Report"))
}
