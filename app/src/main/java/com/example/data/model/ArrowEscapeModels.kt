package com.example.data.model

/**
 * Cardinal directions for Easybrain-style Arrow Puzzle.
 * dx, dy indicate movement step per raycast increment.
 */
enum class EasybrainArrowDir(val symbol: String, val dx: Int, val dy: Int, val rotationDegrees: Float, val label: String) {
    UP("↑", 0, -1, 0f, "North"),
    RIGHT("→", 1, 0, 90f, "East"),
    DOWN("↓", 0, 1, 180f, "South"),
    LEFT("←", -1, 0, 270f, "West");
}

/**
 * Arrow piece on the grid.
 * id: unique piece ID
 * row, col: current grid coordinate
 * direction: exit trajectory direction
 */
data class EasybrainArrowPiece(
    val id: Int,
    val row: Int,
    val col: Int,
    val direction: EasybrainArrowDir,
    val colorHex: Long = 0xFF4F46E5
)

/**
 * Level model for Arrow Escape Puzzle (Easybrain mechanics).
 */
data class ArrowEscapeLevel(
    val levelNumber: Int,
    val title: String,
    val difficulty: String,
    val rows: Int,
    val cols: Int,
    val pieces: List<EasybrainArrowPiece>,
    val maxLives: Int = 3
)

object ArrowEscapeLevelProvider {

