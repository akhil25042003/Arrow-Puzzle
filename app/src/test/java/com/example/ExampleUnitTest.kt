package com.example

import com.example.game.CellItem
import com.example.game.Direction
import com.example.game.GridPosition
import com.example.game.LevelGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelGenerationGuaranteedSolvable() {
        for (lvl in 1L..10L) {
            val level = LevelGenerator.generateCampaignLevel(lvl)
            val arrowCount = level.initialCells.values.count { it is CellItem.Arrow }
            assertTrue("Level $lvl should have arrows", arrowCount > 0)
            assertEquals("Solution sequence should cover all arrows", arrowCount, level.solutionOrder.size)
        }
    }

    @Test
    fun testCustomSeedDeterminism() {
        val p1 = LevelGenerator.generateFromCustomSeed("ARROW123", 5)
        val p2 = LevelGenerator.generateFromCustomSeed("ARROW123", 5)
        assertEquals(p1.initialCells.size, p2.initialCells.size)
        assertEquals(p1.solutionOrder, p2.solutionOrder)
    }

    @Test
    fun testDirectionClockwise() {
        assertEquals(Direction.RIGHT, Direction.UP.nextClockwise())
        assertEquals(Direction.DOWN, Direction.RIGHT.nextClockwise())
        assertEquals(Direction.LEFT, Direction.DOWN.nextClockwise())
        assertEquals(Direction.UP, Direction.LEFT.nextClockwise())
    }
}
