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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.BoardView
import com.example.ui.components.GameHud
import com.example.ui.components.WinCelebrationDialog
import com.example.ui.theme.AvailablePalettes
import com.example.ui.theme.LocalGamePalette

@Composable
fun SeedLabScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    var seedInput by remember { mutableStateOf("ARROW99") }
    var selectedSize by remember { mutableIntStateOf(5) }
    var isPlayingBoard by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEED LAB & SETTINGS",
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Custom Seed Section Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Generate from Custom Seed",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter any word or number to create a deterministic, shareable level!",
                        color = palette.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = seedInput,
                            onValueChange = { seedInput = it },
                            placeholder = { Text("e.g. COSMOS, HERO", color = palette.textSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accentPrimary,
                                unfocusedBorderColor = palette.boardBorder,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("seed_text_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (seedInput.isNotBlank()) {
                                    viewModel.loadCustomSeedLevel(seedInput, selectedSize)
                                    isPlayingBoard = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("seed_play_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF0F0C20)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Size selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grid Size: ${selectedSize}x${selectedSize}",
                            color = palette.textSecondary,
                            fontSize = 13.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (s in listOf(4, 5, 6, 7)) {
                                val isSel = s == selectedSize
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) palette.accentPrimary else palette.boardBackground)
                                        .border(
                                            1.dp,
                                            if (isSel) palette.accentPrimary else palette.boardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedSize = s }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${s}x${s}",
                                        color = if (isSel) Color(0xFF0F0C20) else palette.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // If user is playing a custom seed board, show the board!
            if (isPlayingBoard) {
                Spacer(modifier = Modifier.height(14.dp))
                GameHud(
                    title = state.currentLevel.name,
                    subtitle = "${state.currentLevel.rows}x${state.currentLevel.cols} Custom Board",
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

                BoardView(
                    level = state.currentLevel,
                    cells = state.currentCells,
                    flyingArrows = state.flyingArrows,
                    bumpMap = state.bumpMap,
                    hintedPosition = state.hintedPosition,
                    onCellClicked = { viewModel.onCellClicked(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Color Themes Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Themes",
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Color Themes",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvailablePalettes.forEachIndexed { index, pal ->
                            val isSelected = index == state.themeIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pal.surfaceColor)
                                    .border(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) palette.accentPrimary else palette.boardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectTheme(index) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp)
                                    .testTag("theme_button_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Mini color dots
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        pal.arrowColors.take(3).forEach { c ->
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(c)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = pal.name.split(" ").first(),
                                        color = pal.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sound & Haptics Toggles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Audio & Haptics",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sound switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Sound",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Chime Synthesizer", color = palette.textPrimary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = state.soundEnabled,
                            onCheckedChange = { viewModel.toggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0F0C20),
                                checkedTrackColor = palette.accentPrimary
                            ),
                            modifier = Modifier.testTag("sound_toggle")
                        )
                    }

                    // Haptic switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Haptics",
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Vibration Feedback", color = palette.textPrimary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = state.hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0F0C20),
                                checkedTrackColor = palette.accentPrimary
                            ),
                            modifier = Modifier.testTag("haptic_toggle")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Player Lifetime Stats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Player Progression",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.playerStats.highestUnlockedLevel}",
                                color = palette.accentPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Max Level", color = palette.textSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.levelProgressList.sumOf { it.stars }}",
                                color = Color(0xFFFFD700),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Stars Earned", color = palette.textSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.playerStats.blitzHighScore}",
                                color = Color(0xFFFF2A6D),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Blitz Best", color = palette.textSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Completion Dialog if custom board solved
        if (state.isLevelComplete && isPlayingBoard) {
            WinCelebrationDialog(
                levelName = state.currentLevel.name,
                starsEarned = 3,
                moves = state.movesCount,
                parMoves = state.currentLevel.parMoves,
                timeSeconds = state.elapsedSeconds,
                onNextLevel = {
                    val nextSeed = "${seedInput}_${(1..99).random()}"
                    seedInput = nextSeed
                    viewModel.loadCustomSeedLevel(nextSeed, selectedSize)
                },
                onReplay = { viewModel.restartCurrentLevel() },
                onDismiss = { isPlayingBoard = false }
            )
        }
    }
}
