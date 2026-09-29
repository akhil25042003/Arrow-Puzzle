package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.BoardView
import com.example.ui.theme.LocalGamePalette

@Composable
fun BlitzScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current

    val timerColor by animateColorAsState(
        targetValue = if (state.blitzTimeRemaining <= 10) Color(0xFFFF2A6D) else palette.accentPrimary,
        label = "timerColor"
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
            // Blitz Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Blitz",
                            tint = timerColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BLITZ RUSH",
                            color = palette.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Score: ${state.blitzScore} • Cleared: ${state.blitzLevelsCleared}",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Giant Timer Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceColor)
                        .border(1.5.dp, timerColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time",
                            tint = timerColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.blitzTimeRemaining}s",
                            color = timerColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Combo Banner if combo > 1
            if (state.comboCount > 1) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.accentPrimary.copy(alpha = 0.2f))
                        .border(1.dp, palette.accentPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMBO x${state.comboCount}! +${state.comboCount * 10} pts",
                        color = palette.accentPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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

            // Start / Restart button if not active
            if (!state.isBlitzActive && !state.isBlitzGameOver) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startBlitzMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_blitz_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = Color(0xFF0F0C20)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START 60s BLITZ",
                        color = Color(0xFF0F0C20),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Game Over Dialog
        if (state.isBlitzGameOver) {
            Dialog(onDismissRequest = { viewModel.startBlitzMode() }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.surfaceColor)
                        .border(2.dp, palette.boardBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2A6D).copy(alpha = 0.2f))
                                .border(2.dp, Color(0xFFFF2A6D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "TIME'S UP!",
                            color = palette.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Score: ${state.blitzScore}",
                            color = palette.accentPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Boards Cleared: ${state.blitzLevelsCleared}",
                            color = palette.textSecondary,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Best High Score: ${maxOf(state.blitzScore, state.playerStats.blitzHighScore)}",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.startBlitzMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("blitz_play_again")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Play Again",
                                tint = Color(0xFF0F0C20)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play Again",
                                color = Color(0xFF0F0C20),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
