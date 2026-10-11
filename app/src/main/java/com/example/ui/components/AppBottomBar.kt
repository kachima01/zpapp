package com.example.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.AppScreen

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_bottom_bar")
    ) {
        BottomNavItem("Home", Icons.Default.Home, currentScreen == AppScreen.DASHBOARD) { onNavigate(AppScreen.DASHBOARD) }
        BottomNavItem("Cases", Icons.Default.Folder, currentScreen == AppScreen.CASE_REGISTRY) { onNavigate(AppScreen.CASE_REGISTRY) }
        BottomNavItem("Tools", Icons.Default.Build, currentScreen == AppScreen.TOOL_INVENTORY) { onNavigate(AppScreen.TOOL_INVENTORY) }
        BottomNavItem("Profile", Icons.Default.Person, currentScreen == AppScreen.PROFILE) { onNavigate(AppScreen.PROFILE) }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = title) },
        label = { Text(title, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.testTag("nav_item_${title.lowercase()}")
    )
}
