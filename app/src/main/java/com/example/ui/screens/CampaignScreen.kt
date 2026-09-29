package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.BoardView
import com.example.ui.components.GameHud
import com.example.ui.components.LevelSelectDialog
import com.example.ui.components.WinCelebrationDialog

@Composable
fun CampaignScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var showLevelSelect by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD
            GameHud(
                title = "Level ${state.currentLevel.levelNumber}",
                subtitle = "${state.currentLevel.rows}x${state.currentLevel.cols} Grid • ${state.currentCells.values.count { it is com.example.game.CellItem.Arrow }} Arrows",
                levelNumber = state.currentLevel.levelNumber,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                elapsedSeconds = state.elapsedSeconds,
                canUndo = state.canUndo,
                onUndoClicked = { viewModel.undoMove() },
                onResetClicked = { viewModel.restartCurrentLevel() },
                onHintClicked = { viewModel.requestHint() },
                onLevelSelectClicked = { showLevelSelect = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Board
            BoardView(
                level = state.currentLevel,
                cells = state.currentCells,
                flyingArrows = state.flyingArrows,
                bumpMap = state.bumpMap,
                hintedPosition = state.hintedPosition,
                onCellClicked = { viewModel.onCellClicked(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            )
        }

        // Level Completion Celebration Dialog
        if (state.isLevelComplete) {
            WinCelebrationDialog(
                levelName = "Level ${state.currentLevel.levelNumber}",
                starsEarned = state.earnedStars,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                timeSeconds = state.elapsedSeconds,
                onNextLevel = { viewModel.nextCampaignLevel() },
                onReplay = { viewModel.restartCurrentLevel() },
                onDismiss = { /* Keep dialog visible until player decides */ }
            )
        }

        // Level Selector Modal
        if (showLevelSelect) {
            LevelSelectDialog(
                currentLevel = state.currentLevel.levelNumber,
                highestUnlocked = state.playerStats.highestUnlockedLevel,
                progressList = state.levelProgressList,
                onSelectLevel = { viewModel.loadCampaignLevel(it) },
                onDismiss = { showLevelSelect = false }
            )
        }
    }
}
