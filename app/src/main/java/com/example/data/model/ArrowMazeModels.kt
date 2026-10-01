package com.example.data.model

/**
 * 8-way cardinal and ordinal directions for Arrow Maze Logic Puzzles
 */
enum class ArrowDirection(val symbol: String, val dx: Int, val dy: Int, val label: String) {
    UP("↑", 0, -1, "North"),
    UP_RIGHT("↗", 1, -1, "Northeast"),
    RIGHT("→", 1, 0, "East"),
    DOWN_RIGHT("↘", 1, 1, "Southeast"),
    DOWN("↓", 0, 1, "South"),
    DOWN_LEFT("↙", -1, 1, "Southwest"),
    LEFT("←", -1, 0, "West"),
    UP_LEFT("↖", -1, -1, "Northwest"),
    GOAL("🎯", 0, 0, "Goal");

    companion object {
        fun fromSymbol(sym: String): ArrowDirection = entries.firstOrNull { it.symbol == sym } ?: GOAL
    }
}

/**
 * Single cell in the Arrow Maze Grid
 */
data class ArrowCell(
    val row: Int,
    val col: Int,
    val direction: ArrowDirection
)

/**
 * Complete Arrow Maze Level configuration
 */
data class ArrowMazeLevel(
    val levelNumber: Int,
    val title: String,
    val difficulty: String,
    val rows: Int,
    val cols: Int,
    val startPos: Pair<Int, Int>,
    val goalPos: Pair<Int, Int>,
    val grid: List<List<ArrowDirection>>,
    val parMoves: Int,
    val solutionPath: List<Pair<Int, Int>>
)

object ArrowMazeLevelProvider {

