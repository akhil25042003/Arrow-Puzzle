package com.example.game

/**
 * 4 Cardinal directions for arrow movement.
 */
enum class Direction(val dr: Int, val dc: Int, val angleDegrees: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);

    fun nextClockwise(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        RIGHT -> LEFT
        DOWN -> UP
        LEFT -> RIGHT
    }
}

/**
 * Grid coordinates on the board.
 */
data class GridPosition(val row: Int, val col: Int)

/**
 * Cell items in the puzzle.
 */
sealed interface CellItem {
    val id: Int

    data class Arrow(
        override val id: Int,
        val direction: Direction,
        val isRotator: Boolean = false,
        val colorIndex: Int = 0
    ) : CellItem

    data class Obstacle(
        override val id: Int
    ) : CellItem
}

/**
 * Animation state for flying arrow.
 */
data class FlyingArrowState(
    val id: Int,
    val startRow: Int,
    val startCol: Int,
    val direction: Direction,
    val colorIndex: Int,
    val isRotator: Boolean = false,
    val startTimeMs: Long = System.currentTimeMillis()
)

/**
 * Undo history action.
 */
sealed interface MoveAction {
    data class ArrowLaunched(
        val position: GridPosition,
        val arrow: CellItem.Arrow
    ) : MoveAction

    data class ArrowRotated(
        val position: GridPosition,
        val previousDirection: Direction,
        val newDirection: Direction
    ) : MoveAction
}

/**
 * Full level definition.
 */
data class PuzzleLevel(
    val levelNumber: Long,
    val name: String,
    val rows: Int,
    val cols: Int,
    val initialCells: Map<GridPosition, CellItem>,
    val activePositions: Set<GridPosition>,
    val solutionOrder: List<Int>, // Arrow IDs in guaranteed clearing order
    val parMoves: Int,
    val seed: Long,
    val modeName: String = "Campaign"
)
