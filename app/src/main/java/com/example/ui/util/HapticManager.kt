package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * High-precision tactile haptic feedback controller designed for puzzle piece snapping,
 * equation solving, tile sliding, and victory fanfare feedback.
 */
object HapticManager {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tactile snap haptic when two puzzle pieces connect or a piece drops into its slot.
     */
    fun performPieceSnap(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Crisp tactile double-click for piece snapping
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    val timings = longArrayOf(0, 30, 35, 55)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 35, 55), -1)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    /**
     * Triumphant tactile vibration burst when solving a math/science puzzle or completing a level.
     */
    fun performMathSolveSuccess(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 50, 60, 50, 100)
                val amplitudes = intArrayOf(0, 140, 0, 200, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 40, 50, 60, 50, 100), -1)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    /**
     * Level complete celebration haptic feedback.
     */
    fun performLevelComplete(context: Context) {
        performMathSolveSuccess(context)
    }

    /**
     * Subtle tactile click for tile slide or button tap.
     */
    fun performTileSlide(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 150))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    /**
     * Subtle buzz on incorrect answer selection.
     */
    fun performWrongAnswer(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 60, 80)
                val amplitudes = intArrayOf(0, 180, 0, 180)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 60, 60, 80), -1)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    /**
     * Grand celebration haptic when an achievement badge is unlocked.
     */
    fun performAchievementUnlock(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 40, 80, 40, 120, 60, 180)
                val amplitudes = intArrayOf(0, 160, 0, 200, 0, 230, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 60, 40, 80, 40, 120, 60, 180), -1)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
