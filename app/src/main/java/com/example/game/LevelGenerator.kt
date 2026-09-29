package com.example.game

import kotlin.random.Random

/**
 * Unlimited Procedural Level Generator with 100% Solvability Guarantee.
 * Uses forward-induction backward dependency layering so every level from 1 to infinity
 * is mathematically guaranteed to be solvable with zero dead-ends in its canonical path.
 */
object LevelGenerator {

    fun generateCampaignLevel(levelNumber: Long): PuzzleLevel {
        val seed = 512839L + levelNumber * 104729L
        val random = Random(seed)

        val (rows, cols, targetArrows, obstacleCount, rotatorCount, shapeType) = when {
            levelNumber == 1L -> LevelSpec(3, 3, 4, 0, 0, ShapeType.SQUARE)
            levelNumber == 2L -> LevelSpec(3, 3, 6, 0, 0, ShapeType.SQUARE)
            levelNumber <= 5L -> LevelSpec(4, 4, 8, 0, 0, ShapeType.SQUARE)
            levelNumber <= 10L -> LevelSpec(4, 4, 10, 1, 0, ShapeType.SQUARE)
            levelNumber <= 20L -> LevelSpec(4, 4, 12, 1, 1, ShapeType.CROSS)
            levelNumber <= 35L -> LevelSpec(5, 5, 15, 2, 2, ShapeType.SQUARE)
            levelNumber <= 50L -> LevelSpec(5, 5, 18, 2, 3, ShapeType.DIAMOND)
            levelNumber <= 75L -> LevelSpec(6, 6, 22, 3, 4, ShapeType.SQUARE)
            levelNumber <= 100L -> LevelSpec(6, 6, 26, 3, 5, ShapeType.HOLLOW)
            levelNumber <= 200L -> LevelSpec(7, 7, 32, 4, 6, ShapeType.SQUARE)
            else -> {
                // Unlimited levels (201 to infinity!)
                val size = (6 + (levelNumber % 3)).toInt().coerceIn(6, 8)
                val arrows = (24 + (levelNumber % 16)).toInt().coerceAtMost(size * size - 8)
                val obs = (2 + (levelNumber % 4)).toInt()
                val rots = (3 + (levelNumber % 5)).toInt()
                val shape = ShapeType.entries[(levelNumber % ShapeType.entries.size).toInt()]
                LevelSpec(size, size, arrows, obs, rots, shape)
            }
        }

        return generatePuzzle(
            levelNumber = levelNumber,
            name = "Level $levelNumber",
            rows = rows,
            cols = cols,
            targetArrows = targetArrows,
            obstacleCount = obstacleCount,
            rotatorCount = rotatorCount,
            shapeType = shapeType,
            seed = seed,
            modeName = "Campaign"
        )
    }

    fun generateZenLevel(size: Int, seed: Long = System.currentTimeMillis()): PuzzleLevel {
        val random = Random(seed)
        val targetArrows = ((size * size) * 0.65f).toInt().coerceAtLeast(4)
        val obstacles = (size - 3).coerceIn(0, 3)
        val rotators = (size - 2).coerceIn(0, 4)

        return generatePuzzle(
            levelNumber = (seed % 100000).coerceAtLeast(1),
            name = "Zen ${size}x${size}",
            rows = size,
            cols = size,
            targetArrows = targetArrows,
            obstacleCount = obstacles,
            rotatorCount = rotators,
            shapeType = ShapeType.SQUARE,
            seed = seed,
            modeName = "Zen"
        )
    }

    fun generateDailyLevel(dateString: String, tier: Int = 1): PuzzleLevel {
        val baseSeed = dateString.hashCode().toLong() + tier * 982451653L
        val (size, arrows, obs, rots) = when (tier) {
            1 -> Quad(4, 10, 1, 1) // Daily Bronze
            2 -> Quad(5, 17, 2, 3) // Daily Silver
            else -> Quad(6, 24, 3, 5) // Daily Gold
        }

        val tierName = when (tier) {
            1 -> "Morning Spark"
            2 -> "Afternoon Flow"
            else -> "Night Zenith"
        }

        return generatePuzzle(
            levelNumber = (tier * 1000L) + (baseSeed % 1000).coerceAtLeast(1),
            name = tierName,
            rows = size,
            cols = size,
            targetArrows = arrows,
            obstacleCount = obs,
            rotatorCount = rots,
            shapeType = ShapeType.SQUARE,
            seed = baseSeed,
            modeName = "Daily"
        )
    }

