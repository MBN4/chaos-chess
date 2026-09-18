package com.example.chaoschess.engine.models

enum class ModeCategory(val title: String) {
    ALL("All Modes"),
    CLASSIC("Classic"),
    CHAOS("Chaos"),
    SPECIAL("Special")
}

enum class GameMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val chaosRating: Int, // 1 to 5 stars
    val category: ModeCategory,
    val iconName: String,
    val difficulty: String = "Medium",
    val mechanicBadge: String = ""
) {
    CLASSIC(
        title = "Classic Chess",
        subtitle = "Standard Rules",
        description = "Standard 8x8 chess. No chaos mechanics. Pure strategy, tactics, and traditional FIDE rules.",
        chaosRating = 0,
        category = ModeCategory.CLASSIC,
        iconName = "classic",
        difficulty = "Standard",
        mechanicBadge = "Pure Chess"
    ),
    MUTATION(
        title = "Mutation Chess",
        subtitle = "Every Capture Transforms",
        description = "Whenever a piece captures, it immediately mutates into a random piece (including a 0.001% ultra-rare King!). Queens can become Pawns.",
        chaosRating = 4,
        category = ModeCategory.CHAOS,
        iconName = "mutation",
        difficulty = "High",
        mechanicBadge = "0.001% King"
    ),
    DRAGON(
        title = "Dragon Chess",
        subtitle = "Unleash The Dragon",
        description = "Each player can transform an eligible piece into a mythical Dragon (Knight leaping + Bishop continuous diagonal sliding).",
        chaosRating = 3,
        category = ModeCategory.CHAOS,
        iconName = "dragon",
        difficulty = "Medium",
        mechanicBadge = "Knight + Bishop"
    ),
    SPEED_MUTATION(
        title = "Speed Mutation",
        subtitle = "Escalating Chaos Rules",
        description = "Every 5 turns, a random temporary rule activates (backward pawns, knight-bishops, diagonal rooks, frost storms, piece swaps).",
        chaosRating = 4,
        category = ModeCategory.CHAOS,
        iconName = "speed",
        difficulty = "High",
        mechanicBadge = "5-Turn Rules"
    ),
    KING_HUNT(
        title = "King Hunt",
        subtitle = "Check Disabled • Capture King",
        description = "Check and checkmate are disabled! Kings can step into danger. The only way to win is to directly capture the enemy Royal King.",
        chaosRating = 3,
        category = ModeCategory.CHAOS,
        iconName = "hunt",
        difficulty = "Medium",
        mechanicBadge = "No Check"
    ),
    MYSTERY_PIECE(
        title = "Mystery Piece",
        subtitle = "Hidden Identities",
        description = "Certain pieces are disguised as other pieces. Their true identity is secret until they make a hidden move or capture!",
        chaosRating = 3,
        category = ModeCategory.SPECIAL,
        iconName = "mystery",
        difficulty = "Hard",
        mechanicBadge = "Disguised"
    ),
    LAST_STAND(
        title = "Last Stand",
        subtitle = "Protect Your Champion",
        description = "Select a Champion piece before match starts. If your Champion falls, you immediately lose! Champions possess unique super-abilities.",
        chaosRating = 4,
        category = ModeCategory.SPECIAL,
        iconName = "champion",
        difficulty = "Extreme",
        mechanicBadge = "VIP Piece"
    ),
    CHAOS_BOARD(
        title = "Chaos Board",
        subtitle = "Dynamic Living Board",
        description = "The board itself mutates with warning indicators: burning squares, teleport portals, meteors, collapsing squares, and freezes.",
        chaosRating = 5,
        category = ModeCategory.CHAOS,
        iconName = "chaos_board",
        difficulty = "Extreme",
        mechanicBadge = "Hazards"
    ),
    CUSTOM_CHAOS(
        title = "Custom Chaos",
        subtitle = "Sandbox Rule Builder",
        description = "Mix and match any chaos mechanics with full control over mutation rates, dragon charges, mystery counts, portals, and apocalypse.",
        chaosRating = 5,
        category = ModeCategory.CHAOS,
        iconName = "custom",
        difficulty = "Custom",
        mechanicBadge = "14 Toggles"
    ),
    UNDEAD(
        title = "Undead Chess",
        subtitle = "The Graveyard Rises",
        description = "Captured non-King pieces enter the Graveyard and resurrect onto legal open squares after 3 turns to fight again.",
        chaosRating = 4,
        category = ModeCategory.SPECIAL,
        iconName = "undead",
        difficulty = "High",
        mechanicBadge = "Resurrection"
    ),
    ELEMENTAL(
        title = "Elemental Chess",
        subtitle = "Fire • Ice • Lightning • Nature",
        description = "Pieces possess elemental affinities with cyclic battle advantages (Fire > Nature > Lightning > Ice > Fire) triggering combat buffs.",
        chaosRating = 4,
        category = ModeCategory.SPECIAL,
        iconName = "elemental",
        difficulty = "High",
        mechanicBadge = "4 Elements"
    ),
    PORTAL(
        title = "Portal Chess",
        subtitle = "Dimensional Warp Gates",
        description = "Linked dimensional portals are scattered across the board. Any piece entering a portal warps instantly through to its paired gate.",
        chaosRating = 3,
        category = ModeCategory.SPECIAL,
        iconName = "portal",
        difficulty = "Medium",
        mechanicBadge = "Warp Gates"
    ),
    CLONE_WARS(
        title = "Clone Wars",
        subtitle = "Army Duplication",
        description = "Non-King pieces that successfully capture have a 50% chance to replicate and spawn a duplicate clone in an adjacent free square.",
        chaosRating = 4,
        category = ModeCategory.CHAOS,
        iconName = "clone",
        difficulty = "Hard",
        mechanicBadge = "Duplicate"
    ),
    TIME_WARP(
        title = "Time Warp",
        subtitle = "Temporal Chrono Charges",
        description = "Wield temporal power: Rewind past moves, freeze enemy pieces in time, or accelerate your pieces for bonus momentum strikes.",
        chaosRating = 4,
        category = ModeCategory.SPECIAL,
        iconName = "time_warp",
        difficulty = "Master",
        mechanicBadge = "Chrono Powers"
    ),
    APOCALYPSE(
        title = "Apocalypse Chess",
        subtitle = "The Shrinking Cataclysm",
        description = "An Apocalypse meter escalates each turn through 5 stages of destruction, ultimately collapsing and shrinking the 8x8 board inward!",
        chaosRating = 5,
        category = ModeCategory.CHAOS,
        iconName = "apocalypse",
        difficulty = "Nightmare",
        mechanicBadge = "Board Shrink"
    )
}

