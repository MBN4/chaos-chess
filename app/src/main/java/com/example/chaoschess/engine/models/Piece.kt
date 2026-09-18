package com.example.chaoschess.engine.models

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val letter: String, val baseValue: Int) {
    PAWN("P", 100),
    KNIGHT("N", 320),
    BISHOP("B", 330),
    ROOK("R", 500),
    QUEEN("Q", 900),
    KING("K", 20000),
    DRAGON("D", 650); // Knight + Bishop hybrid

    val isRoyalty: Boolean get() = this == KING
}

enum class Element(val title: String, val badge: String) {
    NONE("Normal", ""),
    FIRE("Fire", "🔥"),
    ICE("Ice", "❄️"),
    LIGHTNING("Lightning", "⚡"),
    NATURE("Nature", "🌿");

    /**
     * Cyclic advantage:
     * Fire > Nature > Lightning > Ice > Fire
     */
    fun hasAdvantageOver(other: Element): Boolean {
        return (this == FIRE && other == NATURE) ||
                (this == NATURE && other == LIGHTNING) ||
                (this == LIGHTNING && other == ICE) ||
                (this == ICE && other == FIRE)
    }
}

data class Position(val file: Int, val rank: Int) {
    init {
        require(file in 0..7 && rank in 0..7) { "Position out of board: file=$file, rank=$rank" }
    }

    val algebraic: String
        get() = "${('a' + file)}${rank + 1}"

    fun offset(df: Int, dr: Int): Position? {
        val nf = file + df
        val nr = rank + dr
        return if (nf in 0..7 && nr in 0..7) Position(nf, nr) else null
    }

    companion object {
        fun fromAlgebraic(alg: String): Position {
            require(alg.length == 2) { "Invalid algebraic position: $alg" }
            val f = alg[0] - 'a'
            val r = alg[1] - '1'
            return Position(f, r)
        }
    }
}

data class Piece(
    val id: String,
    val owner: PieceColor,
    val baseType: PieceType,
    val currentType: PieceType = baseType,
    val position: Position,
    val hasMoved: Boolean = false,
    val isRoyalKing: Boolean = (baseType == PieceType.KING),
    val isSecondaryKing: Boolean = false,
    val isChampion: Boolean = false,
    val isMystery: Boolean = false,
    val hiddenType: PieceType = currentType,
    val isDragon: Boolean = (currentType == PieceType.DRAGON),
    val isFrozen: Boolean = false,
    val freezeTurnsRemaining: Int = 0,
    val hasUsedChampionAbility: Boolean = false,
    val hasResurrected: Boolean = false,
    val isClone: Boolean = false,
    val element: Element = Element.NONE,
    val elementalBuffTurns: Int = 0
) {
    fun moveTo(newPosition: Position): Piece {
        return copy(
            position = newPosition,
            hasMoved = true
        )
    }

    fun mutateTo(newType: PieceType): Piece {
        val isKingTransformation = (newType == PieceType.KING && !isRoyalKing)
        return copy(
            currentType = newType,
            isDragon = (newType == PieceType.DRAGON),
            isSecondaryKing = isKingTransformation,
            // If it mutates, reveal mystery
            isMystery = false,
            hiddenType = newType
        )
    }

    fun transformToDragon(): Piece {
        return copy(
            currentType = PieceType.DRAGON,
            isDragon = true,
            isMystery = false,
            hiddenType = PieceType.DRAGON
        )
    }

    fun revealMystery(): Piece {
        return copy(
            isMystery = false,
            currentType = hiddenType,
            isDragon = (hiddenType == PieceType.DRAGON)
        )
    }

    /**
     * Visible type to a player: if mystery and viewing player is opponent, return baseType / visual disguise.
     * If owner or revealed, return actual currentType.
     */
    fun getVisibleType(viewerColor: PieceColor?): PieceType {
        if (!isMystery) return currentType
        return if (viewerColor == owner) hiddenType else baseType
    }
}

enum class MoveType {
    NORMAL,
    DOUBLE_PAWN_PUSH,
    EN_PASSANT,
    CASTLE_KINGSIDE,
    CASTLE_QUEENSIDE,
    PROMOTION,
    DRAGON_LEAP,
    CHAMPION_ABILITY,
    PORTAL_WARP,
    TIME_WARP_ACCEL
}

data class Move(
    val from: Position,
    val to: Position,
    val piece: Piece,
    val capturedPiece: Piece? = null,
    val moveType: MoveType = MoveType.NORMAL,
    val promotionType: PieceType? = null,
    val notation: String = "",
    val mutationResult: PieceType? = null,
    val isRareKingMutation: Boolean = false,
    val triggeredEventDescription: String? = null,
    val cloneSpawnedAt: Position? = null
)

