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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomBar
import com.example.ui.screens.assessment.AssessmentWizardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.SecurityDashboardScreen
import com.example.ui.screens.inventory.ToolInventoryScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reference.ReferenceLibraryScreen
import com.example.ui.screens.framework.LegalFrameworkScreen
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
    PROFILE,
    REFERENCE_LIBRARY,
    LEGAL_FRAMEWORK
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
    var detailReturnScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

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

    val topLevelScreens = setOf(
        AppScreen.DASHBOARD,
        AppScreen.CASE_REGISTRY,
        AppScreen.TOOL_INVENTORY,
        AppScreen.PROFILE
    )
    val isFocusedWorkflow = currentScreen !in topLevelScreens

    fun startNewCase() {
        viewModel.initNewCaseForm()
        currentScreen = AppScreen.ASSESSMENT_WIZARD
    }

    // Preserve the user's context when they leave a detail screen.
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = when (currentScreen) {
            AppScreen.DECISION_DETAIL -> detailReturnScreen
            AppScreen.REFERENCE_LIBRARY, AppScreen.LEGAL_FRAMEWORK -> AppScreen.PROFILE
            else -> AppScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (!isFocusedWorkflow) AppBottomBar(currentScreen) { screen -> currentScreen = screen }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.CASE_REGISTRY) {
                ExtendedFloatingActionButton(
                    onClick = { startNewCase() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New case") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("btn_initialize_new_case")
                )
            }
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
                            startNewCase()
                        },
                        onViewAllCases = {
                            currentScreen = AppScreen.CASE_REGISTRY
                        },
                        onViewInventory = {
                            currentScreen = AppScreen.TOOL_INVENTORY
                        },
                        onRecordClick = { id ->
                            selectedRecordId = id
                            detailReturnScreen = AppScreen.DASHBOARD
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
                        onBack = { currentScreen = detailReturnScreen },
                        onDelete = { id ->
                            viewModel.deleteRecord(id)
                            currentScreen = AppScreen.DASHBOARD
                        },
                        onUpdateSupervisor = { id, status, notes ->
                            viewModel.updateSupervisorReview(id, status, notes)
                        },
                        canReview = currentOfficer?.role.equals("Supervisor", ignoreCase = true)
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
                            detailReturnScreen = AppScreen.CASE_REGISTRY
                            currentScreen = AppScreen.DECISION_DETAIL
                        },
                        onStartNewAssessment = {
                            startNewCase()
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
                        },
                        onOpenReference = { currentScreen = AppScreen.REFERENCE_LIBRARY },
                        onOpenLegalFramework = { currentScreen = AppScreen.LEGAL_FRAMEWORK }
                    )
                }

                AppScreen.REFERENCE_LIBRARY -> ReferenceLibraryScreen(onBack = { currentScreen = AppScreen.PROFILE })

                AppScreen.LEGAL_FRAMEWORK -> LegalFrameworkScreen(onBack = { currentScreen = AppScreen.PROFILE })
            }
        }
    }
}
