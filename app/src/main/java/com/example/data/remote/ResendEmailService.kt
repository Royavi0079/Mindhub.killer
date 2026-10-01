package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ResendEmailService {
    private const val TAG = "ResendEmailService"
    private const val SEND_URL = "https://api.resend.com/emails"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun sendProgressReport(
        toEmail: String,
        username: String,
        coins: Int,
        completedPuzzles: Int,
        achievementsCount: Int,
        highestScore: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.RESEND_API_KEY
        if (apiKey.isBlank() || apiKey == "PLACEHOLDER_RESEND_API_KEY") {
            return@withContext Result.failure(Exception("Resend API Key is not configured. Please set it in the Secrets panel in AI Studio."))
        }

        try {
            val fromAddress = "MindMatrix <onboarding@resend.dev>"
            val subject = "MindMatrix Player Progress Report: $username"
            
            // Build custom beautiful HTML content
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>MindMatrix Progress Report</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; color: #1e293b; margin: 0; padding: 20px; }
                        .card { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden; }
                        .header { background-color: #4f46e5; color: #ffffff; padding: 32px 24px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.05em; }
                        .header p { margin: 8px 0 0 0; font-size: 16px; opacity: 0.9; }
                        .content { padding: 32px 24px; }
                        .welcome { font-size: 18px; font-weight: 600; color: #0f172a; margin-bottom: 16px; }
                        .stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin: 24px 0; }
                        .stat-box { background-color: #f1f5f9; padding: 16px; border-radius: 12px; text-align: center; border: 1px solid #e2e8f0; }
                        .stat-value { font-size: 24px; font-weight: 700; color: #4f46e5; }
                        .stat-label { font-size: 12px; color: #64748b; text-transform: uppercase; margin-top: 4px; font-weight: 600; }
                        .footer { background-color: #f8fafc; padding: 24px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #e2e8f0; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h1>MindMatrix</h1>
                            <p>Universal Educational Puzzle Adventure</p>
                        </div>
                        <div class="content">
                            <div class="welcome">Congratulations, $username!</div>
                            <p>Here is your official player progress report and stats summary from your MindMatrix journey. Keep playing to sharpen your mind across mathematics, science, history, visual puzzles, and AI-powered challenges!</p>
                            
                            <div class="stats-grid">
                                <div class="stat-box">
                                    <div class="stat-value">🪙 $coins</div>
                                    <div class="stat-label">Total Coins</div>
                                </div>
                                <div class="stat-box">
                                    <div class="stat-value">🧩 $completedPuzzles</div>
                                    <div class="stat-label">Puzzles Completed</div>
                                </div>
                                <div class="stat-box">
                                    <div class="stat-value">🏆 $achievementsCount</div>
                                    <div class="stat-label">Achievements Unlocked</div>
                                </div>
                                <div class="stat-box">
                                    <div class="stat-value">📈 $highestScore</div>
                                    <div class="stat-label">Highest Level Score</div>
                                </div>
                            </div>
                            
                            <p style="text-align: center; margin-top: 32px;">
                                <a href="https://ai.studio/build" style="background-color: #4f46e5; color: #ffffff; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; display: inline-block;">Continue Your Puzzle Quest</a>
                            </p>
                        </div>
                        <div class="footer">
                            <p>Sent via Resend Email Services &bull; Powered by Google AI Studio</p>
                            <p style="font-size: 11px; margin-top: 8px; color: #94a3b8;">Note: If you are using a free Resend developer account, emails can only be sent to your registered and verified Resend domains/emails.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("from", fromAddress)
                put("to", JSONArray().apply { put(toEmail) })
                put("subject", subject)
                put("html", htmlContent)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(SEND_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    Log.i(TAG, "Email sent successfully: $respStr")
                    Result.success(respStr)
                } else {
                    val errStr = response.body?.string() ?: ""
                    Log.e(TAG, "Failed to send email: Code ${response.code}, Response: $errStr")
                    Result.failure(Exception("Error sending email (HTTP ${response.code}): $errStr"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending email", e)
            Result.failure(e)
        }
    }
}