data class MutationProbabilities(
    val pawnPercent: Double = 35.0,
    val knightPercent: Double = 20.0,
    val bishopPercent: Double = 15.0,
    val rookPercent: Double = 12.0,
    val queenPercent: Double = 17.999, // Normalizes total to 100% with King at 0.001%
    val kingPercent: Double = 0.001     // EXACTLY 0.001% as required by specification
) {
    fun selectPieceType(randomFloat0To100: Double): Pair<PieceType, Boolean> {
        var cum = 0.0
        cum += pawnPercent
        if (randomFloat0To100 < cum) return PieceType.PAWN to false
        cum += knightPercent
        if (randomFloat0To100 < cum) return PieceType.KNIGHT to false
        cum += bishopPercent
        if (randomFloat0To100 < cum) return PieceType.BISHOP to false
        cum += rookPercent
        if (randomFloat0To100 < cum) return PieceType.ROOK to false
        cum += queenPercent
        if (randomFloat0To100 < cum) return PieceType.QUEEN to false
        cum += kingPercent
        if (randomFloat0To100 <= cum || (randomFloat0To100 - cum) < 0.0000001) {
            return PieceType.KING to true
        }
        // Fallback safety
        return PieceType.QUEEN to false
    }
}

data class CustomChaosConfig(
    val mutationEnabled: Boolean = false,
    val mutationProbabilities: MutationProbabilities = MutationProbabilities(),
    val dragonEnabled: Boolean = false,
    val dragonChargesPerPlayer: Int = 1, // 0 = disabled, 1, 2, 99 = unlimited
    val mysteryEnabled: Boolean = false,
    val mysteryChance: Double = 0.25, // 0.10, 0.25, 0.50, 0.75
    val kingHuntEnabled: Boolean = false,
    val championEnabled: Boolean = false,
    val boardEventsEnabled: Boolean = false,
    val boardEventIntervalTurns: Int = 4, // 3, 4, 5, 10
    val speedRulesEnabled: Boolean = false,
    val speedRuleIntervalTurns: Int = 5,
    val undeadEnabled: Boolean = false,
    val undeadResurrectTurns: Int = 3,
    val elementalEnabled: Boolean = false,
    val portalsEnabled: Boolean = false,
    val portalPairCount: Int = 2,
    val cloneWarsEnabled: Boolean = false,
    val cloneChance: Double = 0.50,
    val maxClonesPerSide: Int = 4,
    val timeWarpEnabled: Boolean = false,
    val timeChargesPerPlayer: Int = 3,
    val apocalypseEnabled: Boolean = false,
    val apocalypseSpeedPerTurn: Int = 4
) {
    /**
     * Calculates overall Chaos Rating (1 to 10) and Intensity Label
     */
    fun calculateChaosRating(): Pair<Int, String> {
        var score = 0
        if (mutationEnabled) score += 2
        if (dragonEnabled) score += 1
        if (mysteryEnabled) score += 1
        if (kingHuntEnabled) score += 1
        if (championEnabled) score += 1
        if (boardEventsEnabled) score += 2
        if (speedRulesEnabled) score += 2
        if (undeadEnabled) score += 2
        if (elementalEnabled) score += 2
        if (portalsEnabled) score += 1
        if (cloneWarsEnabled) score += 2
        if (timeWarpEnabled) score += 2
        if (apocalypseEnabled) score += 3

        val rating = score.coerceIn(0, 10)
        val intensity = when {
            rating == 0 -> "Pure Standard"
            rating <= 2 -> "Mild Chaos"
            rating <= 4 -> "Moderate Strategy"
            rating <= 7 -> "High Intensity"
            rating <= 9 -> "Extreme Pandemonium"
            else -> "UNSTABLE APOCALYPSE"
        }
        return rating to intensity
    }

    companion object {
        fun forMode(mode: GameMode): CustomChaosConfig {
            return when (mode) {
                GameMode.CLASSIC -> CustomChaosConfig()
                GameMode.MUTATION -> CustomChaosConfig(mutationEnabled = true)
                GameMode.DRAGON -> CustomChaosConfig(dragonEnabled = true, dragonChargesPerPlayer = 1)
                GameMode.SPEED_MUTATION -> CustomChaosConfig(speedRulesEnabled = true, speedRuleIntervalTurns = 5)
                GameMode.KING_HUNT -> CustomChaosConfig(kingHuntEnabled = true)
                GameMode.MYSTERY_PIECE -> CustomChaosConfig(mysteryEnabled = true, mysteryChance = 0.25)
                GameMode.LAST_STAND -> CustomChaosConfig(championEnabled = true)
                GameMode.CHAOS_BOARD -> CustomChaosConfig(boardEventsEnabled = true, boardEventIntervalTurns = 4)
                GameMode.UNDEAD -> CustomChaosConfig(undeadEnabled = true, undeadResurrectTurns = 3)
                GameMode.ELEMENTAL -> CustomChaosConfig(elementalEnabled = true)
                GameMode.PORTAL -> CustomChaosConfig(portalsEnabled = true, portalPairCount = 2)
                GameMode.CLONE_WARS -> CustomChaosConfig(cloneWarsEnabled = true, cloneChance = 0.50, maxClonesPerSide = 4)
                GameMode.TIME_WARP -> CustomChaosConfig(timeWarpEnabled = true, timeChargesPerPlayer = 3)
                GameMode.APOCALYPSE -> CustomChaosConfig(apocalypseEnabled = true, apocalypseSpeedPerTurn = 4)
                GameMode.CUSTOM_CHAOS -> CustomChaosConfig(
                    mutationEnabled = true,
                    dragonEnabled = true,
                    portalsEnabled = true,
                    boardEventsEnabled = true
                )
            }
        }
    }
}

