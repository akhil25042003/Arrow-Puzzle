package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalGamePalette

@Composable
fun GameHud(
    title: String,
    subtitle: String? = null,
    levelNumber: Long,
    moves: Int,
    parMoves: Int,
    elapsedSeconds: Int,
    canUndo: Boolean,
    onUndoClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onHintClicked: () -> Unit,
    onLevelSelectClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header Row: Level name, Level selector trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    if (onLevelSelectClicked != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, palette.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { onLevelSelectClicked() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("level_select_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = "Levels",
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All",
                                    color = palette.accentPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Timer Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatTime(elapsedSeconds),
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Middle Stats & Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Moves Counter with Par indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceColor)
                    .border(1.dp, palette.boardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Moves: ",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$moves",
                        color = if (moves <= parMoves) palette.accentPrimary else Color(0xFFFF9100),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / Par $parMoves",
                        color = palette.textSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Actions: Undo, Reset, Hint
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Undo Button
                HudActionButton(
                    icon = Icons.Default.Undo,
                    label = "Undo",
                    enabled = canUndo,
                    testTag = "undo_button",
                    onClick = onUndoClicked
                )

                // Reset Button
                HudActionButton(
                    icon = Icons.Default.Refresh,
                    label = "Reset",
                    enabled = true,
                    testTag = "reset_button",
                    onClick = onResetClicked
                )

                // Hint Button
                HudActionButton(
                    icon = Icons.Default.Lightbulb,
                    label = "Hint",
                    enabled = true,
                    isAccent = true,
                    testTag = "hint_button",
                    onClick = onHintClicked
                )
            }
        }
    }
}

@Composable
fun HudActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    testTag: String,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val palette = LocalGamePalette.current
    val tint = when {
        !enabled -> palette.textSecondary.copy(alpha = 0.35f)
        isAccent -> palette.accentPrimary
        else -> palette.textPrimary
    }
    val bg = when {
        isAccent && enabled -> palette.accentPrimary.copy(alpha = 0.18f)
        else -> palette.surfaceColor
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                1.dp,
                if (isAccent && enabled) palette.accentPrimary.copy(alpha = 0.5f) else palette.boardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
