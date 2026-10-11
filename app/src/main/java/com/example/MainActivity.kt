package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomBar
import com.example.ui.screens.assessment.AssessmentWizardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.SecurityDashboardScreen
import com.example.ui.screens.inventory.ToolInventoryScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.record.DecisionRecordDetailScreen
import com.example.ui.screens.registry.CaseRegistryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ForensicViewModel

enum class AppScreen {
    DASHBOARD,
    ASSESSMENT_WIZARD,
    DECISION_DETAIL,
    CASE_REGISTRY,
    TOOL_INVENTORY,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZcmffAppRoot()
            }
        }
    }
}

@Composable
fun ZcmffAppRoot(
    viewModel: ForensicViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentOfficer by viewModel.currentOfficer.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var selectedRecordId by remember { mutableLongStateOf(-1L) }

    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val tools by viewModel.allTools.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()

    val selectedRecord = remember(records, selectedRecordId) {
        records.find { it.id == selectedRecordId }
    }

    if (!isLoggedIn) {
        AuthScreen(
            onLogin = { officerId, pass ->
                viewModel.login(officerId, pass) { success, _ -> }
            },
            onQuickLogin = { role ->
                viewModel.quickLoginAs(role)
            }
        )
        return
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17)),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen },
                onCenterPlusClick = {
                    viewModel.initNewCaseForm()
                    currentScreen = AppScreen.ASSESSMENT_WIZARD
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    SecurityDashboardScreen(
                        currentOfficer = currentOfficer,
                        records = records,
                        onInitializeCase = {
                            viewModel.initNewCaseForm()
                            currentScreen = AppScreen.ASSESSMENT_WIZARD
                        },
                        onViewAllCases = {
                            currentScreen = AppScreen.CASE_REGISTRY
                        },
                        onViewInventory = {
                            currentScreen = AppScreen.TOOL_INVENTORY
                        },
                        onRecordClick = { id ->
                            selectedRecordId = id
                            currentScreen = AppScreen.DECISION_DETAIL
                        },
                        onLogout = {
                            viewModel.logout()
                        }
                    )
                }

                AppScreen.ASSESSMENT_WIZARD -> {
                    AssessmentWizardScreen(
                        viewModel = viewModel,
                        allTools = tools,
                        onCancel = { currentScreen = AppScreen.DASHBOARD },
                        onComplete = { recordId ->
                            selectedRecordId = recordId
                            currentScreen = AppScreen.DECISION_DETAIL
                        }
                    )
                }

                AppScreen.DECISION_DETAIL -> {
                    DecisionRecordDetailScreen(
                        recordId = selectedRecordId,
                        initialRecord = selectedRecord,
                        getRecordDirect = { viewModel.getRecordByIdDirect(it) },
                        onBack = { currentScreen = AppScreen.DASHBOARD },
                        onDelete = { id ->
                            viewModel.deleteRecord(id)
                            currentScreen = AppScreen.DASHBOARD
                        },
                        onUpdateSupervisor = { id, status, notes ->
                            viewModel.updateSupervisorReview(id, status, notes)
                        }
                    )
                }

                AppScreen.CASE_REGISTRY -> {
                    CaseRegistryScreen(
                        records = filteredRecords,
                        searchQuery = searchQuery,
                        filterMethod = filterCategory,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setFilterCategory(it) },
                        onRecordClick = { id ->
                            selectedRecordId = id
                            currentScreen = AppScreen.DECISION_DETAIL
                        },
                        onStartNewAssessment = {
                            viewModel.initNewCaseForm()
                            currentScreen = AppScreen.ASSESSMENT_WIZARD
                        },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.TOOL_INVENTORY -> {
                    ToolInventoryScreen(
                        tools = tools,
                        onToggleTool = { id, available ->
                            viewModel.toggleStationTool(id, available)
                        },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        currentOfficer = currentOfficer,
                        totalCasesCount = records.size,
                        onLogout = {
                            viewModel.logout()
                        }
                    )
                }
            }
        }
    }
}
