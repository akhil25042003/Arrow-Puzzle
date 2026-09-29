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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun ZenScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    val sizes = listOf(3, 4, 5, 6, 7, 8)

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
                title = "Zen Mode",
                subtitle = "Relaxing Endless ${state.zenSize}x${state.zenSize} Board",
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

            // Size Selector Row & New Board Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (s in sizes) {
                        val isSelected = s == state.zenSize
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) palette.accentPrimary else palette.surfaceColor)
                                .border(
                                    1.dp,
                                    if (isSelected) palette.accentPrimary else palette.boardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.loadZenLevel(s) }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("zen_size_$s"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${s}x${s}",
                                color = if (isSelected) Color(0xFF0F0C20) else palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // New Random Board Button
                Button(
                    onClick = { viewModel.loadZenLevel(state.zenSize) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.surfaceColor),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("zen_new_board")
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "New",
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        color = palette.accentPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                levelName = "Zen ${state.zenSize}x${state.zenSize}",
                starsEarned = 3,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                timeSeconds = state.elapsedSeconds,
                onNextLevel = { viewModel.loadZenLevel(state.zenSize) },
                onReplay = { viewModel.restartCurrentLevel() },
                onDismiss = { /* do nothing */ }
            )
        }
    }
}
