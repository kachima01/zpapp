package com.example.ui.screens.framework

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BadgeType
import com.example.ui.components.HighContrastBadge
import com.example.ui.components.TacticalHeader
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ForensicCyanPrimary
import com.example.ui.theme.HighContrastBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

@Composable
fun LegalFrameworkScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("legal_framework_screen")
    ) {
        TacticalHeader(
            title = "Zambia Legal Framework",
            subtitle = "ZCMFF Statutory Guidelines & Evidentiary Standards",
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
            // RESEARCH CITATION CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, HighContrastBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESEARCH & INSTITUTIONAL BASIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForensicCyanPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            HighContrastBadge(text = "ICU 2026", type = BadgeType.PRIMARY)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Zambia Context-Aware Mobile Forensic Framework (ZCMFF)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Amon Bili · Student ID: 2105207771\nInformation and Communications University (ICU)\nSchool of Engineering · Department of ICT",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            // CORE PURPOSE & SCOPE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, CompliantGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CORE MANDATE: GUIDANCE, NOT EXTRACTION",
                                style = MaterialTheme.typography.labelMedium,
                                color = CompliantGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This application never touches or connects to the evidence device. Its sole purpose is decision support and contemporaneous documentation. Before any acquisition begins, the investigator assesses resources, device characteristics, competence, and legal authority to generate an audit-proof Decision Record.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryDark,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // ZAMBIAN STATUTORY PROVISIONS
            item {
                Text(
                    text = "ZAMBIAN STATUTORY AUTHORITIES",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace
                )
            }

            item {
                StatuteCard(
                    statute = "Criminal Procedure Code (Cap 88) - Section 118",
                    scope = "Search Warrant & Seizure Authority",
                    detail = "Empowers magistrates and authorized officers to issue warrants for the search of property or devices believed to contain evidentiary material. Extraction methods must remain strictly within the four corners of the warrant."
                )
            }

            item {
                StatuteCard(
                    statute = "Cyber Security and Cyber Crimes Act No. 2 of 2021",
                    scope = "Digital Evidence & Data Preservation",
                    detail = "Governs cybercrime investigations, production orders, expedited preservation of computer data, and lawful acquisition of electronic records. Requires strict verification before seizing or extracting remote cloud accounts."
                )
            }

            item {
                StatuteCard(
                    statute = "Electronic Communications and Transactions Act No. 4 of 2021",
                    scope = "Functional Equivalence & Court Admissibility",
                    detail = "Codifies the legal recognition and admissibility of electronic records. Mandates contemporaneous recording of evidence acquisition to withstand courtroom cross-examination."
                )
            }

            item {
                StatuteCard(
                    statute = "ISO/IEC 27037:2012 Forensics Standard",
                    scope = "Digital Evidence Handling Principles",
                    detail = "International benchmark for digital evidence identification, collection, acquisition, and preservation. Requires write-blocking, RF isolation (Faraday bag), and strict chain-of-custody documentation."
                )
            }

            // ZCMFF ARCHITECTURAL LAYERS
            item {
                Text(
                    text = "ZCMFF 5-LAYER ARCHITECTURE",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace
                )
            }

            item {
                LayerCard(layer = "Layer 1: Context Assessment", role = "Operationalised in this app", desc = "Structured capture of tool inventory, device characterisation, competence, and legal authority.")
            }
            item {
                LayerCard(layer = "Layer 2: Acquisition Decision", role = "Operationalised in this app", desc = "Deterministic decision rules selecting the highest feasible, lawful method, with explicit rejection reasons.")
            }
            item {
                LayerCard(layer = "Layer 3: Evidence Collection", role = "Carried out via approved forensic tools", desc = "Physical, logical, or file-system acquisition performed on external workstations (Cellebrite, XRY, Oxygen, AXIOM).")
            }
            item {
                LayerCard(layer = "Layer 4: Evidence Analysis", role = "Carried out via forensic suites", desc = "Parsing databases, timeline correlation, carving unallocated space.")
            }
            item {
                LayerCard(layer = "Layer 5: Evidentiary Reporting", role = "Submitted to Court of Law", desc = "Final court package including the contemporaneous Decision Record generated in this app.")
            }
        }
    }
}

@Composable
fun StatuteCard(statute: String, scope: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, HighContrastBorder),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statute,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = ForensicCyanPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = scope,
                style = MaterialTheme.typography.labelSmall,
                color = ForensicCyanPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun LayerCard(layer: String, role: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, HighContrastBorder),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = layer,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                HighContrastBadge(
                    text = role,
                    type = if (role.contains("this app")) BadgeType.SUCCESS else BadgeType.NEUTRAL
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark
            )
        }
    }
}
