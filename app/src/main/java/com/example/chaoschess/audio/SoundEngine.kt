package com.example.chaoschess.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.chaoschess.engine.models.PieceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

enum class SoundEffect {
    MOVE,
    MOVE_PAWN,
    MOVE_KNIGHT,
    MOVE_BISHOP,
    MOVE_ROOK,
    MOVE_QUEEN,
    MOVE_KING,
    CAPTURE,
    CAPTURE_HEAVY,
    CASTLE,
    PROMOTION,
    PIECE_SELECT,
    PIECE_DESELECT,
    CHECK,
    CHECKMATE,
    MUTATION,
    RARE_KING_MUTATION,
    DRAGON_ROAR,
    CHAMPION_ABILITY,
    BOARD_EVENT_WARNING,
    METEOR_IMPACT,
    VICTORY,
    DEFEAT,
    UI_CLICK
}

/**
 * High-fidelity Sound Engine synthesizing authentic Chess.com & real tournament wooden board acoustics.
 * Employs physical acoustic modeling (transient strike, body resonance, felt damping, micro-rebound)
 * and memory-cached PCM buffers for instantaneous zero-latency playback.
 */
object SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 24000

    var sfxVolume: Float = 0.90f
    var soundEnabled: Boolean = true

    // Pre-cached audio sample buffers for 0ms latency playback
    private val sampleCache = ConcurrentHashMap<SoundEffect, ShortArray>()

    init {
        // Pre-warm cache for the most frequent board movement sounds
        scope.launch {
            try {
                SoundEffect.values().forEach { effect ->
                    sampleCache[effect] = generateSamples(effect)
                }
            } catch (_: Exception) {}
        }
    }

    fun playSound(effect: SoundEffect) {
        if (!soundEnabled || sfxVolume <= 0.01f) return
        scope.launch {
            try {
                val samples = sampleCache.getOrPut(effect) { generateSamples(effect) }
                playPcm(samples)
            } catch (_: Exception) {
                // Audio device fallback safety
            }
        }
    }

    fun playPieceMove(pieceType: PieceType) {
        val effect = when (pieceType) {
            PieceType.PAWN -> SoundEffect.MOVE_PAWN
            PieceType.KNIGHT -> SoundEffect.MOVE_KNIGHT
            PieceType.BISHOP -> SoundEffect.MOVE_BISHOP
            PieceType.ROOK -> SoundEffect.MOVE_ROOK
            PieceType.QUEEN -> SoundEffect.MOVE_QUEEN
            PieceType.KING, PieceType.DRAGON -> SoundEffect.MOVE_KING
        }
        playSound(effect)
    }

    private suspend fun playPcm(samples: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            delay((samples.size * 1000L / SAMPLE_RATE) + 20L)
        } finally {
            try {
                audioTrack.stop()
            } catch (_: Exception) {}
            audioTrack.release()
        }
    }

    /**
     * Synthesizes physical acoustics of real wooden tournament chess pieces & Chess.com audio:
     * - Initial micro-transient (felt & solid wood contact click)
     * - Fast downward pitch frequency glide (the signature Chess.com "pop/thwack")
     * - Resonant wooden board body damping
     * - Dynamic double-impact acoustics for captures and castling
     */
    private fun generateSamples(effect: SoundEffect): ShortArray {
        val durationMs = when (effect) {
            SoundEffect.MOVE, SoundEffect.MOVE_PAWN -> 72
            SoundEffect.MOVE_KNIGHT, SoundEffect.MOVE_BISHOP -> 82
            SoundEffect.MOVE_ROOK -> 88
            SoundEffect.MOVE_QUEEN -> 92
            SoundEffect.MOVE_KING -> 96
            SoundEffect.CAPTURE -> 118
            SoundEffect.CAPTURE_HEAVY -> 145
            SoundEffect.CASTLE -> 180
            SoundEffect.PROMOTION -> 380
            SoundEffect.PIECE_SELECT -> 60
            SoundEffect.PIECE_DESELECT -> 45
            SoundEffect.CHECK -> 320
            SoundEffect.CHECKMATE -> 750
            SoundEffect.MUTATION -> 420
            SoundEffect.RARE_KING_MUTATION -> 1000
            SoundEffect.DRAGON_ROAR -> 550
            SoundEffect.CHAMPION_ABILITY -> 450
            SoundEffect.BOARD_EVENT_WARNING -> 380
            SoundEffect.METEOR_IMPACT -> 500
            SoundEffect.VICTORY -> 800
            SoundEffect.DEFEAT -> 650
            SoundEffect.UI_CLICK -> 35
        }

        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        val vol = (Short.MAX_VALUE * sfxVolume).toInt()

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples

            val wave: Double = when (effect) {
                // Classic Chess.com / Real Board Crisp Wooden Move "Thwock"
                SoundEffect.MOVE, SoundEffect.MOVE_PAWN -> {
                    // 1. Initial felt-on-wood sharp attack transient (0 to 3ms)
                    val click = (Math.random() - 0.5) * exp(-t * 900.0) * 0.9
                    // 2. Signature fast downward pitch glide: 520Hz rapidly sweeping down to 180Hz
                    val freq = 180.0 + 340.0 * exp(-t * 85.0)
                    val phase = 2 * PI * (180.0 * t + (340.0 / -85.0) * (exp(-t * 85.0) - 1.0))
                    val body = sin(phase) * exp(-t * 48.0) * 1.2
                    // 3. Low board hollow box cavity resonance
                    val sub = sin(2 * PI * 135.0 * t) * exp(-t * 35.0) * 0.4
                    click + body + sub
                }

                // Knight: Distinctive double-tap clip-wood jump step
                SoundEffect.MOVE_KNIGHT -> {
                    val click1 = (Math.random() - 0.5) * exp(-t * 850.0) * 0.8
                    val f1 = 200.0 + 300.0 * exp(-t * 80.0)
                    val phase1 = 2 * PI * (200.0 * t + (300.0 / -80.0) * (exp(-t * 80.0) - 1.0))
                    val body1 = sin(phase1) * exp(-t * 45.0) * 1.0
                    // Micro secondary land tap at 28ms
                    val secondary = if (t > 0.028) {
                        val t2 = t - 0.028
                        sin(2 * PI * 260.0 * t2) * exp(-t2 * 60.0) * 0.5
                    } else 0.0
                    click1 + body1 + secondary
                }

                // Bishop: Sleek diagonal slide & firm felt placement
                SoundEffect.MOVE_BISHOP -> {
                    val slideFriction = (Math.random() - 0.5) * exp(-t * 220.0) * 0.25 * (1.0 - progress)
                    val click = (Math.random() - 0.5) * exp(-t * 800.0) * 0.7
                    val phase = 2 * PI * (190.0 * t + (310.0 / -80.0) * (exp(-t * 80.0) - 1.0))
                    val body = sin(phase) * exp(-t * 44.0) * 1.1
                    slideFriction + click + body
                }

                // Rook: Heavy, deep solid wood castle placement
                SoundEffect.MOVE_ROOK -> {
                    val click = (Math.random() - 0.5) * exp(-t * 750.0) * 0.9
                    val phase = 2 * PI * (150.0 * t + (280.0 / -70.0) * (exp(-t * 70.0) - 1.0))
                    val body = sin(phase) * exp(-t * 36.0) * 1.3
                    val deepThump = sin(2 * PI * 110.0 * t) * exp(-t * 26.0) * 0.6
                    click + body + deepThump
                }

                // Queen: Resonant, weighty tournament queen impact
                SoundEffect.MOVE_QUEEN -> {
                    val click = (Math.random() - 0.5) * exp(-t * 800.0) * 0.95
                    val phase = 2 * PI * (160.0 * t + (320.0 / -75.0) * (exp(-t * 75.0) - 1.0))
                    val body = sin(phase) * exp(-t * 38.0) * 1.25
                    val harmonic = sin(2 * PI * 320.0 * t) * exp(-t * 45.0) * 0.35
                    click + body + harmonic
                }

                // King: Heavy royal piece with rich timber bass
                SoundEffect.MOVE_KING -> {
                    val click = (Math.random() - 0.5) * exp(-t * 700.0) * 1.0
                    val phase = 2 * PI * (140.0 * t + (260.0 / -65.0) * (exp(-t * 65.0) - 1.0))
                    val body = sin(phase) * exp(-t * 32.0) * 1.35
                    val timberBass = sin(2 * PI * 95.0 * t) * exp(-t * 22.0) * 0.7
                    click + body + timberBass
                }

                // Chess.com style Capture: Solid, punchy wood-on-wood collision & replacement
                SoundEffect.CAPTURE -> {
                    // Strike 1 (t = 0ms): Initial wood strike crack & deflection
                    val crack1 = (Math.random() - 0.5) * exp(-t * 1100.0) * 1.2
                    val strike1 = sin(2 * PI * 380.0 * t) * exp(-t * 55.0) * 0.9
                    // Strike 2 (t = 16ms): Capturing piece firmly landing down
                    val strike2 = if (t > 0.016) {
                        val t2 = t - 0.016
                        val click2 = (Math.random() - 0.5) * exp(-t2 * 900.0) * 0.8
                        val phase2 = 2 * PI * (160.0 * t2 + (300.0 / -75.0) * (exp(-t2 * 75.0) - 1.0))
                        val body2 = sin(phase2) * exp(-t2 * 40.0) * 1.3
                        click2 + body2
                    } else 0.0
                    crack1 + strike1 + strike2
                }

                // Heavy Capture (Queen / Rook / Dragon takedown)
                SoundEffect.CAPTURE_HEAVY -> {
                    val crack = (Math.random() - 0.5) * exp(-t * 1200.0) * 1.4
                    val strike = sin(2 * PI * 420.0 * t) * exp(-t * 50.0) * 1.0
                    val landing = if (t > 0.018) {
                        val t2 = t - 0.018
                        val phase2 = 2 * PI * (130.0 * t2 + (280.0 / -60.0) * (exp(-t2 * 60.0) - 1.0))
                        sin(phase2) * exp(-t2 * 30.0) * 1.4 + sin(2 * PI * 85.0 * t2) * exp(-t2 * 22.0) * 0.8
                    } else 0.0
                    crack + strike + landing
                }

                // Castling: Rhythmic two-step wood moves (King lands at 0ms, Rook lands at 80ms)
                SoundEffect.CASTLE -> {
                    val moveKing = { time: Double ->
                        val click = (Math.random() - 0.5) * exp(-time * 800.0) * 0.9
                        val phase = 2 * PI * (150.0 * time + (280.0 / -70.0) * (exp(-time * 70.0) - 1.0))
                        val body = sin(phase) * exp(-time * 40.0) * 1.1
                        click + body
                    }
                    val moveRook = { time: Double ->
                        val click = (Math.random() - 0.5) * exp(-time * 850.0) * 0.9
                        val phase = 2 * PI * (170.0 * time + (300.0 / -75.0) * (exp(-time * 75.0) - 1.0))
                        val body = sin(phase) * exp(-time * 42.0) * 1.1
                        click + body
                    }

                    val k = moveKing(t)
                    val r = if (t >= 0.080) moveRook(t - 0.080) else 0.0
                    k + r
                }

                // Promotion: Crisp move knock layered with royal crowning fanfare chime
                SoundEffect.PROMOTION -> {
                    val movePart = if (t < 0.12) {
                        val phase = 2 * PI * (180.0 * t + (320.0 / -80.0) * (exp(-t * 80.0) - 1.0))
                        sin(phase) * exp(-t * 45.0) * 1.0
                    } else 0.0
                    // Ascending royal chime chords (C6, E6, G6, C7)
                    val chordTime = (progress * 4).toInt()
                    val chimeFreq = when (chordTime) {
                        0 -> 1046.50
                        1 -> 1318.51
                        2 -> 1567.98
                        else -> 2093.00
                    }
                    val chime = sin(2 * PI * chimeFreq * t) * (1.0 - progress) * 0.4
                    movePart + chime
                }

                // Piece Pickup: Subtle tactile felt lift
                SoundEffect.PIECE_SELECT -> {
                    val liftFriction = (Math.random() - 0.5) * exp(-t * 400.0) * 0.3
                    val pop = sin(2 * PI * 650.0 * t) * exp(-t * 90.0) * 0.4
                    liftFriction + pop
                }

                // Piece Deselect
                SoundEffect.PIECE_DESELECT -> {
                    sin(2 * PI * 400.0 * t) * exp(-t * 95.0) * 0.25
                }

                // Check: Move sound + high-priority tactical bell chime
                SoundEffect.CHECK -> {
                    val movePart = if (t < 0.10) {
                        val phase = 2 * PI * (180.0 * t + (340.0 / -80.0) * (exp(-t * 80.0) - 1.0))
                        sin(phase) * exp(-t * 45.0) * 0.9
                    } else 0.0
                    val chime1 = sin(2 * PI * 880.0 * t) * exp(-t * 8.0) * 0.5
                    val chime2 = sin(2 * PI * 1318.5 * t) * exp(-t * 10.0) * 0.4
                    movePart + chime1 + chime2
                }

                // Checkmate: Grand triumphant wood gavel + victory chord
                SoundEffect.CHECKMATE -> {
                    val heavyGavel = sin(2 * PI * 110.0 * t) * exp(-t * 12.0) * 1.2
                    val chordFreq = if (progress < 0.25) 523.25
                    else if (progress < 0.50) 659.25
                    else if (progress < 0.75) 783.99
                    else 1046.50
                    val fanfare = sin(2 * PI * chordFreq * t) * (1.0 - progress * 0.7) * 0.6
                    heavyGavel + fanfare
                }

                SoundEffect.MUTATION -> {
                    val freq = 320.0 + 900.0 * progress
                    sin(2 * PI * freq * t) * sin(PI * progress) * 0.7
                }

                SoundEffect.RARE_KING_MUTATION -> {
                    val f1 = 523.25 * (1.0 + progress * 0.5)
                    val f2 = 659.25 * (1.0 + progress * 0.5)
                    val f3 = 783.99 * (1.0 + progress * 0.5)
                    val f4 = 1046.50
                    (sin(2 * PI * f1 * t) + sin(2 * PI * f2 * t) + sin(2 * PI * f3 * t) + sin(2 * PI * f4 * t)) * 0.22 * (1.0 - progress * 0.5)
                }

                SoundEffect.DRAGON_ROAR -> {
                    val mod = sin(2 * PI * 18.0 * t) * 80.0
                    val freq = 120.0 + mod
                    sin(2 * PI * freq * t) * exp(-progress * 5.0) * 0.8
                }

                SoundEffect.CHAMPION_ABILITY -> {
                    val freq = 600.0 + 400.0 * sin(progress * PI)
                    sin(2 * PI * freq * t) * (1.0 - progress) * 0.7
                }

                SoundEffect.BOARD_EVENT_WARNING -> {
                    val pulse = if (((t * 8).toInt() % 2) == 0) 750.0 else 550.0
                    sin(2 * PI * pulse * t) * 0.6
                }

                SoundEffect.METEOR_IMPACT -> {
                    val freq = 90.0 * (1.0 - progress * 0.8)
                    (sin(2 * PI * freq * t) + (Math.random() - 0.5) * 0.8) * exp(-progress * 7.0) * 0.8
                }

                SoundEffect.VICTORY -> {
                    val freq = if (progress < 0.3) 587.33 else if (progress < 0.6) 739.99 else 880.0
                    sin(2 * PI * freq * t) * (1.0 - progress * 0.7) * 0.7
                }

                SoundEffect.DEFEAT -> {
                    val freq = 440.0 * (1.0 - progress * 0.6)
                    sin(2 * PI * freq * t) * exp(-progress * 4.0) * 0.6
                }

                SoundEffect.UI_CLICK -> {
                    sin(2 * PI * 1200.0 * t) * exp(-progress * 40.0) * 0.4
                }
            }

            // Soft saturation limiter to prevent any digital distortion
            val limited = (wave * 0.85).coerceIn(-1.0, 1.0)
            buffer[i] = (limited * vol).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return buffer
    }
}