    val levels: List<ArrowEscapeLevel> = listOf(
        // Level 1: 3x3 Starter - Gentle introduction to unblocking
        // (0, 1) -> UP exits freely
        // (1, 1) -> UP is blocked until (0, 1) leaves!
        // (1, 0) -> LEFT exits freely
        // (1, 2) -> RIGHT exits freely
        // (2, 1) -> DOWN exits freely
        ArrowEscapeLevel(
            levelNumber = 1,
            title = "First Flight",
            difficulty = "Easy",
            rows = 3,
            cols = 3,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 1, direction = EasybrainArrowDir.UP, colorHex = 0xFF2563EB),
                EasybrainArrowPiece(id = 2, row = 1, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF059669),
                EasybrainArrowPiece(id = 3, row = 1, col = 1, direction = EasybrainArrowDir.UP, colorHex = 0xFFD97706),
                EasybrainArrowPiece(id = 4, row = 1, col = 2, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF7C3AED),
                EasybrainArrowPiece(id = 5, row = 2, col = 1, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFDC2626)
            )
        ),

        // Level 2: 4x4 Cascade - Sequence clearance
        // Unblock chains: Outer arrows exit first, opening internal rows/cols
        ArrowEscapeLevel(
            levelNumber = 2,
            title = "Cascade Lock",
            difficulty = "Medium",
            rows = 4,
            cols = 4,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 0, direction = EasybrainArrowDir.UP, colorHex = 0xFF2563EB),
                EasybrainArrowPiece(id = 2, row = 0, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF059669),
                EasybrainArrowPiece(id = 3, row = 1, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF7C3AED),
                EasybrainArrowPiece(id = 4, row = 1, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFFEA580C),
                EasybrainArrowPiece(id = 5, row = 2, col = 1, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF0891B2),
                EasybrainArrowPiece(id = 6, row = 2, col = 2, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF4338CA),
                EasybrainArrowPiece(id = 7, row = 3, col = 0, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF16A34A),
                EasybrainArrowPiece(id = 8, row = 3, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFDC2626)
            )
        ),

        // Level 3: 4x4 Pinwheel Knot - Interlocking circular dependency broken by 1 free key
        ArrowEscapeLevel(
            levelNumber = 3,
            title = "Pinwheel Knot",
            difficulty = "Challenging",
            rows = 4,
            cols = 4,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 1, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF3B82F6),
                EasybrainArrowPiece(id = 2, row = 0, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF10B981),
                EasybrainArrowPiece(id = 3, row = 1, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFF59E0B), // Key exit!
                EasybrainArrowPiece(id = 4, row = 2, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF8B5CF6),
                EasybrainArrowPiece(id = 5, row = 3, col = 2, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFEC4899),
                EasybrainArrowPiece(id = 6, row = 3, col = 1, direction = EasybrainArrowDir.UP, colorHex = 0xFF06B6D4),
                EasybrainArrowPiece(id = 7, row = 2, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF14B8A6), // Free exit!
                EasybrainArrowPiece(id = 8, row = 1, col = 0, direction = EasybrainArrowDir.UP, colorHex = 0xFF6366F1),
                EasybrainArrowPiece(id = 9, row = 1, col = 1, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFEF4444),
                EasybrainArrowPiece(id = 10, row = 2, col = 2, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF84CC16)
            )
        ),

        // Level 4: 5x5 Crossfire - High density tactical clearance
        ArrowEscapeLevel(
            levelNumber = 4,
            title = "Crossfire Grid",
            difficulty = "Expert",
            rows = 5,
            cols = 5,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFF2563EB),
                EasybrainArrowPiece(id = 2, row = 1, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF059669),
                EasybrainArrowPiece(id = 3, row = 1, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFFD97706),
                EasybrainArrowPiece(id = 4, row = 1, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF7C3AED),
                EasybrainArrowPiece(id = 5, row = 2, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF0284C7),
                EasybrainArrowPiece(id = 6, row = 2, col = 1, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF16A34A),
                EasybrainArrowPiece(id = 7, row = 2, col = 3, direction = EasybrainArrowDir.UP, colorHex = 0xFFEA580C),
                EasybrainArrowPiece(id = 8, row = 2, col = 4, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF9333EA),
                EasybrainArrowPiece(id = 9, row = 3, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF0D9488),
                EasybrainArrowPiece(id = 10, row = 3, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFE11D48),
                EasybrainArrowPiece(id = 11, row = 3, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF4F46E5),
                EasybrainArrowPiece(id = 12, row = 4, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFCA8A04)
            )
        ),

        // Level 5: 5x5 Matrix Fortress - Multi-layered escape labyrinth
        ArrowEscapeLevel(
            levelNumber = 5,
            title = "Matrix Fortress",
            difficulty = "Master",
            rows = 5,
            cols = 5,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 0, direction = EasybrainArrowDir.UP, colorHex = 0xFF3B82F6),
                EasybrainArrowPiece(id = 2, row = 0, col = 4, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF10B981),
                EasybrainArrowPiece(id = 3, row = 4, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFF59E0B),
                EasybrainArrowPiece(id = 4, row = 4, col = 4, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFEC4899),
                EasybrainArrowPiece(id = 5, row = 1, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFF6366F1),
                EasybrainArrowPiece(id = 6, row = 2, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF14B8A6),
                EasybrainArrowPiece(id = 7, row = 2, col = 2, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF8B5CF6),
                EasybrainArrowPiece(id = 8, row = 2, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF06B6D4),
                EasybrainArrowPiece(id = 9, row = 3, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFE11D48),
                EasybrainArrowPiece(id = 10, row = 1, col = 1, direction = EasybrainArrowDir.UP, colorHex = 0xFF059669),
                EasybrainArrowPiece(id = 11, row = 3, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFD97706),
                EasybrainArrowPiece(id = 12, row = 1, col = 3, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF2563EB),
                EasybrainArrowPiece(id = 13, row = 3, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF7C3AED)
            )
        ),

        // Level 6: Heart Silhouette - Handcrafted pictorial shape with intricate escape dependencies
        ArrowEscapeLevel(
            levelNumber = 6,
            title = "Heart Rhythm",
            difficulty = "Master",
            rows = 6,
            cols = 6,
            maxLives = 3,
            pieces = listOf(
                // Top twin lobes of heart
                EasybrainArrowPiece(id = 1, row = 0, col = 1, direction = EasybrainArrowDir.UP, colorHex = 0xFFEF4444),
                EasybrainArrowPiece(id = 2, row = 0, col = 2, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFEC4899),
                EasybrainArrowPiece(id = 3, row = 0, col = 3, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFF43F5E),
                EasybrainArrowPiece(id = 4, row = 0, col = 4, direction = EasybrainArrowDir.UP, colorHex = 0xFFE11D48),
                // Mid body
                EasybrainArrowPiece(id = 5, row = 1, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFBE123C),
                EasybrainArrowPiece(id = 6, row = 1, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF881337),
                EasybrainArrowPiece(id = 7, row = 1, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFFB7185),
                EasybrainArrowPiece(id = 8, row = 1, col = 5, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF9F1239),
                // Lower taper
                EasybrainArrowPiece(id = 9, row = 2, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFFDA4AF),
                EasybrainArrowPiece(id = 10, row = 2, col = 5, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFF43F5E),
                EasybrainArrowPiece(id = 11, row = 3, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFFB7185),
                EasybrainArrowPiece(id = 12, row = 3, col = 4, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFE11D48),
                EasybrainArrowPiece(id = 13, row = 4, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFBE123C),
                EasybrainArrowPiece(id = 14, row = 4, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF9F1239),
                EasybrainArrowPiece(id = 15, row = 5, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF881337),
                EasybrainArrowPiece(id = 16, row = 5, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFEF4444)
            )
        ),

        // Level 7: Diamond Vortex - 6x6 high difficulty labyrinth with layered interlocking escapes
        ArrowEscapeLevel(
            levelNumber = 7,
            title = "Diamond Vortex",
            difficulty = "Grandmaster",
            rows = 6,
            cols = 6,
            maxLives = 3,
            pieces = listOf(
                EasybrainArrowPiece(id = 1, row = 0, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFF0284C7),
                EasybrainArrowPiece(id = 2, row = 0, col = 3, direction = EasybrainArrowDir.UP, colorHex = 0xFF0284C7),
                EasybrainArrowPiece(id = 3, row = 1, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF0D9488),
                EasybrainArrowPiece(id = 4, row = 1, col = 4, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF10B981),
                EasybrainArrowPiece(id = 5, row = 2, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF6366F1),
                EasybrainArrowPiece(id = 6, row = 2, col = 2, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF8B5CF6),
                EasybrainArrowPiece(id = 7, row = 2, col = 3, direction = EasybrainArrowDir.LEFT, colorHex = 0xFFA855F7),
                EasybrainArrowPiece(id = 8, row = 2, col = 5, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFD946EF),
                EasybrainArrowPiece(id = 9, row = 3, col = 0, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF3B82F6),
                EasybrainArrowPiece(id = 10, row = 3, col = 2, direction = EasybrainArrowDir.UP, colorHex = 0xFFF59E0B),
                EasybrainArrowPiece(id = 11, row = 3, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFFEA580C),
                EasybrainArrowPiece(id = 12, row = 3, col = 5, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFFEF4444),
                EasybrainArrowPiece(id = 13, row = 4, col = 1, direction = EasybrainArrowDir.LEFT, colorHex = 0xFF14B8A6),
                EasybrainArrowPiece(id = 14, row = 4, col = 4, direction = EasybrainArrowDir.RIGHT, colorHex = 0xFF84CC16),
                EasybrainArrowPiece(id = 15, row = 5, col = 2, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF059669),
                EasybrainArrowPiece(id = 16, row = 5, col = 3, direction = EasybrainArrowDir.DOWN, colorHex = 0xFF059669)
            )
        )
    )

    /**
     * Checks whether the given piece has an unobstructed path to the boundary of the board.
     * Returns true if no other piece is in its forward path.
     */
    fun isPieceUnblocked(
        piece: EasybrainArrowPiece,
        allActivePieces: List<EasybrainArrowPiece>,
        rows: Int,
        cols: Int
    ): Boolean {
        var r = piece.row + piece.direction.dy
        var c = piece.col + piece.direction.dx

        while (r in 0 until rows && c in 0 until cols) {
            if (allActivePieces.any { it.row == r && it.col == c && it.id != piece.id }) {
                return false
            }
            r += piece.direction.dy
            c += piece.direction.dx
        }
        return true
    }

    /**
     * Finds which piece is the obstacle blocking this arrow's path, if any.
     */
    fun getBlockingPiece(
        piece: EasybrainArrowPiece,
        allActivePieces: List<EasybrainArrowPiece>,
        rows: Int,
        cols: Int
    ): EasybrainArrowPiece? {
        var r = piece.row + piece.direction.dy
        var c = piece.col + piece.direction.dx

        while (r in 0 until rows && c in 0 until cols) {
            val blocker = allActivePieces.firstOrNull { it.row == r && it.col == c && it.id != piece.id }
            if (blocker != null) return blocker
            r += piece.direction.dy
            c += piece.direction.dx
        }
        return null
    }

    /**
     * Returns the first currently unblocked arrow that can safely fly off.
     */
    fun findUnblockedPiece(
        allActivePieces: List<EasybrainArrowPiece>,
        rows: Int,
        cols: Int
    ): EasybrainArrowPiece? {
        return allActivePieces.firstOrNull { isPieceUnblocked(it, allActivePieces, rows, cols) }
    }
}