    fun generateFromCustomSeed(seedStr: String, size: Int = 5): PuzzleLevel {
        val seed = seedStr.trim().hashCode().toLong()
        val random = Random(seed)
        val targetArrows = ((size * size) * 0.65f).toInt().coerceAtLeast(5)
        val obstacles = (1 + (random.nextInt(3))).coerceAtMost(size - 2)
        val rotators = (1 + (random.nextInt(3))).coerceAtMost(size - 2)

        return generatePuzzle(
            levelNumber = (seed % 99999).coerceAtLeast(1),
            name = "Seed: $seedStr",
            rows = size,
            cols = size,
            targetArrows = targetArrows,
            obstacleCount = obstacles,
            rotatorCount = rotators,
            shapeType = ShapeType.SQUARE,
            seed = seed,
            modeName = "Custom"
        )
    }

    private data class LevelSpec(
        val rows: Int,
        val cols: Int,
        val targetArrows: Int,
        val obstacleCount: Int,
        val rotatorCount: Int,
        val shapeType: ShapeType
    )

    private data class Quad(val a: Int, val b: Int, val c: Int, val d: Int)

    enum class ShapeType {
        SQUARE,
        CROSS,
        DIAMOND,
        HOLLOW
    }

    private fun generatePuzzle(
        levelNumber: Long,
        name: String,
        rows: Int,
        cols: Int,
        targetArrows: Int,
        obstacleCount: Int,
        rotatorCount: Int,
        shapeType: ShapeType,
        seed: Long,
        modeName: String
    ): PuzzleLevel {
        val random = Random(seed)

        // 1. Compute active positions based on shape
        val activePositions = mutableSetOf<GridPosition>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val pos = GridPosition(r, c)
                val include = when (shapeType) {
                    ShapeType.SQUARE -> true
                    ShapeType.CROSS -> {
                        // Skip outer corners for 4x4 or larger
                        if (rows >= 4 && cols >= 4) {
                            !((r == 0 && c == 0) || (r == 0 && c == cols - 1) ||
                              (r == rows - 1 && c == 0) || (r == rows - 1 && c == cols - 1))
                        } else true
                    }
                    ShapeType.DIAMOND -> {
                        val centerR = (rows - 1) / 2.0
                        val centerC = (cols - 1) / 2.0
                        val dist = Math.abs(r - centerR) + Math.abs(c - centerC)
                        dist <= (rows / 2.0 + 0.6)
                    }
                    ShapeType.HOLLOW -> {
                        // Center 1-2 cells empty
                        val midR = rows / 2
                        val midC = cols / 2
                        !(r == midR && c == midC)
                    }
                }
                if (include) {
                    activePositions.add(pos)
                }
            }
        }

        // 2. Place static obstacles in inner area so board edges remain clear for exits
        val innerPositions = activePositions.filter {
            it.row > 0 && it.row < rows - 1 && it.col > 0 && it.col < cols - 1
        }.shuffled(random)

        val obstacleMap = mutableMapOf<GridPosition, CellItem.Obstacle>()
        var nextId = 1
        val actualObstacles = obstacleCount.coerceAtMost(innerPositions.size / 3)
        for (i in 0 until actualObstacles) {
            val pos = innerPositions[i]
            obstacleMap[pos] = CellItem.Obstacle(id = nextId++)
        }

        // 3. Reverse dependency construction for arrows:
        // Candidate cells are active positions not occupied by obstacles
        val availableCells = (activePositions - obstacleMap.keys).toMutableList()
        availableCells.shuffle(random)

        val placedPositions = mutableListOf<GridPosition>()
        val arrowDefinitions = mutableMapOf<GridPosition, Pair<Direction, Boolean>>() // pos -> (exitDir, isRotator)
        val solutionSequence = mutableListOf<Int>() // IDs in forward solve order
        val idToCell = mutableMapOf<Int, GridPosition>()

        var arrowsToPlace = targetArrows.coerceAtMost(availableCells.size)
        var rotatorsLeft = rotatorCount

        // Forward construction with backward dependency:
        // We pick arrows in the EXACT ORDER they will be cleared forward: A_1, A_2, ..., A_n
        // At step k: Arrow A_k at (r, c) looking along direction D only encounters:
        // - Off-grid, OR
        // - Positions from previous arrows {A_1, ..., A_{k-1}} (which are already cleared in forward play!), OR
        // - Cells not in activePositions
        // It must NOT encounter any obstacle or any cell not yet placed!
        val placedSet = mutableSetOf<GridPosition>()
        val candidates = availableCells.toMutableList()

        while (placedPositions.size < arrowsToPlace && candidates.isNotEmpty()) {
            candidates.shuffle(random)
            var placedThisRound = false

            for (pos in candidates) {
                // Find valid directions for this pos
                val validDirs = mutableListOf<Direction>()
                val directions = Direction.entries.shuffled(random)

                for (dir in directions) {
                    var curR = pos.row + dir.dr
                    var curC = pos.col + dir.dc
                    var pathClear = true

                    while (curR in 0 until rows && curC in 0 until cols) {
                        val checkPos = GridPosition(curR, curC)
                        if (obstacleMap.containsKey(checkPos)) {
                            // Blocked by obstacle
                            pathClear = false
                            break
                        }
                        if (activePositions.contains(checkPos) && !placedSet.contains(checkPos)) {
                            // Blocked by an arrow that would be cleared AFTER this one!
                            pathClear = false
                            break
                        }
                        curR += dir.dr
                        curC += dir.dc
                    }

                    if (pathClear) {
                        validDirs.add(dir)
                    }
                }

                if (validDirs.isNotEmpty()) {
                    val chosenDir = validDirs.first()
                    val isRotator = rotatorsLeft > 0 && random.nextFloat() < 0.35f
                    if (isRotator) rotatorsLeft--

                    arrowDefinitions[pos] = Pair(chosenDir, isRotator)
                    placedPositions.add(pos)
                    placedSet.add(pos)
                    candidates.remove(pos)
                    placedThisRound = true
                    break
                }
            }

            if (!placedThisRound) {
                // If no more candidates can be placed without blocking, stop gracefully
                break
            }
        }

        // 4. Build final CellItem map:
        // For rotators, rotate the starting visual direction so player must turn them to exit!
        val finalCells = mutableMapOf<GridPosition, CellItem>()
        // Put obstacles
        finalCells.putAll(obstacleMap)

        var totalRotationsNeeded = 0

        // Palette of 4 balanced modern colors for visual vibrancy
        val colorCount = 4

        for (pos in placedPositions) {
            val (exitDir, isRotator) = arrowDefinitions[pos]!!
            val arrowId = nextId++

            val initialDir = if (isRotator) {
                val rotations = 1 + random.nextInt(3) // 1, 2, or 3 rotations needed
                totalRotationsNeeded += rotations
                var d = exitDir
                repeat(rotations) {
                    d = d.nextClockwise()
                }
                d
            } else {
                exitDir
            }

            val colorIdx = (pos.row + pos.col) % colorCount

            val arrow = CellItem.Arrow(
                id = arrowId,
                direction = initialDir,
                isRotator = isRotator,
                colorIndex = colorIdx
            )
            finalCells[pos] = arrow
            solutionSequence.add(arrowId)
            idToCell[arrowId] = pos
        }

        val par = placedPositions.size + totalRotationsNeeded

        return PuzzleLevel(
            levelNumber = levelNumber,
            name = name,
            rows = rows,
            cols = cols,
            initialCells = finalCells,
            activePositions = activePositions,
            solutionOrder = solutionSequence,
            parMoves = par,
            seed = seed,
            modeName = modeName
        )
    }
}
