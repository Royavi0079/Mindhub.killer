package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Energy colors and identifiers for Circuit Link (Flow Connect) Puzzle
 */
enum class CircuitColor(val id: Int, val title: String, val color: Color, val hex: Long) {
    RED(1, "Plasma Red", Color(0xFFEF4444), 0xFFEF4444),
    CYAN(2, "Volt Cyan", Color(0xFF06B6D4), 0xFF06B6D4),
    EMERALD(3, "Bio Emerald", Color(0xFF10B981), 0xFF10B981),
    YELLOW(4, "Solar Amber", Color(0xFFF59E0B), 0xFFF59E0B),
    PURPLE(5, "Quantum Purple", Color(0xFF8B5CF6), 0xFF8B5CF6),
    ORANGE(6, "Flux Orange", Color(0xFFF97316), 0xFFF97316),
    BLUE(7, "Cobalt Blue", Color(0xFF3B82F6), 0xFF3B82F6);
}

data class CircuitTerminal(
    val color: CircuitColor,
    val pos1: Pair<Int, Int>,
    val pos2: Pair<Int, Int>
)

data class CircuitLinkLevel(
    val levelNumber: Int,
    val title: String,
    val difficulty: String,
    val size: Int,
    val terminals: List<CircuitTerminal>,
    val sampleSolution: Map<CircuitColor, List<Pair<Int, Int>>> = emptyMap()
)

object CircuitLinkLevelProvider {

    val levels: List<CircuitLinkLevel> = listOf(
        // Level 1: 4x4 Beginner (3 pairs)
        // RED: (0,0) - (0,3)
        // CYAN: (1,0) - (3,0)
        // EMERALD: (1,2) - (3,2)
        CircuitLinkLevel(
            levelNumber = 1,
            title = "Spark Conduit",
            difficulty = "Starter",
            size = 4,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(0, 3)),
                CircuitTerminal(CircuitColor.CYAN, Pair(1, 0), Pair(3, 0)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(1, 2), Pair(3, 2))
            ),
            sampleSolution = mapOf(
                CircuitColor.RED to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
                CircuitColor.CYAN to listOf(Pair(1, 0), Pair(2, 0), Pair(3, 0)),
                CircuitColor.EMERALD to listOf(Pair(1, 2), Pair(2, 2), Pair(3, 2))
            )
        ),

        // Level 2: 4x4 Grid (4 pairs)
        // RED: (0,0) - (3,3)
        // CYAN: (0,1) - (1,3)
        // EMERALD: (2,0) - (3,1)
        // YELLOW: (1,1) - (2,2)
        CircuitLinkLevel(
            levelNumber = 2,
            title = "Voltage Matrix",
            difficulty = "Novice",
            size = 4,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(0, 3)),
                CircuitTerminal(CircuitColor.CYAN, Pair(1, 0), Pair(1, 3)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(2, 0), Pair(2, 3)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(3, 0), Pair(3, 3))
            ),
            sampleSolution = mapOf(
                CircuitColor.RED to listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)),
                CircuitColor.CYAN to listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2), Pair(1, 3)),
                CircuitColor.EMERALD to listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2), Pair(2, 3)),
                CircuitColor.YELLOW to listOf(Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3))
            )
        ),

        // Level 3: 5x5 Grid (4 pairs)
        CircuitLinkLevel(
            levelNumber = 3,
            title = "Current Flow",
            difficulty = "Explorer",
            size = 5,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(4, 0)),
                CircuitTerminal(CircuitColor.CYAN, Pair(0, 2), Pair(4, 2)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(0, 4), Pair(4, 4)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(1, 1), Pair(3, 3))
            )
        ),

        // Level 4: 5x5 Grid (5 pairs)
        CircuitLinkLevel(
            levelNumber = 4,
            title = "Resistor Network",
            difficulty = "Strategist",
            size = 5,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(0, 4)),
                CircuitTerminal(CircuitColor.CYAN, Pair(1, 0), Pair(3, 0)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(2, 1), Pair(4, 1)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(1, 3), Pair(3, 3)),
                CircuitTerminal(CircuitColor.PURPLE, Pair(4, 3), Pair(4, 4))
            )
        ),

        // Level 5: 6x6 Grid (5 pairs)
        CircuitLinkLevel(
            levelNumber = 5,
            title = "Capacitor Loop",
            difficulty = "Master",
            size = 6,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(5, 5)),
                CircuitTerminal(CircuitColor.CYAN, Pair(0, 5), Pair(5, 0)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(1, 2), Pair(4, 2)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(1, 3), Pair(4, 3)),
                CircuitTerminal(CircuitColor.PURPLE, Pair(2, 1), Pair(3, 4))
            )
        ),

        // Level 6: 6x6 Grid (6 pairs)
        CircuitLinkLevel(
            levelNumber = 6,
            title = "Logic Conduit",
            difficulty = "Grandmaster",
            size = 6,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(0, 5)),
                CircuitTerminal(CircuitColor.CYAN, Pair(1, 0), Pair(1, 5)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(2, 0), Pair(4, 0)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(2, 5), Pair(4, 5)),
                CircuitTerminal(CircuitColor.PURPLE, Pair(5, 0), Pair(5, 5)),
                CircuitTerminal(CircuitColor.ORANGE, Pair(2, 2), Pair(3, 3))
            )
        ),

        // Level 7: 6x6 Apex (7 pairs)
        CircuitLinkLevel(
            levelNumber = 7,
            title = "Superconductor Nexus",
            difficulty = "Apex",
            size = 6,
            terminals = listOf(
                CircuitTerminal(CircuitColor.RED, Pair(0, 0), Pair(2, 0)),
                CircuitTerminal(CircuitColor.CYAN, Pair(0, 2), Pair(0, 5)),
                CircuitTerminal(CircuitColor.EMERALD, Pair(1, 1), Pair(4, 1)),
                CircuitTerminal(CircuitColor.YELLOW, Pair(1, 4), Pair(4, 4)),
                CircuitTerminal(CircuitColor.PURPLE, Pair(3, 2), Pair(5, 2)),
                CircuitTerminal(CircuitColor.ORANGE, Pair(3, 5), Pair(5, 5)),
                CircuitTerminal(CircuitColor.BLUE, Pair(5, 0), Pair(5, 4))
            )
        )
    )
}
