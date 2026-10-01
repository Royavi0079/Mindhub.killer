package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiPuzzleService {
    private const val TAG = "GeminiPuzzleService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateAIPuzzle(
        category: PuzzleCategory,
        ageGroup: AgeGroup,
        difficulty: Difficulty
    ): PuzzleItem? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to dynamic offline bank.")
            return@withContext null
        }

        try {
            val tierInstruction = when (difficulty) {
                Difficulty.NORMAL -> "Tier I (Normal): Accessible, elegant foundational concepts and logical problem solving."
                Difficulty.HARD -> "Tier II (Hard): Advanced multi-step deductions, quantitative calculations, and deeper scientific/historical nuances."
                Difficulty.EXTREME -> "Tier III (Extreme Grandmaster): High-level, complex, brain-twisting puzzle requiring profound understanding, non-trivial steps, or counter-intuitive principles."
            }

            val prompt = """
                Generate 1 unique, fun, and high-quality educational puzzle for a puzzle game.
                Category: ${category.title}
                Target Player: ${ageGroup.title} (${ageGroup.ageRange})
                Difficulty Tier: ${difficulty.label} (${difficulty.tierCode})
                Tier Guidance: $tierInstruction
                
                Format the response strictly as valid JSON with no markdown wrapping or code blocks:
                {
                  "title": "Short creative title (max 5 words)",
                  "question": "Clear and engaging high-quality puzzle prompt or challenge",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctAnswer": "Exact text of the correct option",
                  "hint": "Gentle helpful clue without giving away the exact answer",
                  "explanation": "Clear 2-sentence explanation of why this answer is correct and the underlying concepts",
                  "funFact": "Fascinating bite-sized trivia fact related to this puzzle"
                }
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
            val contentObj = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            val rawText = parts.optJSONObject(0)?.optString("text") ?: return@withContext null

            val cleanedJsonText = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val puzzleJson = JSONObject(cleanedJsonText)
            val title = puzzleJson.getString("title")
            val question = puzzleJson.getString("question")
            val optionsJson = puzzleJson.getJSONArray("options")
            val options = mutableListOf<String>()
            for (i in 0 until optionsJson.length()) {
                options.add(optionsJson.getString(i))
            }
            val correctAnswer = puzzleJson.getString("correctAnswer")
            val hint = puzzleJson.getString("hint")
            val explanation = puzzleJson.getString("explanation")
            val funFact = puzzleJson.optString("funFact", "")

            return@withContext PuzzleItem(
                id = "ai_${System.currentTimeMillis()}",
                category = category,
                type = PuzzleType.MULTIPLE_CHOICE,
                title = title,
                question = question,
                options = options,
                correctAnswer = correctAnswer,
                hint = hint,
                explanation = explanation,
                funFact = funFact,
                difficulty = difficulty,
                targetAge = ageGroup,
                imageRes = difficulty.tierImageRes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI puzzle: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun askGeminiForDeepHint(
        puzzleQuestion: String,
        currentOptions: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Think about the foundational concepts and eliminate the options that seem mathematically or scientifically improbable!"
        }

        try {
            val prompt = "Provide a warm, encouraging, 1-2 sentence hint for this question without giving away the direct answer: '$puzzleQuestion'. Options are: ${currentOptions.joinToString(", ")}"
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
            }
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: return@withContext "Look closely at the pattern and relationships in the problem!"
            val root = JSONObject(resStr)
            val candidates = root.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
            return@withContext text?.trim() ?: "Check the key formulas and properties involved."
        } catch (e: Exception) {
            return@withContext "Think step-by-step to rule out less likely choices."
        }
    }
}
