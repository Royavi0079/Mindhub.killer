package com.example.data.model

/**
 * Direction a snake-arrow segment points or turns.
 */
enum class SnakeSegmentDir(val dx: Float, val dy: Float) {
    UP(0f, -1f),
    DOWN(0f, 1f),
    LEFT(-1f, 0f),
    RIGHT(1f, 0f)
}

/**
 * Waypoint in normalized grid coordinates (0f..100f).
 */
data class GridPoint(val x: Float, val y: Float)

/**
 * A bendy snake-arrow puzzle piece.
 * points: list of vertices from tail to head. The last vertex is the arrow head pointing away from the second-to-last vertex.
 * escapeDirection: primary exit direction of the head.
 * colorHex: color of the line and arrowhead.
 */
data class SnakeArrowPiece(
    val id: Int,
    val points: List<GridPoint>,
    val escapeDirection: SnakeSegmentDir,
    val colorHex: Long = 0xFF1E293B
) {
    val head: GridPoint get() = points.last()
    val tail: GridPoint get() = points.first()
}

/**
 * Handcrafted Pictorial Snake Arrow Puzzle Levels (e.g. Mushroom Silhouette, Spiral Lab, Knight, Tree).
 */
data class SnakeArrowLevel(
    val levelNumber: Int,
    val title: String,
    val difficulty: String,
    val pieces: List<SnakeArrowPiece>,
    val maxLives: Int = 3
)

object SnakeArrowLevelProvider {

