package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onCenterPlusClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_bottom_bar"),
        color = Color(0xFF090E17),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF162032))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home tab
                BottomNavItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { onNavigate(AppScreen.DASHBOARD) },
                    modifier = Modifier.weight(1f)
                )

                // Cases tab
                BottomNavItem(
                    title = "Cases",
                    icon = Icons.Default.Folder,
                    isSelected = currentScreen == AppScreen.CASE_REGISTRY,
                    onClick = { onNavigate(AppScreen.CASE_REGISTRY) },
                    modifier = Modifier.weight(1f)
                )

                // Space for Center Floating Button
                Spacer(modifier = Modifier.weight(1f))

                // Tools tab
                BottomNavItem(
                    title = "Tools",
                    icon = Icons.Default.Build,
                    isSelected = currentScreen == AppScreen.TOOL_INVENTORY,
                    onClick = { onNavigate(AppScreen.TOOL_INVENTORY) },
                    modifier = Modifier.weight(1f)
                )

                // Profile tab
                BottomNavItem(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    isSelected = currentScreen == AppScreen.PROFILE,
                    onClick = { onNavigate(AppScreen.PROFILE) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Center Blue Plus Button for New Case
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .size(52.dp)
                    .background(Color(0xFF2563EB), CircleShape)
                    .border(2.dp, Color(0xFF090E17), CircleShape)
                    .clickable { onCenterPlusClick() }
                    .testTag("center_plus_action"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Case",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag("nav_item_${title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}
