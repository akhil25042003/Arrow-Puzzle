package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.screens.BlitzScreen
import com.example.ui.screens.CampaignScreen
import com.example.ui.screens.DailyScreen
import com.example.ui.screens.SeedLabScreen
import com.example.ui.screens.ZenScreen
import com.example.ui.theme.LocalGamePalette
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.getGamePalette

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CAMPAIGN("Campaign", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle, "nav_tab_campaign"),
    ZEN("Zen", Icons.Filled.Spa, Icons.Outlined.Spa, "nav_tab_zen"),
    BLITZ("Blitz", Icons.Filled.Bolt, Icons.Outlined.Bolt, "nav_tab_blitz"),
    DAILY("Daily", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_tab_daily"),
    LAB("Lab", Icons.Filled.Science, Icons.Outlined.Science, "nav_tab_lab")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
            val palette = getGamePalette(uiState.themeIndex)

            CompositionLocalProvider(LocalGamePalette provides palette) {
                MyApplicationTheme(darkTheme = palette.isDark) {
                    ArrowPuzzleApp(gameViewModel = gameViewModel)
                }
            }
        }
    }
}

@Composable
fun ArrowPuzzleApp(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalGamePalette.current
    var currentTab by rememberSaveable { mutableStateOf(AppTab.CAMPAIGN) }

    // Custom back navigation handling: if on secondary tab, back brings to Campaign
    BackHandler(enabled = currentTab != AppTab.CAMPAIGN) {
        currentTab = AppTab.CAMPAIGN
        gameViewModel.loadCampaignLevel(uiState.currentLevel.levelNumber)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = palette.surfaceColor,
                contentColor = palette.textPrimary
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentTab != tab) {
                                currentTab = tab
                                when (tab) {
                                    AppTab.CAMPAIGN -> gameViewModel.loadCampaignLevel(uiState.currentLevel.levelNumber)
                                    AppTab.ZEN -> gameViewModel.loadZenLevel()
                                    AppTab.BLITZ -> gameViewModel.startBlitzMode()
                                    AppTab.DAILY -> gameViewModel.loadDailyLevel(1)
                                    AppTab.LAB -> {}
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = palette.accentPrimary,
                            selectedTextColor = palette.accentPrimary,
                            indicatorColor = palette.accentPrimary.copy(alpha = 0.18f),
                            unselectedIconColor = palette.textSecondary,
                            unselectedTextColor = palette.textSecondary
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (currentTab) {
                AppTab.CAMPAIGN -> CampaignScreen(state = uiState, viewModel = gameViewModel)
                AppTab.ZEN -> ZenScreen(state = uiState, viewModel = gameViewModel)
                AppTab.BLITZ -> BlitzScreen(state = uiState, viewModel = gameViewModel)
                AppTab.DAILY -> DailyScreen(state = uiState, viewModel = gameViewModel)
                AppTab.LAB -> SeedLabScreen(state = uiState, viewModel = gameViewModel)
            }
        }
    }
}
