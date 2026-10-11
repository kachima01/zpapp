package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.OfficerEntity
import com.example.ui.components.DetailRow
import com.example.ui.components.SectionCard

@Composable
fun ProfileScreen(
    currentOfficer: OfficerEntity?,
    totalCasesCount: Int,
    onLogout: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenLegalFramework: () -> Unit
) {
    val name = currentOfficer?.fullName ?: "Officer Amon Bili"
    val role = currentOfficer?.role ?: "Investigator"
    val id = currentOfficer?.officerId ?: "ZP-8842"
    val station = currentOfficer?.station ?: "Forensic Laboratory Unit, Police HQ Lusaka"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A101D))
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Avatar and Name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF13233F), CircleShape)
                    .border(2.dp, Color(0xFF2563EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$role • $id",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF38BDF8),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Officer Info Card
        SectionCard(title = "OFFICER PROFILE & CREDENTIALS", icon = Icons.Default.Security) {
            DetailRow(label = "Full Name", value = name)
            DetailRow(label = "Officer ID / Badge", value = id, isMonospace = true)
            DetailRow(label = "Operational Role", value = role)
            DetailRow(label = "Station / Unit", value = station)
            DetailRow(label = "Total Cases Logged", value = "$totalCasesCount Cases")
        }

        // Offline Database Card
        SectionCard(title = "OFFLINE STORAGE INTEGRITY", icon = Icons.Default.Storage) {
            DetailRow(label = "Database Engine", value = "Local Room SQLite (Encrypted / On-Device)")
            DetailRow(label = "Network Status", value = "Air-Gapped / Offline Capable")
            DetailRow(label = "Evidentiary Standard", value = "ISO/IEC 27037 Contemporaneous Logging")
        }

        SectionCard(title = "REFERENCE & GOVERNANCE", icon = Icons.Default.Shield) {
            Text(
                text = "Consult legal authority, tool guidance, and the decision-support framework without leaving the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenReference, modifier = Modifier.weight(1f)) { Text("Reference library") }
                OutlinedButton(onClick = onOpenLegalFramework, modifier = Modifier.weight(1f)) { Text("Legal framework") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_logout"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFFF87171)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out Officer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
