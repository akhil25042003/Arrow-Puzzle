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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.game.CellItem
import com.example.game.Direction
import com.example.game.FlyingArrowState
import com.example.game.GridPosition
import com.example.game.PuzzleLevel
import com.example.ui.theme.LocalGamePalette
import kotlin.math.roundToInt

@Composable
fun BoardView(
    level: PuzzleLevel,
    cells: Map<GridPosition, CellItem>,
    flyingArrows: List<FlyingArrowState>,
    bumpMap: Map<GridPosition, Direction>,
    hintedPosition: GridPosition?,
    onCellClicked: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current

    BoxWithConstraints(
        modifier = modifier
            .padding(12.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.boardBackground)
            .border(2.dp, palette.boardBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        val boardWidth = maxWidth
        val boardHeight = maxHeight
        val maxDim = maxOf(level.rows, level.cols)
        val cellSize = minOf(boardWidth / level.cols, boardHeight / level.rows)

        // Draw Board Grid Background Cells
        Box(
            modifier = Modifier.size(cellSize * level.cols, cellSize * level.rows)
        ) {
            // Draw slots for active positions
            for (pos in level.activePositions) {
                val left = cellSize * pos.col
                val top = cellSize * pos.row
                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(cellSize)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.cellEmptyBackground)
                )
            }

            // Draw active cells
            for ((pos, item) in cells) {
                val left = cellSize * pos.col
                val top = cellSize * pos.row
                val isHinted = hintedPosition == pos
                val bumpDir = bumpMap[pos]

                // Bump spring animation offset
                val bumpAnim = remember(bumpDir) { Animatable(0f) }
                LaunchedEffect(bumpDir) {
                    if (bumpDir != null) {
                        bumpAnim.animateTo(
                            targetValue = 12f,
                            animationSpec = spring(dampingRatio = 0.35f, stiffness = 1200f)
                        )
                        bumpAnim.animateTo(0f, animationSpec = spring())
                    }
                }

                val offsetX = if (bumpDir != null) {
                    (bumpAnim.value * bumpDir.dc).dp
                } else 0.dp

                val offsetY = if (bumpDir != null) {
                    (bumpAnim.value * bumpDir.dr).dp
                } else 0.dp

                Box(
                    modifier = Modifier
                        .offset(x = left + offsetX, y = top + offsetY)
                        .size(cellSize)
                        .padding(3.5.dp)
                        .testTag("cell_${pos.row}_${pos.col}")
                ) {
                    when (item) {
                        is CellItem.Obstacle -> {
                            ObstacleCellView(size = cellSize - 7.dp)
                        }
                        is CellItem.Arrow -> {
                            ArrowCellView(
                                arrow = item,
                                isHinted = isHinted,
                                onClick = { onCellClicked(pos) },
                                size = cellSize - 7.dp
                            )
                        }
                    }
                }
            }

            // Flying arrows animation layer
            for (flying in flyingArrows) {
                FlyingArrowView(
                    flying = flying,
                    cellSize = cellSize,
                    rows = level.rows,
                    cols = level.cols
                )
            }
        }
    }
}