    val levels: List<ArrowMazeLevel> = listOf(
        // Level 1: 3x3 Starter
        // Path: (0,0) -> (0,2) -> (2,2) -> (1,1) [Goal]
        ArrowMazeLevel(
            levelNumber = 1,
            title = "Vector Steps",
            difficulty = "Starter",
            rows = 3,
            cols = 3,
            startPos = Pair(0, 0),
            goalPos = Pair(1, 1),
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.GOAL, ArrowDirection.LEFT),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.UP_LEFT)
            ),
            parMoves = 3,
            solutionPath = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 2), Pair(1, 1))
        ),

        // Level 2: 4x4 Novice
        // Path: (0,0) -> (0,3) -> (3,3) -> (3,0) -> (1,2) [Goal]
        ArrowMazeLevel(
            levelNumber = 2,
            title = "Cardinal Compass",
            difficulty = "Novice",
            rows = 4,
            cols = 4,
            startPos = Pair(0, 0),
            goalPos = Pair(1, 2),
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.GOAL, ArrowDirection.LEFT),
                listOf(ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT),
                listOf(ArrowDirection.UP_RIGHT, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT)
            ),
            parMoves = 4,
            solutionPath = listOf(Pair(0, 0), Pair(0, 3), Pair(3, 3), Pair(3, 0), Pair(1, 2))
        ),

        // Level 3: 4x4 Explorer (Diagonal Rays)
        // Path: (0,0) -> (2,2) -> (0,2) -> (3,2) -> (3,3) [Goal]
        ArrowMazeLevel(
            levelNumber = 3,
            title = "Diagonal Horizon",
            difficulty = "Explorer",
            rows = 4,
            cols = 4,
            startPos = Pair(0, 0),
            goalPos = Pair(3, 3),
            grid = listOf(
                listOf(ArrowDirection.DOWN_RIGHT, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT),
                listOf(ArrowDirection.DOWN, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.UP_LEFT),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.GOAL)
            ),
            parMoves = 4,
            solutionPath = listOf(Pair(0, 0), Pair(2, 2), Pair(0, 2), Pair(3, 2), Pair(3, 3))
        ),

        // Level 4: 5x5 Strategist
        // Path: (0,0) -> (0,4) -> (4,4) -> (4,1) -> (1,1) -> (2,2) [Goal]
        ArrowMazeLevel(
            levelNumber = 4,
            title = "Windmill Labyrinth",
            difficulty = "Strategist",
            rows = 5,
            cols = 5,
            startPos = Pair(0, 0),
            goalPos = Pair(2, 2),
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.DOWN),
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN_RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.LEFT),
                listOf(ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.GOAL, ArrowDirection.LEFT, ArrowDirection.UP),
                listOf(ArrowDirection.UP, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT)
            ),
            parMoves = 5,
            solutionPath = listOf(Pair(0, 0), Pair(0, 4), Pair(4, 4), Pair(4, 1), Pair(1, 1), Pair(2, 2))
        ),

        // Level 5: 5x5 Quantum Matrix
        // Path: (0,0) -> (3,3) -> (1,3) -> (1,0) -> (4,0) -> (4,4) [Goal]
        ArrowMazeLevel(
            levelNumber = 5,
            title = "Quantum Vector Grid",
            difficulty = "Master",
            rows = 5,
            cols = 5,
            startPos = Pair(0, 0),
            goalPos = Pair(4, 4),
            grid = listOf(
                listOf(ArrowDirection.DOWN_RIGHT, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.DOWN_RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.UP),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.LEFT),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP_RIGHT, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.GOAL)
            ),
            parMoves = 5,
            solutionPath = listOf(Pair(0, 0), Pair(3, 3), Pair(1, 3), Pair(1, 0), Pair(4, 0), Pair(4, 4))
        ),

        // Level 6: 6x6 Hyperion Matrix
        // Path: (0,0) -> (0,5) -> (5,5) -> (5,2) -> (2,2) -> (2,4) -> (4,4) -> (3,3) [Goal]
        ArrowMazeLevel(
            levelNumber = 6,
            title = "Hyperion Matrix",
            difficulty = "Grandmaster",
            rows = 6,
            cols = 6,
            startPos = Pair(0, 0),
            goalPos = Pair(3, 3),
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.DOWN),
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN_RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.DOWN, ArrowDirection.LEFT),
                listOf(ArrowDirection.UP, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.GOAL, ArrowDirection.LEFT, ArrowDirection.UP),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP_LEFT, ArrowDirection.RIGHT, ArrowDirection.UP_LEFT, ArrowDirection.UP_LEFT, ArrowDirection.LEFT),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT)
            ),
            parMoves = 7,
            solutionPath = listOf(Pair(0, 0), Pair(0, 5), Pair(5, 5), Pair(5, 2), Pair(2, 2), Pair(2, 4), Pair(4, 4), Pair(3, 3))
        ),

        // Level 7: 6x6 Apex Labyrinth
        // Path: (0,0) -> (4,4) -> (1,4) -> (1,1) -> (4,1) -> (4,5) -> (5,5) [Goal]
        ArrowMazeLevel(
            levelNumber = 7,
            title = "Grandmaster Apex",
            difficulty = "Apex",
            rows = 6,
            cols = 6,
            startPos = Pair(0, 0),
            goalPos = Pair(5, 5),
            grid = listOf(
                listOf(ArrowDirection.DOWN_RIGHT, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.DOWN, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN_RIGHT, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.LEFT),
                listOf(ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.GOAL)
            ),
            parMoves = 6,
            solutionPath = listOf(Pair(0, 0), Pair(4, 4), Pair(1, 4), Pair(1, 1), Pair(4, 1), Pair(4, 5), Pair(5, 5))
        )
    )

    /**
     * Compute all legal destinations from the given position along the arrow's ray.
     */
    fun getLegalDestinations(
        currentPos: Pair<Int, Int>,
        rows: Int,
        cols: Int,
        direction: ArrowDirection
    ): List<Pair<Int, Int>> {
        if (direction == ArrowDirection.GOAL) return emptyList()
        val legal = mutableListOf<Pair<Int, Int>>()
        var step = 1
        while (true) {
            val nextR = currentPos.first + (step * direction.dy)
            val nextC = currentPos.second + (step * direction.dx)
            if (nextR in 0 until rows && nextC in 0 until cols) {
                legal.add(Pair(nextR, nextC))
                step++
            } else {
                break
            }
        }
        return legal
    }
}
