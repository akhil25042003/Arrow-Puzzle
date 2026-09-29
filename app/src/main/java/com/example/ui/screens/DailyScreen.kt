package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.BoardView
import com.example.ui.components.GameHud
import com.example.ui.components.WinCelebrationDialog
import com.example.ui.theme.LocalGamePalette

@Composable
fun DailyScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current

    val completedTiers = remember(state.dailyRecords) {
        state.dailyRecords.filter { it.completed }.map { it.tier }.toSet()
    }

    val tiers = listOf(
        Triple(1, "Morning", "4x4"),
        Triple(2, "Afternoon", "5x5"),
        Triple(3, "Zenith", "6x6")
    )

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
                title = state.currentLevel.name,
                subtitle = "Daily ${state.dailyDateString} • ${state.currentLevel.rows}x${state.currentLevel.cols}",
                levelNumber = state.currentLevel.levelNumber,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                elapsedSeconds = state.elapsedSeconds,
                canUndo = state.canUndo,
                onUndoClicked = { viewModel.undoMove() },
                onResetClicked = { viewModel.restartCurrentLevel() },
                onHintClicked = { viewModel.requestHint() },
                onLevelSelectClicked = null
            )

            // 3 Tier Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((tierId, label, sizeText) in tiers) {
                    val isSelected = tierId == state.dailyTier
                    val isCompleted = completedTiers.contains(tierId)

                    val bg = when {
                        isSelected -> palette.accentPrimary
                        isCompleted -> palette.surfaceColor
                        else -> palette.surfaceColor.copy(alpha = 0.6f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (isSelected) palette.accentPrimary else palette.boardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.loadDailyLevel(tierId) }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .testTag("daily_tier_$tierId"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = if (isSelected) Color(0xFF0F0C20) else Color(0xFF00E676),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF0F0C20) else palette.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = sizeText,
                                color = if (isSelected) Color(0xFF0F0C20).copy(alpha = 0.7f) else palette.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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

        // Completion Dialog
        if (state.isLevelComplete) {
            WinCelebrationDialog(
                levelName = "Daily: ${state.currentLevel.name}",
                starsEarned = 3,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                timeSeconds = state.elapsedSeconds,
                onNextLevel = {
                    val nextTier = if (state.dailyTier < 3) state.dailyTier + 1 else 1
                    viewModel.loadDailyLevel(nextTier)
                },
                onReplay = { viewModel.restartCurrentLevel() },
                onDismiss = { /* do nothing */ }
            )
        }
    }
}
