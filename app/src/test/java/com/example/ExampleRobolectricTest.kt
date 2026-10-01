package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.repository.PuzzleRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MindMatrix", appName)
    }

    @Test
    fun `puzzle repository provides puzzles for all categories`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = PuzzleRepository(context)

        val categories = listOf(
            PuzzleCategory.VISUAL,
            PuzzleCategory.MATH,
            PuzzleCategory.PHYSICS,
            PuzzleCategory.CHEMISTRY,
            PuzzleCategory.BIOLOGY,
            PuzzleCategory.HISTORY
        )

        for (cat in categories) {
            val puzzles = repo.getPuzzlesForCategory(cat, AgeGroup.STANDARD)
            assertTrue("Category ${cat.title} should have puzzles", puzzles.isNotEmpty())
            puzzles.forEach { p ->
                assertNotNull(p.title)
                assertNotNull(p.question)
            }
        }
    }
}
