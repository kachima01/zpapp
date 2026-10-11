package com.example.ui.screens.assessment

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.viewmodel.ForensicViewModel

@Composable
fun AssessmentWizardScreen(
    viewModel: ForensicViewModel,
    allTools: List<StationToolEntity>,
    onCancel: () -> Unit,
    onComplete: (Long) -> Unit
) {
    val formState by viewModel.caseFormState.collectAsState()
    val allocatedTools = viewModel.getAllocatedToolsForCurrentForm()

    var expandedCrimeDropdown by remember { mutableStateOf(false) }
    var expandedDeviceDropdown by remember { mutableStateOf(false) }
    var expandedLockDropdown by remember { mutableStateOf(false) }
    var expandedWarrantDropdown by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val crimeOptions = listOf(
        "Mobile Money Fraud",
        "Computer Hacking & Intrusion",
        "Financial Theft & Embezzlement",
        "Defamation / Hate Speech (Cyber Crimes)",
        "Drug Trafficking & Contraband",
        "Active Malware / Live Infection",
        "General Digital Evidence"
    )

    val deviceOptions = listOf(
        "Smartphone / Mobile",
        "Computer / Server / HDD",
        "Cloud Account",
        "Volatile Memory / RAM",
        "Cross-Platform Correlation"
    )

    val lockOptions = listOf(
        "Passcode Locked",
        "Unlocked",
        "BitLocker / Full Disk Encrypted",
        "Pattern / Biometric Unknown",
        "Broken Display"
    )

    val warrantOptions = listOf(
        "Full Search Warrant (Sec 118 CPC)",
        "Limited Scope Warrant",
        "Owner Signed Consent",
        "Exigent Circumstances (Sec 22)",
        "Preliminary Preservation"
    )
    val isFormValid = formState.caseNumber.isNotBlank() &&
        formState.incidentReference.isNotBlank() &&
        formState.deviceMake.isNotBlank() &&
        formState.deviceModel.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .testTag("assessment_wizard_screen")
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
                IconButton(onClick = { showDiscardConfirm = true }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NEW FORENSIC CASE",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SQLite Case Setup & Tool Allocation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Text(
                text = "Step 1 of 3",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // CASE IDENTIFIERS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CASE IDENTIFICATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CASE NUMBER", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = formState.caseNumber,
                                    onValueChange = { viewModel.updateCaseForm(caseNumber = it) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_case_number"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = fieldColors()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("DOCKET REF", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = formState.incidentReference,
                                    onValueChange = { viewModel.updateCaseForm(incidentRef = it) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_docket_ref"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = fieldColors()
                                )
                            }
                        }

                        if (attemptedSave && (formState.caseNumber.isBlank() || formState.incidentReference.isBlank())) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter both the case number and docket reference before saving.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Crime Type Dropdown
                        Text("CRIME CATEGORY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedCrimeDropdown = true }
                                    .testTag("dropdown_crime"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formState.crimeType, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedCrimeDropdown,
                                onDismissRequest = { expandedCrimeDropdown = false },
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF131D31))
                            ) {
                                crimeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = {
                                            viewModel.updateCaseForm(crimeType = option)
                                            expandedCrimeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TARGET EVIDENCE & DEVICE SPECS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TARGET EVIDENCE & DEVICE TYPE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Evidence / Device Type Dropdown
                        Text("EVIDENCE CATEGORY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedDeviceDropdown = true }
                                    .testTag("dropdown_device_type"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formState.deviceType, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedDeviceDropdown,
                                onDismissRequest = { expandedDeviceDropdown = false },
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF131D31))
                            ) {
                                deviceOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = {
                                            viewModel.updateCaseForm(deviceType = option)
                                            expandedDeviceDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MAKE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = formState.deviceMake,
                                    onValueChange = { viewModel.updateCaseForm(deviceMake = it) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_device_make"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = fieldColors()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("MODEL", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = formState.deviceModel,
                                    onValueChange = { viewModel.updateCaseForm(deviceModel = it) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_device_model"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = fieldColors()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lock Status Dropdown
                        Text("DEVICE LOCK STATUS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedLockDropdown = true }
                                    .testTag("dropdown_lock"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formState.lockStatus, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedLockDropdown,
                                onDismissRequest = { expandedLockDropdown = false },
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF131D31))
                            ) {
                                lockOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = {
                                            viewModel.updateCaseForm(lockStatus = option)
                                            expandedLockDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cloud scope toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF131D31), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cloud Scope Authorized in Warrant", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
                                Text("Extract iCloud / Google / WhatsApp backups", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            }
                            Switch(
                                checked = formState.cloudScopeAuthorized,
                                onCheckedChange = { viewModel.updateCaseForm(cloudAuthorized = it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        if (attemptedSave && (formState.deviceMake.isBlank() || formState.deviceModel.isBlank())) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter the device make and model before saving.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // ALLOCATED TOOLS FOR THIS CASE (DYNAMICALLY CALCULATED)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("LEGAL AUTHORITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Warrant or authority", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { expandedWarrantDropdown = true },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(formState.warrantType, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose warrant authority", tint = Color(0xFF94A3B8))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedWarrantDropdown,
                                onDismissRequest = { expandedWarrantDropdown = false },
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF131D31))
                            ) {
                                warrantOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = { viewModel.updateCaseForm(warrantType = option); expandedWarrantDropdown = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ALLOCATED TOOLS FOR THIS CASE (DYNAMICALLY CALCULATED)
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
                            text = "ALLOCATED TOOLS FOR THIS CASE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${formState.selectedTools.size} Assigned",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(allocatedTools) { alloc ->
                val isSelected = formState.selectedTools.contains(alloc.toolName)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleToolInCase(alloc.toolName) }
                        .testTag("tool_card_${alloc.toolName.take(10)}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF13223D) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleToolInCase(alloc.toolName) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2563EB),
                                checkmarkColor = Color.White,
                                uncheckedColor = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
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
                                Text(
                                    text = if (alloc.isPrimaryAllocation) "PRIMARY" else "SUPPORTING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (alloc.isPrimaryAllocation) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
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
                        }
                    }
                }
            }

            // PRIMARY SAVE CASE BUTTON
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        attemptedSave = true
                        if (isFormValid) viewModel.saveCaseRecord { recordId -> onComplete(recordId) }
                    },
                    enabled = isFormValid || !attemptedSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_case_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFormValid) "Save case" else "Complete required fields",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard this case draft?") },
            text = { Text("Your unsaved case details and selected tools will be lost.") },
            confirmButton = {
                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Discard")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDiscardConfirm = false }) { Text("Keep editing") }
            }
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF131D31),
    unfocusedContainerColor = Color(0xFF131D31),
    focusedBorderColor = Color(0xFF2563EB),
    unfocusedBorderColor = Color(0xFF1E293B),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF38BDF8)
)
