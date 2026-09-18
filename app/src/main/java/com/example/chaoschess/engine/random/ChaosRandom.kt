package com.example.chaoschess.engine.random

import java.util.Random

class ChaosRandom(var seed: Long = System.currentTimeMillis()) {
    private var internalRandom: Random = Random(seed)

    fun reseed(newSeed: Long) {
        seed = newSeed
        internalRandom = Random(newSeed)
    }

    fun nextInt(bound: Int): Int {
        if (bound <= 0) return 0
        return internalRandom.nextInt(bound)
    }

    fun nextInt(from: Int, until: Int): Int {
        if (until <= from) return from
        return from + internalRandom.nextInt(until - from)
    }

    fun nextDouble(): Double {
        return internalRandom.nextDouble()
    }

    /**
     * Returns a double in 0.0 .. 100.0 with high precision for micro-percentages like 0.001%
     */
    fun nextPercent(): Double {
        // Use double precision for 0.001% support
        return internalRandom.nextDouble() * 100.0
    }

    fun <T> pickRandom(list: List<T>): T? {
        if (list.isEmpty()) return null
        return list[nextInt(list.size)]
    }

    fun <T> weightedChoice(items: List<Pair<T, Double>>): T {
        val totalWeight = items.sumOf { it.second }
        val r = nextDouble() * totalWeight
        var current = 0.0
        for ((item, weight) in items) {
            current += weight
            if (r <= current) return item
        }
        return items.last().first
    }
}