@Composable
fun ArrowCellView(
    arrow: CellItem.Arrow,
    isHinted: Boolean,
    onClick: () -> Unit,
    size: Dp
) {
    val palette = LocalGamePalette.current
    val arrowColor = palette.arrowColors.getOrElse(arrow.colorIndex) { palette.accentPrimary }

    // Pulse animation when hinted
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(isHinted) {
        if (isHinted) {
            pulseAnim.animateTo(
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(1f)
        }
    }

    // Rotation animation when arrow direction changes
    val rotationAnim = remember { Animatable(arrow.direction.angleDegrees) }
    LaunchedEffect(arrow.direction) {
        rotationAnim.animateTo(
            targetValue = arrow.direction.angleDegrees,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(if (isHinted) pulseAnim.value else 1f)
            .shadow(
                elevation = if (isHinted) 10.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = arrowColor,
                spotColor = arrowColor
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        arrowColor.copy(alpha = 0.95f),
                        arrowColor.copy(alpha = 0.75f)
                    )
                )
            )
            .border(
                width = if (isHinted) 2.5.dp else 1.dp,
                color = if (isHinted) Color.White else arrowColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Arrow Shape drawn on Canvas with sharp arrowhead & shaft
        Canvas(
            modifier = Modifier
                .size(size * 0.7f)
                .rotate(rotationAnim.value)
        ) {
            val w = this.size.width
            val h = this.size.height

            // Sharp dynamic arrow path facing UP
            val arrowPath = Path().apply {
                moveTo(w * 0.5f, h * 0.10f) // Arrowhead tip
                lineTo(w * 0.82f, h * 0.45f) // Right barb
                lineTo(w * 0.62f, h * 0.45f) // Right inner neck
                lineTo(w * 0.62f, h * 0.88f) // Right shaft base
                lineTo(w * 0.38f, h * 0.88f) // Left shaft base
                lineTo(w * 0.38f, h * 0.45f) // Left inner neck
                lineTo(w * 0.18f, h * 0.45f) // Left barb
                close()
            }

            // Arrow white silhouette with slight drop shadow
            drawPath(
                path = arrowPath,
                color = Color.White
            )

            // Inner accent line
            drawLine(
                color = Color(0x33000000),
                start = Offset(w * 0.5f, h * 0.25f),
                end = Offset(w * 0.5f, h * 0.80f),
                strokeWidth = 3f
            )
        }

        // Rotator badge if arrow is a rotator
        if (arrow.isRotator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(palette.rotatorBadgeColor)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Rotator",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}

@Composable
fun ObstacleCellView(size: Dp) {
    val palette = LocalGamePalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.obstacleColor)
            .border(1.dp, palette.obstaclePatternColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            // Modern geometric cross grid for obstacle rock
            drawLine(
                color = palette.obstaclePatternColor,
                start = Offset(0f, 0f),
                end = Offset(w, h),
                strokeWidth = 2.5f
            )
            drawLine(
                color = palette.obstaclePatternColor,
                start = Offset(w, 0f),
                end = Offset(0f, h),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = palette.obstaclePatternColor,
                radius = w * 0.22f,
                center = Offset(w / 2f, h / 2f),
                style = Stroke(width = 2.5f)
            )
        }
    }
}

@Composable
fun FlyingArrowView(
    flying: FlyingArrowState,
    cellSize: Dp,
    rows: Int,
    cols: Int
) {
    val palette = LocalGamePalette.current
    val arrowColor = palette.arrowColors.getOrElse(flying.colorIndex) { palette.accentPrimary }

    // Distance in pixels to fly off screen
    val flyAnim = remember { Animatable(0f) }
    LaunchedEffect(flying.id) {
        flyAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    val flyDistancePx = (maxOf(rows, cols) * 2f + 2f)
    val curProgress = flyAnim.value

    val targetOffsetX = (cellSize * flying.startCol) + (cellSize * flyDistancePx * flying.direction.dc * curProgress)
    val targetOffsetY = (cellSize * flying.startRow) + (cellSize * flyDistancePx * flying.direction.dr * curProgress)
    val alpha = (1f - curProgress * 0.9f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .offset(x = targetOffsetX, y = targetOffsetY)
            .size(cellSize)
            .padding(3.5.dp)
            .scale(1f + curProgress * 0.25f)
            .clip(RoundedCornerShape(12.dp))
            .background(arrowColor.copy(alpha = alpha))
            .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = arrowColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(cellSize * 0.7f)
                .rotate(flying.direction.angleDegrees)
        ) {
            val w = this.size.width
            val h = this.size.height

            val arrowPath = Path().apply {
                moveTo(w * 0.5f, h * 0.10f)
                lineTo(w * 0.82f, h * 0.45f)
                lineTo(w * 0.62f, h * 0.45f)
                lineTo(w * 0.62f, h * 0.88f)
                lineTo(w * 0.38f, h * 0.88f)
                lineTo(w * 0.38f, h * 0.45f)
                lineTo(w * 0.18f, h * 0.45f)
                close()
            }
            drawPath(path = arrowPath, color = Color.White.copy(alpha = alpha))
        }
    }
}
