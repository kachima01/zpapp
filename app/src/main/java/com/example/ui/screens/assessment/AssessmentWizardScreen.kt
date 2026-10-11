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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

    // ── Dropdown expansion states ─────────────────────────────────────────────
    var expandedCrimeDropdown by remember { mutableStateOf(false) }
    var expandedDeviceDropdown by remember { mutableStateOf(false) }
    var expandedLockDropdown by remember { mutableStateOf(false) }
    var expandedWarrantDropdown by remember { mutableStateOf(false) }
    var expandedOsDropdown by remember { mutableStateOf(false) }
    var expandedEncryptionDropdown by remember { mutableStateOf(false) }
    var expandedCloudDropdown by remember { mutableStateOf(false) }
    var expandedCompetenceDropdown by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // ── Option lists ──────────────────────────────────────────────────────────
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
    val osOptions = listOf(
        "Android", "iOS", "Windows", "Linux", "macOS", "Feature Phone", "Other"
    )
    val encryptionOptions = listOf(
        "File-Based (FBE) + Secure Enclave",
        "Full-Disk (FDE)",
        "BitLocker",
        "Unencrypted",
        "Unknown"
    )
    val cloudOptions = listOf(
        "Cloud Backups Detected",
        "Cloud Synced Apps",
        "Local Only",
        "Unknown"
    )
    val competenceOptions = listOf("Basic", "Intermediate", "Advanced")

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
        // ── TOP APP BAR ────────────────────────────────────────────────────────
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
                        text = "New Case",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Forensic assessment & tool recommendation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── SECTION 1: CASE IDENTIFIERS ──────────────────────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) },
                    label = "CASE DETAILS",
                    labelColor = Color(0xFF38BDF8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("CASE NUMBER")
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
                            FieldLabel("DOCKET REF")
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Enter both the case number and docket reference.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FieldLabel("CRIME CATEGORY")
                    SimpleDropdown(
                        selected = formState.crimeType,
                        options = crimeOptions,
                        expanded = expandedCrimeDropdown,
                        onExpand = { expandedCrimeDropdown = true },
                        onDismiss = { expandedCrimeDropdown = false },
                        onSelect = { viewModel.updateCaseForm(crimeType = it); expandedCrimeDropdown = false },
                        testTag = "dropdown_crime"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("INCIDENT LOCATION")
                            OutlinedTextField(
                                value = formState.incidentLocation,
                                onValueChange = { viewModel.updateCaseForm(incidentLocation = it) },
                                modifier = Modifier.fillMaxWidth().testTag("input_incident_location"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = fieldColors()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FieldLabel("SUSPECT INFORMATION (optional)")
                    OutlinedTextField(
                        value = formState.suspectInfo,
                        onValueChange = { viewModel.updateCaseForm(suspectInfo = it) },
                        modifier = Modifier.fillMaxWidth().testTag("input_suspect_info"),
                        singleLine = false,
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Name, alias, role…", color = Color(0xFF475569)) },
                        colors = fieldColors()
                    )
                }
            }

            // ── SECTION 2: EVIDENCE DEVICE ────────────────────────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp)) },
                    label = "EVIDENCE DEVICE",
                    labelColor = Color(0xFF10B981)
                ) {
                    FieldLabel("EVIDENCE CATEGORY")
                    SimpleDropdown(
                        selected = formState.deviceType,
                        options = deviceOptions,
                        expanded = expandedDeviceDropdown,
                        onExpand = { expandedDeviceDropdown = true },
                        onDismiss = { expandedDeviceDropdown = false },
                        onSelect = { viewModel.updateCaseForm(deviceType = it); expandedDeviceDropdown = false },
                        testTag = "dropdown_device_type"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("MAKE")
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
                            FieldLabel("MODEL")
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

                    Spacer(modifier = Modifier.height(10.dp))

                    FieldLabel("IMEI / SERIAL NUMBER")
                    OutlinedTextField(
                        value = formState.deviceImeiOrSerial,
                        onValueChange = { viewModel.updateCaseForm(imeiOrSerial = it) },
                        modifier = Modifier.fillMaxWidth().testTag("input_imei"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("e.g. 358900000000000", color = Color(0xFF475569)) },
                        colors = fieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(2f)) {
                            FieldLabel("OPERATING SYSTEM")
                            SimpleDropdown(
                                selected = formState.osType,
                                options = osOptions,
                                expanded = expandedOsDropdown,
                                onExpand = { expandedOsDropdown = true },
                                onDismiss = { expandedOsDropdown = false },
                                onSelect = { viewModel.updateCaseForm(osType = it); expandedOsDropdown = false },
                                testTag = "dropdown_os"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("VERSION")
                            OutlinedTextField(
                                value = formState.osVersion,
                                onValueChange = { viewModel.updateCaseForm(osVersion = it) },
                                modifier = Modifier.fillMaxWidth().testTag("input_os_version"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = fieldColors()
                            )
                        }
                    }
                }
            }

            // ── SECTION 3: DEVICE STATE (KEY ENGINE INPUTS) ───────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)) },
                    label = "DEVICE STATE",
                    labelColor = Color(0xFFF59E0B)
                ) {
                    FieldLabel("LOCK STATUS")
                    SimpleDropdown(
                        selected = formState.lockStatus,
                        options = listOf(
                            "Passcode Locked",
                            "Unlocked",
                            "BitLocker / Full Disk Encrypted",
                            "Pattern / Biometric Unknown",
                            "Broken Display"
                        ),
                        expanded = expandedLockDropdown,
                        onExpand = { expandedLockDropdown = true },
                        onDismiss = { expandedLockDropdown = false },
                        onSelect = { viewModel.updateCaseForm(lockStatus = it); expandedLockDropdown = false },
                        testTag = "dropdown_lock"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FieldLabel("ENCRYPTION TYPE")
                    SimpleDropdown(
                        selected = formState.encryptionType,
                        options = encryptionOptions,
                        expanded = expandedEncryptionDropdown,
                        onExpand = { expandedEncryptionDropdown = true },
                        onDismiss = { expandedEncryptionDropdown = false },
                        onSelect = { viewModel.updateCaseForm(encryptionType = it); expandedEncryptionDropdown = false },
                        testTag = "dropdown_encryption"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FieldLabel("CLOUD DEPENDENCY")
                    SimpleDropdown(
                        selected = formState.cloudDependency,
                        options = cloudOptions,
                        expanded = expandedCloudDropdown,
                        onExpand = { expandedCloudDropdown = true },
                        onDismiss = { expandedCloudDropdown = false },
                        onSelect = { viewModel.updateCaseForm(cloudDependency = it); expandedCloudDropdown = false },
                        testTag = "dropdown_cloud_dependency"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                            Text("Cloud access authorised", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("Warrant covers cloud evidence", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = formState.cloudScopeAuthorized,
                            onCheckedChange = { viewModel.updateCaseForm(cloudAuthorized = it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                        )
                    }
                }
            }

            // ── SECTION 4: LEGAL AUTHORITY ────────────────────────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) },
                    label = "LEGAL AUTHORITY",
                    labelColor = Color(0xFF38BDF8)
                ) {
                    FieldLabel("WARRANT OR AUTHORITY TYPE")
                    SimpleDropdown(
                        selected = formState.warrantType,
                        options = warrantOptions,
                        expanded = expandedWarrantDropdown,
                        onExpand = { expandedWarrantDropdown = true },
                        onDismiss = { expandedWarrantDropdown = false },
                        onSelect = { viewModel.updateCaseForm(warrantType = it); expandedWarrantDropdown = false },
                        testTag = "dropdown_warrant"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FieldLabel("WARRANT REFERENCE NUMBER")
                    OutlinedTextField(
                        value = formState.warrantReference,
                        onValueChange = { viewModel.updateCaseForm(warrantReference = it) },
                        modifier = Modifier.fillMaxWidth().testTag("input_warrant_ref"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("e.g. WARR-2025-091", color = Color(0xFF475569)) },
                        colors = fieldColors()
                    )
                }
            }

            // ── SECTION 5: EXAMINER COMPETENCE ────────────────────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp)) },
                    label = "EXAMINER COMPETENCE",
                    labelColor = Color(0xFF8B5CF6)
                ) {
                    FieldLabel("EXAMINER COMPETENCE LEVEL")
                    SimpleDropdown(
                        selected = formState.competenceLevel,
                        options = competenceOptions,
                        expanded = expandedCompetenceDropdown,
                        onExpand = { expandedCompetenceDropdown = true },
                        onDismiss = { expandedCompetenceDropdown = false },
                        onSelect = { viewModel.updateCaseForm(competenceLevel = it); expandedCompetenceDropdown = false },
                        testTag = "dropdown_competence"
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    val hint = when (formState.competenceLevel) {
                        "Basic" -> "Basic: logical / manual methods only; physical extraction requires commercial tool."
                        "Intermediate" -> "Intermediate: can execute file-system extraction and cloud pulls."
                        "Advanced" -> "Advanced: full physical acquisition, chip-off, and custom scripts."
                        else -> ""
                    }
                    if (hint.isNotBlank()) {
                        Text(hint, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    }
                }
            }

            // ── SECTION 6: CHAIN OF CUSTODY ───────────────────────────────────
            item {
                SectionCard(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp)) },
                    label = "CHAIN OF CUSTODY",
                    labelColor = Color(0xFF10B981)
                ) {
                    CustodyCheckRow(
                        label = "Faraday isolation confirmed",
                        subtitle = "Device is shielded from all wireless signals",
                        checked = formState.isFaradayIsolated,
                        onCheckedChange = { viewModel.updateCaseForm(isFaradayIsolated = it) },
                        testTag = "chk_faraday"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustodyCheckRow(
                        label = "Airplane mode confirmed",
                        subtitle = "All radios disabled before extraction",
                        checked = formState.isAirplaneModeConfirmed,
                        onCheckedChange = { viewModel.updateCaseForm(isAirplaneModeConfirmed = it) },
                        testTag = "chk_airplane_mode"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustodyCheckRow(
                        label = "Write-blocker attached",
                        subtitle = "Hardware write protection active before imaging",
                        checked = formState.isWriteBlockerAttached,
                        onCheckedChange = { viewModel.updateCaseForm(isWriteBlockerAttached = it) },
                        testTag = "chk_write_blocker"
                    )
                }
            }

            // ── SECTION 7: RECOMMENDED TOOLS ─────────────────────────────────
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
                        text = "${formState.selectedTools.size} selected",
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

            // ── SAVE BUTTON ───────────────────────────────────────────────────
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
                        text = if (isFormValid) "Create Case & Run Engine" else "Complete required fields",
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
            text = { Text("Your case details and selected tools will be lost.") },
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

// ── Helper composables ─────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    icon: @Composable () -> Unit,
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun SimpleDropdown(
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    testTag: String
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpand() }
                .testTag(testTag),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selected, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(Color(0xFF131D31))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun CustodyCheckRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131D31), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF10B981),
                checkmarkColor = Color.White,
                uncheckedColor = Color(0xFF64748B)
            )
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
