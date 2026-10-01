package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.UploadedPuzzleEntity
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PuzzleDatabaseManager(context: Context) {

    private val db = androidx.room.Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "mindmatrix_db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.puzzleDao()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Download and import puzzles from a remote URL directly into the Room database.
     * Generates new levels sequentially starting after Level 15 (or after current max uploaded level).
     */
    suspend fun importPuzzlesFromUrl(url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP Error: ${response.code}"))
                }
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                return@withContext importPuzzlesFromJson(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse and import JSON string of puzzles into database.
     * Generates new levels starting from 16 upwards.
     */
    suspend fun importPuzzlesFromJson(jsonStr: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val trimmed = jsonStr.trim()
            val jsonArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("puzzles")) {
                    obj.getJSONArray("puzzles")
                } else {
                    val arr = JSONArray()
                    arr.put(obj)
                    arr
                }
            } else {
                return@withContext Result.failure(Exception("Invalid JSON format"))
            }

            var importedCount = 0
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val catStr = item.optString("category", "MATH").uppercase()
                val category = try {
                    PuzzleCategory.values().firstOrNull { it.id.equals(catStr, ignoreCase = true) || it.name.equals(catStr, ignoreCase = true) } ?: PuzzleCategory.MATH
                } catch (e: Exception) {
                    PuzzleCategory.MATH
                }

                // Determine next sequential level for this category
                val currentMax = dao.getMaxUploadedLevel(category.id) ?: 15
                val newLevel = (currentMax.coerceAtLeast(15)) + 1 + i

                val title = item.optString("title", "Level $newLevel: Custom Cloud Challenge")
                val question = item.optString("question", "Solve the puzzle to advance.")
                val correctAnswer = item.optString("correctAnswer", "Option A")
                val explanation = item.optString("explanation", "Great job solving this dynamic level!")
                val hint = item.optString("hint", "Analyze the pattern carefully.")
                val difficultyStr = item.optString("difficulty", "NORMAL").uppercase()
                val typeStr = item.optString("type", "MULTIPLE_CHOICE").uppercase()
                val imageUrl = if (item.has("imageUrl")) item.optString("imageUrl") else null

                // Extract options
                val optionsList = mutableListOf<String>()
                if (item.has("options")) {
                    val optArr = item.optJSONArray("options")
                    if (optArr != null) {
                        for (o in 0 until optArr.length()) {
                            optionsList.add(optArr.getString(o))
                        }
                    }
                }
                if (optionsList.isEmpty()) {
                    optionsList.addAll(listOf(correctAnswer, "Option B", "Option C", "Option D"))
                }

                val optionsJson = JSONArray(optionsList).toString()

                val entity = UploadedPuzzleEntity(
                    id = "upload_${category.id}_lvl_${newLevel}_${System.currentTimeMillis()}_$i",
                    categoryId = category.id,
                    level = newLevel,
                    title = title,
                    question = question,
                    optionsJson = optionsJson,
                    correctAnswer = correctAnswer,
                    explanation = explanation,
                    hint = hint,
                    type = typeStr,
                    difficulty = difficultyStr,
                    imageUrl = imageUrl
                )

                dao.insertUploadedPuzzle(entity)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all uploaded puzzles for a category and convert to PuzzleItem models
     */
    suspend fun getUploadedPuzzleItems(category: PuzzleCategory): List<PuzzleItem> = withContext(Dispatchers.IO) {
        val entities = dao.getUploadedPuzzles(category.id)
        entities.map { entity ->
            val options = try {
                val arr = JSONArray(entity.optionsJson)
                val list = mutableListOf<String>()
                for (j in 0 until arr.length()) {
                    list.add(arr.getString(j))
                }
                list
            } catch (e: Exception) {
                listOf(entity.correctAnswer)
            }

            val diff = try { Difficulty.valueOf(entity.difficulty) } catch (e: Exception) { Difficulty.NORMAL }
            val pType = try { PuzzleType.valueOf(entity.type) } catch (e: Exception) { PuzzleType.MULTIPLE_CHOICE }

            PuzzleItem(
                id = entity.id,
                category = category,
                type = pType,
                title = entity.title,
                question = entity.question,
                options = options,
                correctAnswer = entity.correctAnswer,
                explanation = entity.explanation,
                hint = entity.hint,
                funFact = "Custom puzzle level stored securely in local database.",
                difficulty = diff,
                level = entity.level,
                targetAge = AgeGroup.STANDARD,
                imageRes = R.drawable.img_quantum_core
            )
        }
    }
}
