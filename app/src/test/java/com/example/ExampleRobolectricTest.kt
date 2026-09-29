package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.CellItem
import com.example.game.Direction
import com.example.game.LevelGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Arrow Puzzle", appName)
    }

    @Test
    fun `procedural campaign levels generate successfully`() {
        // Test level 1, level 50, level 999
        val l1 = LevelGenerator.generateCampaignLevel(1)
        assertEquals(3, l1.rows)
        assertEquals(3, l1.cols)
        assertTrue(l1.initialCells.isNotEmpty())

        val l50 = LevelGenerator.generateCampaignLevel(50)
        assertTrue(l50.rows >= 5)
        assertTrue(l50.initialCells.values.any { it is CellItem.Arrow })

        val l999 = LevelGenerator.generateCampaignLevel(999)
        assertTrue(l999.rows in 6..8)
        assertTrue(l999.solutionOrder.isNotEmpty())
    }
}
