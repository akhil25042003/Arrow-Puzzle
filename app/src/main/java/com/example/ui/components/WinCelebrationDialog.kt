package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LocalGamePalette
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun WinCelebrationDialog(
    levelName: String,
    starsEarned: Int,
    moves: Int,
    parMoves: Int,
    timeSeconds: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalGamePalette.current

    // Dialog with confetti particle overlay
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(palette.surfaceColor)
                .border(2.dp, palette.boardBorder, RoundedCornerShape(28.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Confetti canvas in background
            ConfettiBackground()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(palette.accentPrimary.copy(alpha = 0.2f))
                        .border(2.dp, palette.accentPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LEVEL CLEARED!",
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = levelName,
                    color = palette.accentPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3 Animated Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        AnimatedStar(
                            starIndex = i,
                            isEarned = i <= starsEarned
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.boardBackground)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "Moves",
                            value = "$moves",
                            subValue = "Par $parMoves",
                            accentColor = if (moves <= parMoves) palette.accentPrimary else palette.textPrimary
                        )
                        StatItem(
                            label = "Time",
                            value = String.format("%02d:%02d", timeSeconds / 60, timeSeconds % 60),
                            subValue = "Total",
                            accentColor = palette.accentPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_level_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Next Level",
                            color = Color(0xFF0F0C20),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color(0xFF0F0C20)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onReplay,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("replay_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Replay Level",
                            color = palette.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedStar(starIndex: Int, isEarned: Boolean) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(isEarned) {
        if (isEarned) {
            delay((starIndex * 150).toLong())
            scale.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f)
            )
            scale.animateTo(1f, animationSpec = spring())
        } else {
            scale.snapTo(1f)
        }
    }

    val starColor = if (isEarned) Color(0xFFFFD700) else Color(0x35FFFFFF)

    Box(
        modifier = Modifier
            .size(if (starIndex == 2) 46.dp else 38.dp)
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            tint = starColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color
) {
    val palette = LocalGamePalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = accentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subValue,
            color = palette.textSecondary.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun ConfettiBackground() {
    val confettiColors = listOf(
        Color(0xFF00E5FF),
        Color(0xFFFF2A6D),
        Color(0xFFFFD600),
        Color(0xFFB388FF),
        Color(0xFF00E676)
    )

    val random = remember { Random(System.currentTimeMillis()) }
    val particles = remember {
        List(28) {
            ConfettiParticle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                color = confettiColors[random.nextInt(confettiColors.size)],
                size = 4f + random.nextFloat() * 6f
            )
        }
    }

    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val curY = ((p.y + anim.value) % 1f) * h
            val curX = p.x * w
            drawCircle(
                color = p.color.copy(alpha = 0.85f),
                radius = p.size,
                center = Offset(curX, curY)
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float
)
