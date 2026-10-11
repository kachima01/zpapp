package com.example.ui.screens.assessment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
    var isSaving by remember { mutableStateOf(false) }

    val crimeOptions = listOf(
        "Mobile Money Fraud",
        "Computer Hacking & Intrusion",
        "Active Malware / Live Infection",
        "Financial Theft & Embezzlement",
        "Defamation / Hate Speech",
        "General Digital Evidence"
    )

    val deviceOptions = listOf(
        "Smartphone / Mobile",
        "Computer / Server / HDD",
        "Volatile RAM & Memory",
        "Cross-Platform & Cloud Accounts"
    )

    val lockOptions = listOf(
        "Passcode Locked",
        "BitLocker / Full-Disk Encrypted",
        "Biometric / Secure Enclave",
        "Unlocked (Live Session)",
        "Pattern / PIN"
    )

    data class LegalAccessChoice(
        val title: String,
        val subtitle: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val legalAccessOptions = listOf(
        LegalAccessChoice("Consent", "Voluntary signed owner / subject consent", Icons.Default.CheckCircle),
        LegalAccessChoice("Court Order", "Judicial production order or high court directive", Icons.Default.Description),
        LegalAccessChoice("Warrant", "Formal judicial search warrant (Sec 118 CPC)", Icons.Default.Gavel),
        LegalAccessChoice("Exigent Circumstances", "Emergency authority to prevent evidence destruction", Icons.Default.Warning)
    )

    fun performSave() {
        if (isSaving) return
        isSaving = true
        viewModel.saveCaseRecord { recordId ->
            onComplete(recordId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .testTag("assessment_wizard_screen")
    ) {
        // TOP APP BAR (SINGLE SAVE BUTTON REMAINS AT THE BOTTOM)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                        text = "Tool Allocation & Assessment",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SECTION 1: CASE & DEVICE DETAILS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CASE & EVIDENCE PROFILE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Case number and Docket
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.caseNumber,
                                onValueChange = { viewModel.updateCaseForm(caseNumber = it) },
                                label = { Text("Case Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("case_number_input"),
                                colors = inputFieldColors()
                            )
                            OutlinedTextField(
                                value = formState.incidentReference,
                                onValueChange = { viewModel.updateCaseForm(incidentRef = it) },
                                label = { Text("Docket Ref") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("incident_ref_input"),
                                colors = inputFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Crime Type Dropdown
                        Text("Crime Category", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        DropdownSelector(
                            selected = formState.crimeType,
                            options = crimeOptions,
                            expanded = expandedCrimeDropdown,
                            onExpand = { expandedCrimeDropdown = true },
                            onDismiss = { expandedCrimeDropdown = false },
                            onSelect = {
                                viewModel.updateCaseForm(crimeType = it)
                                expandedCrimeDropdown = false
                            },
                            testTag = "crime_type_dropdown"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Device / Evidence Type Dropdown
                        Text("Evidence / Device Type", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        DropdownSelector(
                            selected = formState.deviceType,
                            options = deviceOptions,
                            expanded = expandedDeviceDropdown,
                            onExpand = { expandedDeviceDropdown = true },
                            onDismiss = { expandedDeviceDropdown = false },
                            onSelect = { selectedType ->
                                // Automatically adapt make/model defaults for convenience
                                val (defaultMake, defaultModel) = when {
                                    selectedType.contains("Computer", ignoreCase = true) -> "Dell PowerEdge" to "R740 Rack Server"
                                    selectedType.contains("RAM", ignoreCase = true) -> "Lenovo ThinkPad" to "P1 Workstation (Live RAM)"
                                    selectedType.contains("Cross-Platform", ignoreCase = true) -> "Apple MacBook & iPhone" to "MacBook Pro M2 & iPhone 15"
                                    else -> "Samsung" to "Galaxy S22"
                                }
                                viewModel.updateCaseForm(
                                    deviceType = selectedType,
                                    deviceMake = defaultMake,
                                    deviceModel = defaultModel
                                )
                                expandedDeviceDropdown = false
                            },
                            testTag = "device_type_dropdown"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Device Make & Model
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.deviceMake,
                                onValueChange = { viewModel.updateCaseForm(deviceMake = it) },
                                label = { Text("Make") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("device_make_input"),
                                colors = inputFieldColors()
                            )
                            OutlinedTextField(
                                value = formState.deviceModel,
                                onValueChange = { viewModel.updateCaseForm(deviceModel = it) },
                                label = { Text("Model / Specs") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("device_model_input"),
                                colors = inputFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lock Status Dropdown
                        Text("Security / Lock Status", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        DropdownSelector(
                            selected = formState.lockStatus,
                            options = lockOptions,
                            expanded = expandedLockDropdown,
                            onExpand = { expandedLockDropdown = true },
                            onDismiss = { expandedLockDropdown = false },
                            onSelect = {
                                viewModel.updateCaseForm(lockStatus = it)
                                expandedLockDropdown = false
                            },
                            testTag = "lock_status_dropdown"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Legal Access Button & Dropdown
                        Text("Authorization Basis", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expandedWarrantDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("legal_access_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF131D31),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Gavel,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Legal Access",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF1E293B),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF334155))
                                        ) {
                                            Text(
                                                text = formState.warrantType.ifBlank { "Warrant" },
                                                color = Color(0xFF38BDF8),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand Legal Access",
                                            tint = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = expandedWarrantDropdown,
                                onDismissRequest = { expandedWarrantDropdown = false },
                                modifier = Modifier
                                    .background(Color(0xFF0F172A))
                                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(8.dp))
                            ) {
                                legalAccessOptions.forEach { choice ->
                                    DropdownMenuItem(
                                        text = {
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = choice.title,
                                                        color = if (formState.warrantType == choice.title) Color(0xFF38BDF8) else Color.White,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (formState.warrantType == choice.title) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color(0xFF10B981),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = choice.subtitle,
                                                    color = Color(0xFF94A3B8),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = choice.icon,
                                                contentDescription = null,
                                                tint = if (formState.warrantType == choice.title) Color(0xFF38BDF8) else Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            viewModel.updateCaseForm(warrantType = choice.title)
                                            expandedWarrantDropdown = false
                                        },
                                        modifier = Modifier.testTag("legal_access_option_${choice.title.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: CASE-ALLOCATED TOOLS (ALLOCATED DYNAMICALLY FOR THIS CASE!)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOOLS ALLOCATED FOR THIS CASE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${formState.selectedTools.size} active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF38BDF8),
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
                            .padding(12.dp),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = alloc.toolName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (alloc.isPrimaryAllocation) Color(0xFF065F46) else Color(0xFF1E293B),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (alloc.isPrimaryAllocation) "PRIMARY" else "SUPPORTING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (alloc.isPrimaryAllocation) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

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

            // SECTION 3: CUSTODY & ISOLATION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHAIN OF CUSTODY INTEGRITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF59E0B),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        CustodySwitchRow(
                            label = "Faraday Bag Isolation",
                            subtitle = "Prevents remote wipe signals",
                            checked = formState.isFaradayIsolated,
                            onCheckedChange = { viewModel.updateCaseForm(isFaradayIsolated = it) },
                            testTag = "switch_faraday"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustodySwitchRow(
                            label = "Airplane Mode / Network Severed",
                            subtitle = "Confirmed zero radio transmission",
                            checked = formState.isAirplaneModeConfirmed,
                            onCheckedChange = { viewModel.updateCaseForm(isAirplaneModeConfirmed = it) },
                            testTag = "switch_airplane"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // STICKY BOTTOM SAVE ACTION BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .border(BorderStroke(1.dp, Color(0xFF1E293B)))
                .padding(16.dp)
        ) {
            Button(
                onClick = { performSave() },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_case_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaving) "Saving to SQLite…" else "Save Case & Allocate Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DropdownSelector(
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
private fun CustodySwitchRow(
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun inputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF131D31),
    unfocusedContainerColor = Color(0xFF131D31),
    focusedBorderColor = Color(0xFF2563EB),
    unfocusedBorderColor = Color(0xFF1E293B),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF38BDF8)
)
