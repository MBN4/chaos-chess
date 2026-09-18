package com.example.chaoschess.engine.models

enum class SpeedRuleType(val title: String, val description: String) {
    BACKWARD_PAWNS("Backward Pawns", "Pawns can move 1 square backward for 2 turns!"),
    KNIGHT_BISHOPS("Knight-Bishops", "Bishops gain Knight jumping moves for 2 turns!"),
    DIAGONAL_ROOKS("Diagonal Rooks", "Rooks can also move diagonally for 2 turns!"),
    FREEZE_STORM("Frost Surge", "One random non-King piece is frozen for 2 turns!"),
    TELEPORT_SWAP("Chaos Swap", "Two random non-King pieces on the board have swapped places!"),
    SUPER_KNIGHTS("Super Knights", "Knights can jump 3x1 or 2x2 for 2 turns!"),
    DANGER_ZONE("Infernal Square", "A central square turns into a dangerous zone!")
}

data class ActiveSpeedRule(
    val type: SpeedRuleType,
    val turnsRemaining: Int,
    val affectedPositions: List<Position> = emptyList()
)

enum class BoardHazardType {
    BURNING_SQUARE,
    TELEPORT_PORTAL,
    COLLAPSED_SQUARE,
    METEOR_WARNING
}

data class BoardHazard(
    val id: String,
    val type: BoardHazardType,
    val primaryPosition: Position,
    val linkedPosition: Position? = null, // For teleport portals
    val countdownTurns: Int = 1,
    val durationTurns: Int = 2,
    val warningActive: Boolean = true
)

enum class ChampionType(val pieceType: PieceType, val title: String, val abilityName: String, val abilityDescription: String) {
    PAWN(PieceType.PAWN, "Pawn Vanguard", "Tactical Retreat", "Once per game: Move backward 1 square."),
    KNIGHT(PieceType.KNIGHT, "Knight Paladin", "Double Leap", "Once per game: Perform a second Knight move in the same turn."),
    BISHOP(PieceType.BISHOP, "Bishop Mystic", "Color Shift", "Once per game: Shift diagonal color by moving 1 square orthogonally."),
    ROOK(PieceType.ROOK, "Rook Juggernaut", "Momentum Strike", "Once per game: Take a bonus straight move after capturing."),
    QUEEN(PieceType.QUEEN, "Queen Empress", "Phoenix Rebirth", "Once per game: Automatically resurrects on the home rank if captured.")
}

data class ChampionState(
    val color: PieceColor,
    val championType: ChampionType,
    val pieceId: String,
    val abilityAvailable: Boolean = true,
    val resurrectionAvailable: Boolean = true
)

data class PortalPair(
    val id: String,
    val gateA: Position,
    val gateB: Position,
    val colorIndex: Int = 0
)

data class GraveyardPiece(
    val piece: Piece,
    val turnsUntilRevive: Int = 3,
    val originalOwner: PieceColor
)

enum class TimeWarpAction(val title: String, val description: String, val cost: Int = 1) {
    REWIND("Rewind Time", "Undo the last move and revert board state.", 1),
    FREEZE("Chrono Freeze", "Freeze a target enemy non-King unit for 1 turn.", 1),
    ACCELERATE("Time Surge", "Grant chosen friendly unit an immediate bonus move.", 1),
    TIME_SHIFT("Phase Shift", "Shift a piece back to its previous position.", 1)
}

enum class ApocalypseStage(val stageNumber: Int, val title: String, val description: String) {
    STAGE_0(0, "Stable Reality", "The board is currently stable."),
    STAGE_1(1, "Unstable Squares", "Cracks form across the battlefield; hazard warnings active."),
    STAGE_2(2, "Board Collapse", "Outer perimeter squares begin collapsing into the void."),
    STAGE_3(3, "Meteor Strikes", "Cataclysmic meteors target non-King pieces on the outer perimeter."),
    STAGE_4(4, "Chaos Storm", "High-energy chaos field destabilizes all squares."),
    STAGE_5(5, "Board Shrink", "The battlefield collapses! Playable grid reduced inward!")
}

data class ApocalypseState(
    val meter: Int = 0, // 0 to 100
    val stage: ApocalypseStage = ApocalypseStage.STAGE_0,
    val minFile: Int = 0,
    val maxFile: Int = 7,
    val minRank: Int = 0,
    val maxRank: Int = 7,
    val collapsedSquares: Set<Position> = emptySet(),
    val warningSquares: Set<Position> = emptySet()
)

