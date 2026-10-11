package com.example.ui.screens.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReferenceItem(
    val title: String,
    val description: String,
    val category: String // "LEGAL", "TOOLS", "NIST"
)

@Composable
fun ReferenceLibraryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("LEGAL") } // "LEGAL", "TOOLS", "NIST"

    val allReferences = remember {
        listOf(
            // LEGAL
            ReferenceItem(
                title = "Zambia Cyber Security and Cyber Crimes Act, 2021",
                description = "Section 41: Search and Seizure of Computer Systems. Outlines legal authority for mobile device acquisition.",
                category = "LEGAL"
            ),
            ReferenceItem(
                title = "Electronic Communications and Transactions Act, 2009",
                description = "Rules for the admissibility of electronic evidence in Zambian courts and establishing chain of custody.",
                category = "LEGAL"
            ),
            ReferenceItem(
                title = "Criminal Procedure Code (CPC) Act",
                description = "General provisions for warrants and securing a crime scene, applicable to digital devices.",
                category = "LEGAL"
            ),
            ReferenceItem(
                title = "Criminal Procedure Code - Section 118",
                description = "Magistrate issuance of search warrants. Execution rules and evidentiary boundaries.",
                category = "LEGAL"
            ),
            // TOOLS
            ReferenceItem(
                title = "Cellebrite UFED & Physical Analyzer",
                description = "Gold standard for physical acquisition, lock bypass, and encrypted app decoding.",
                category = "TOOLS"
            ),
            ReferenceItem(
                title = "MSAB XRY & XAMN",
                description = "Field-ready mobile forensics suite for secure logical and physical extraction from phones and SIM cards.",
                category = "TOOLS"
            ),
            ReferenceItem(
                title = "Oxygen Forensics Detective",
                description = "Specializes in device lock bypass, app database extraction, and direct live cloud account pulls.",
                category = "TOOLS"
            ),
            ReferenceItem(
                title = "Magnet AXIOM & Belkasoft X",
                description = "All-in-one timeline correlation aggregating mobile data, memory dumps, and cloud evidence.",
                category = "TOOLS"
            ),
            ReferenceItem(
                title = "Autopsy & Volatility Framework",
                description = "Open-source disk analysis and volatile RAM memory dump triage.",
                category = "TOOLS"
            ),
            // NIST
            ReferenceItem(
                title = "ISO/IEC 27037:2012 Standard",
                description = "Guidelines for digital evidence identification, collection, acquisition, and preservation under chain of custody.",
                category = "NIST"
            ),
            ReferenceItem(
                title = "NIST SP 800-101 Rev. 1",
                description = "Guidelines on Mobile Device Forensics: Preservation, physical acquisition, and reporting standards.",
                category = "NIST"
            ),
            ReferenceItem(
                title = "ISO/IEC 27042 Evidentiary SOP",
                description = "Guidelines for the analysis and interpretation of digital evidence.",
                category = "NIST"
            )
        )
    }

    val filteredList = allReferences.filter {
        val matchesTab = it.category == selectedTab
        val matchesQuery = if (searchQuery.isBlank()) true else {
            it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
        }
        matchesTab && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A101D))
            .padding(16.dp)
            .testTag("reference_library_screen")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search guidelines, laws, SOPs...", color = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_guidelines"),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131D31),
                unfocusedContainerColor = Color(0xFF131D31),
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF38BDF8)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter tabs: Legal | Tools | NIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterTabPill(
                title = "Legal",
                icon = Icons.Default.Gavel,
                isSelected = selectedTab == "LEGAL",
                onClick = { selectedTab = "LEGAL" },
                modifier = Modifier.weight(1f)
            )
            FilterTabPill(
                title = "Tools",
                icon = Icons.Default.Build,
                isSelected = selectedTab == "TOOLS",
                onClick = { selectedTab = "TOOLS" },
                modifier = Modifier.weight(1f)
            )
            FilterTabPill(
                title = "NIST",
                icon = Icons.Default.Description,
                isSelected = selectedTab == "NIST",
                onClick = { selectedTab = "NIST" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cards list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Blue document icon in rounded square
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF13233F), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(
                if (isSelected) Color(0xFF2563EB) else Color(0xFF131D31),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("filter_tab_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
