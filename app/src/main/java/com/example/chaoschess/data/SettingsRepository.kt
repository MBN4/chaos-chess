package com.example.chaoschess.data

import android.content.Context
import android.content.SharedPreferences
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.PieceColor
import org.json.JSONArray
import org.json.JSONObject

data class GameSettings(
    val soundEnabled: Boolean = true,
    val sfxVolume: Float = 0.8f,
    val musicVolume: Float = 0.6f,
    val showLegalMoves: Boolean = true,
    val showCoordinates: Boolean = true,
    val confirmMoves: Boolean = false,
    val animationSpeedMs: Int = 200,
    val screenShakeEnabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val highContrastMode: Boolean = false,
    val reducedMotion: Boolean = false
)

data class ReplayData(
    val id: String,
    val title: String,
    val mode: GameMode,
    val dateEpoch: Long,
    val winner: PieceColor?,
    val seed: Long,
    val moveNotations: List<String>,
    val totalMoves: Int
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("chaos_chess_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): GameSettings {
        return GameSettings(
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            sfxVolume = prefs.getFloat("sfx_volume", 0.8f),
            musicVolume = prefs.getFloat("music_volume", 0.6f),
            showLegalMoves = prefs.getBoolean("show_legal_moves", true),
            showCoordinates = prefs.getBoolean("show_coords", true),
            confirmMoves = prefs.getBoolean("confirm_moves", false),
            animationSpeedMs = prefs.getInt("anim_speed", 200),
            screenShakeEnabled = prefs.getBoolean("screen_shake", true),
            particlesEnabled = prefs.getBoolean("particles", true),
            highContrastMode = prefs.getBoolean("high_contrast", false),
            reducedMotion = prefs.getBoolean("reduced_motion", false)
        )
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit().apply {
            putBoolean("sound_enabled", settings.soundEnabled)
            putFloat("sfx_volume", settings.sfxVolume)
            putFloat("music_volume", settings.musicVolume)
            putBoolean("show_legal_moves", settings.showLegalMoves)
            putBoolean("show_coords", settings.showCoordinates)
            putBoolean("confirm_moves", settings.confirmMoves)
            putInt("anim_speed", settings.animationSpeedMs)
            putBoolean("screen_shake", settings.screenShakeEnabled)
            putBoolean("particles", settings.particlesEnabled)
            putBoolean("high_contrast", settings.highContrastMode)
            putBoolean("reduced_motion", settings.reducedMotion)
            apply()
        }
    }

    fun saveReplay(replay: ReplayData) {
        val replays = loadReplays().toMutableList()
        replays.add(0, replay)
        val jsonArray = JSONArray()
        for (r in replays.take(30)) { // Keep last 30 replays
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("mode", r.mode.name)
                put("dateEpoch", r.dateEpoch)
                put("winner", r.winner?.name ?: "DRAW")
                put("seed", r.seed)
                put("moves", JSONArray(r.moveNotations))
                put("totalMoves", r.totalMoves)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_replays", jsonArray.toString()).apply()
    }

    fun loadReplays(): List<ReplayData> {
        val str = prefs.getString("saved_replays", null) ?: return emptyList()
        val list = mutableListOf<ReplayData>()
        try {
            val jsonArray = JSONArray(str)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val movesArray = obj.getJSONArray("moves")
                val movesList = (0 until movesArray.length()).map { movesArray.getString(it) }
                val winnerStr = obj.getString("winner")
                val winner = if (winnerStr == "DRAW") null else PieceColor.valueOf(winnerStr)
                list.add(
                    ReplayData(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        mode = GameMode.valueOf(obj.getString("mode")),
                        dateEpoch = obj.getLong("dateEpoch"),
                        winner = winner,
                        seed = obj.getLong("seed"),
                        moveNotations = movesList,
                        totalMoves = obj.getInt("totalMoves")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
