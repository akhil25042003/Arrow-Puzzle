package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LevelProgressEntity
import com.example.ui.theme.LocalGamePalette

@Composable
fun LevelSelectDialog(
    currentLevel: Long,
    highestUnlocked: Long,
    progressList: List<LevelProgressEntity>,
    onSelectLevel: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalGamePalette.current
    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelNumber }
    }

    var customLevelText by remember { mutableStateOf("") }

    // Display levels window around highest unlocked (e.g. 1 to max(highestUnlocked + 10, 40))
    val maxDisplay = maxOf(highestUnlocked + 15, 40L)
    val levelNumbers = (1L..maxDisplay).toList()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceColor)
                .border(2.dp, palette.boardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELECT LEVEL",
                            color = palette.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Unlimited Procedural Levels",
                            color = palette.accentPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick jump to any level input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customLevelText,
                        onValueChange = { customLevelText = it.filter { ch -> ch.isDigit() } },
                        placeholder = {
                            Text(
                                "Enter any Level # (e.g. 500)",
                                fontSize = 13.sp,
                                color = palette.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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
                            .testTag("jump_level_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = customLevelText.toLongOrNull()
                            if (target != null && target >= 1) {
                                onSelectLevel(target)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("jump_level_submit")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Go",
                            tint = Color(0xFF0F0C20)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grid of levels
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(levelNumbers) { num ->
                        val isCurrent = num == currentLevel
                        val isUnlocked = num <= highestUnlocked
                        val progress = progressMap[num]
                        val stars = progress?.stars ?: 0

                        val bg = when {
                            isCurrent -> palette.accentPrimary.copy(alpha = 0.25f)
                            isUnlocked -> palette.boardBackground
                            else -> palette.surfaceColor.copy(alpha = 0.4f)
                        }

                        val border = when {
                            isCurrent -> palette.accentPrimary
                            isUnlocked -> palette.boardBorder
                            else -> palette.boardBorder.copy(alpha = 0.3f)
                        }

                        Box(
                            modifier = Modifier
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(if (isCurrent) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
                                .clickable(enabled = isUnlocked) {
                                    onSelectLevel(num)
                                    onDismiss()
                                }
                                .padding(4.dp)
                                .testTag("level_item_$num"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = "$num",
                                        color = if (isCurrent) palette.accentPrimary else palette.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // Stars
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                        for (s in 1..3) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (s <= stars) Color(0xFFFFD700) else palette.textSecondary.copy(alpha = 0.25f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = palette.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$num",
                                        color = palette.textSecondary.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