    val levels: List<SnakeArrowLevel> = listOf(
        // Level 1: Beginner Labyrinth (Clean winding arrows with clear exits)
        SnakeArrowLevel(
            levelNumber = 1,
            title = "Whispering Trails",
            difficulty = "Easy",
            maxLives = 3,
            pieces = listOf(
                // 1. Top runner exit Right
                SnakeArrowPiece(
                    id = 1,
                    points = listOf(GridPoint(20f, 15f), GridPoint(75f, 15f), GridPoint(75f, 10f), GridPoint(88f, 10f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                // 2. Upper loop exit Up
                SnakeArrowPiece(
                    id = 2,
                    points = listOf(GridPoint(40f, 25f), GridPoint(40f, 18f), GridPoint(50f, 18f), GridPoint(50f, 6f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),
                // 3. Left corner exit Left
                SnakeArrowPiece(
                    id = 3,
                    points = listOf(GridPoint(25f, 35f), GridPoint(15f, 35f), GridPoint(15f, 25f), GridPoint(6f, 25f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                // 4. Center spiral hook exit Down
                SnakeArrowPiece(
                    id = 4,
                    points = listOf(GridPoint(35f, 50f), GridPoint(65f, 50f), GridPoint(65f, 65f), GridPoint(50f, 65f), GridPoint(50f, 75f), GridPoint(50f, 92f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                // 5. Right perimeter exit Right
                SnakeArrowPiece(
                    id = 5,
                    points = listOf(GridPoint(75f, 40f), GridPoint(85f, 40f), GridPoint(85f, 60f), GridPoint(94f, 60f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                // 6. Bottom curve exit Left
                SnakeArrowPiece(
                    id = 6,
                    points = listOf(GridPoint(70f, 85f), GridPoint(30f, 85f), GridPoint(30f, 92f), GridPoint(10f, 92f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                // 7. Core interior spiral - blocked until #4 moves
                SnakeArrowPiece(
                    id = 7,
                    points = listOf(GridPoint(45f, 35f), GridPoint(55f, 35f), GridPoint(55f, 45f), GridPoint(45f, 45f), GridPoint(45f, 55f), GridPoint(45f, 70f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                // 8. Core horizontal lane - blocked until #3 moves
                SnakeArrowPiece(
                    id = 8,
                    points = listOf(GridPoint(60f, 30f), GridPoint(30f, 30f), GridPoint(30f, 35f), GridPoint(12f, 35f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                )
            )
        ),

        // Level 2: Mushroom Silhouette (Matching user reference screenshots images.jpeg & images.png!)
        SnakeArrowLevel(
            levelNumber = 2,
            title = "Mushroom Silhouette",
            difficulty = "Hard",
            maxLives = 3,
            pieces = listOf(
                // Cap Crest (Top horizontal rows)
                SnakeArrowPiece(
                    id = 101,
                    points = listOf(GridPoint(35f, 12f), GridPoint(65f, 12f), GridPoint(65f, 8f), GridPoint(75f, 8f), GridPoint(92f, 8f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 102,
                    points = listOf(GridPoint(50f, 18f), GridPoint(50f, 10f), GridPoint(38f, 10f), GridPoint(38f, 4f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 103,
                    points = listOf(GridPoint(55f, 22f), GridPoint(55f, 14f), GridPoint(70f, 14f), GridPoint(70f, 5f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),

                // Cap Left Outer Flange
                SnakeArrowPiece(
                    id = 104,
                    points = listOf(GridPoint(25f, 22f), GridPoint(15f, 22f), GridPoint(15f, 32f), GridPoint(6f, 32f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 105,
                    points = listOf(GridPoint(30f, 32f), GridPoint(12f, 32f), GridPoint(12f, 42f), GridPoint(4f, 42f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 106,
                    points = listOf(GridPoint(20f, 45f), GridPoint(10f, 45f), GridPoint(10f, 50f), GridPoint(5f, 50f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),

                // Cap Right Outer Flange
                SnakeArrowPiece(
                    id = 107,
                    points = listOf(GridPoint(75f, 20f), GridPoint(85f, 20f), GridPoint(85f, 30f), GridPoint(95f, 30f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 108,
                    points = listOf(GridPoint(70f, 32f), GridPoint(88f, 32f), GridPoint(88f, 40f), GridPoint(96f, 40f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 109,
                    points = listOf(GridPoint(72f, 44f), GridPoint(88f, 44f), GridPoint(88f, 50f), GridPoint(97f, 50f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),

                // Cap Interior Maze (Complex interlocking bends)
                SnakeArrowPiece(
                    id = 110,
                    points = listOf(GridPoint(30f, 24f), GridPoint(48f, 24f), GridPoint(48f, 30f), GridPoint(35f, 30f), GridPoint(35f, 36f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 111,
                    points = listOf(GridPoint(65f, 24f), GridPoint(52f, 24f), GridPoint(52f, 34f), GridPoint(60f, 34f), GridPoint(60f, 40f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 112,
                    points = listOf(GridPoint(25f, 38f), GridPoint(45f, 38f), GridPoint(45f, 44f), GridPoint(30f, 44f), GridPoint(30f, 52f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 113,
                    points = listOf(GridPoint(68f, 38f), GridPoint(50f, 38f), GridPoint(50f, 46f), GridPoint(65f, 46f), GridPoint(65f, 54f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 114,
                    points = listOf(GridPoint(35f, 46f), GridPoint(55f, 46f), GridPoint(55f, 52f), GridPoint(75f, 52f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),

                // Mushroom Stem Waist & Bulb (Bottom stem of mushroom)
                SnakeArrowPiece(
                    id = 115,
                    points = listOf(GridPoint(32f, 56f), GridPoint(26f, 56f), GridPoint(26f, 68f), GridPoint(20f, 68f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 116,
                    points = listOf(GridPoint(68f, 56f), GridPoint(74f, 56f), GridPoint(74f, 68f), GridPoint(80f, 68f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 117,
                    points = listOf(GridPoint(35f, 64f), GridPoint(45f, 64f), GridPoint(45f, 74f), GridPoint(35f, 74f), GridPoint(35f, 85f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 118,
                    points = listOf(GridPoint(65f, 64f), GridPoint(55f, 64f), GridPoint(55f, 74f), GridPoint(65f, 74f), GridPoint(65f, 85f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 119,
                    points = listOf(GridPoint(40f, 86f), GridPoint(60f, 86f), GridPoint(60f, 92f), GridPoint(50f, 92f), GridPoint(50f, 98f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 120,
                    points = listOf(GridPoint(30f, 76f), GridPoint(25f, 76f), GridPoint(25f, 86f), GridPoint(20f, 86f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 121,
                    points = listOf(GridPoint(70f, 76f), GridPoint(75f, 76f), GridPoint(75f, 86f), GridPoint(80f, 86f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                )
            )
        ),

        // Level 3: Castle Tower Silhouette
        SnakeArrowLevel(
            levelNumber = 3,
            title = "Fortress Keep",
            difficulty = "Expert",
            maxLives = 3,
            pieces = listOf(
                // Left Battlement
                SnakeArrowPiece(
                    id = 201,
                    points = listOf(GridPoint(18f, 25f), GridPoint(18f, 12f), GridPoint(10f, 12f), GridPoint(10f, 4f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),
                // Center Battlement
                SnakeArrowPiece(
                    id = 202,
                    points = listOf(GridPoint(45f, 20f), GridPoint(55f, 20f), GridPoint(55f, 8f), GridPoint(50f, 8f), GridPoint(50f, 3f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),
                // Right Battlement
                SnakeArrowPiece(
                    id = 203,
                    points = listOf(GridPoint(82f, 25f), GridPoint(82f, 12f), GridPoint(90f, 12f), GridPoint(90f, 4f)),
                    escapeDirection = SnakeSegmentDir.UP,
                    colorHex = 0xFF0F172A
                ),
                // Portcullis Core
                SnakeArrowPiece(
                    id = 204,
                    points = listOf(GridPoint(30f, 30f), GridPoint(70f, 30f), GridPoint(70f, 45f), GridPoint(92f, 45f)),
                    escapeDirection = SnakeSegmentDir.RIGHT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 205,
                    points = listOf(GridPoint(70f, 35f), GridPoint(30f, 35f), GridPoint(30f, 50f), GridPoint(8f, 50f)),
                    escapeDirection = SnakeSegmentDir.LEFT,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 206,
                    points = listOf(GridPoint(40f, 55f), GridPoint(60f, 55f), GridPoint(60f, 70f), GridPoint(50f, 70f), GridPoint(50f, 96f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 207,
                    points = listOf(GridPoint(25f, 60f), GridPoint(35f, 60f), GridPoint(35f, 80f), GridPoint(20f, 80f), GridPoint(20f, 92f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                ),
                SnakeArrowPiece(
                    id = 208,
                    points = listOf(GridPoint(75f, 60f), GridPoint(65f, 60f), GridPoint(65f, 80f), GridPoint(80f, 80f), GridPoint(80f, 92f)),
                    escapeDirection = SnakeSegmentDir.DOWN,
                    colorHex = 0xFF0F172A
                )
            )
        )
    )

    /**
     * Determines whether a snake-arrow piece has a free trajectory out of the screen.
     * Raycasts from the arrowhead along its escape direction to ensure no other line segments intersect it.
     */
    fun isPieceUnblocked(
        piece: SnakeArrowPiece,
        allActivePieces: List<SnakeArrowPiece>
    ): Boolean {
        val head = piece.head
        val dir = piece.escapeDirection

        // Check if head ray collides with any other piece's bounding segment
        for (other in allActivePieces) {
            if (other.id == piece.id) continue

            for (i in 0 until other.points.size - 1) {
                val p1 = other.points[i]
                val p2 = other.points[i + 1]

                if (rayIntersectsSegment(head.x, head.y, dir, p1.x, p1.y, p2.x, p2.y)) {
                    return false
                }
            }
        }
        return true
    }

    private fun rayIntersectsSegment(
        hx: Float, hy: Float,
        dir: SnakeSegmentDir,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Boolean {
        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)

        return when (dir) {
            SnakeSegmentDir.RIGHT -> {
                // Ray goes from hx -> +infinity at y = hy
                val crossesY = hy >= minY - 2f && hy <= maxY + 2f
                val isAheadX = minX >= hx - 0.5f
                crossesY && isAheadX
            }
            SnakeSegmentDir.LEFT -> {
                // Ray goes from hx -> -infinity at y = hy
                val crossesY = hy >= minY - 2f && hy <= maxY + 2f
                val isAheadX = maxX <= hx + 0.5f
                crossesY && isAheadX
            }
            SnakeSegmentDir.DOWN -> {
                // Ray goes from hy -> +infinity at x = hx
                val crossesX = hx >= minX - 2f && hx <= maxX + 2f
                val isAheadY = minY >= hy - 0.5f
                crossesX && isAheadY
            }
            SnakeSegmentDir.UP -> {
                // Ray goes from hy -> -infinity at x = hx
                val crossesX = hx >= minX - 2f && hx <= maxX + 2f
                val isAheadY = maxY <= hy + 0.5f
                crossesX && isAheadY
            }
        }
    }

    fun findUnblockedPiece(
        allActivePieces: List<SnakeArrowPiece>
    ): SnakeArrowPiece? {
        return allActivePieces.firstOrNull { isPieceUnblocked(it, allActivePieces) }
    }
}
