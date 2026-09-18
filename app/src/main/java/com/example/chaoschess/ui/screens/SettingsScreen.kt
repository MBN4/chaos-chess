package com.example.chaoschess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.audio.SoundEffect
import com.example.chaoschess.audio.SoundEngine
import com.example.chaoschess.data.GameSettings
import com.example.chaoschess.data.SettingsRepository
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor

@Composable
fun SettingsScreen(
    currentSettings: GameSettings,
    repository: SettingsRepository,
    onSettingsChanged: (GameSettings) -> Unit,
    onBackClicked: () -> Unit
) {
    var settings by remember { mutableStateOf(currentSettings) }

    fun update(newSettings: GameSettings) {
        settings = newSettings
        SoundEngine.soundEnabled = newSettings.soundEnabled
        SoundEngine.sfxVolume = newSettings.sfxVolume
        repository.saveSettings(newSettings)
        onSettingsChanged(newSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClicked, modifier = Modifier.testTag("settings_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhitePieceColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SETTINGS & ACCESSIBILITY",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Configure Audio, Visuals & Gameplay",
                    color = MysticPurpleLight,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Audio Section
            item {
                SectionHeader("AUDIO CONTROLS")
            }

            item {
                SettingsToggleCard(
                    title = "Sound Effects (SFX)",
                    description = "Enable synthesized audio for moves, captures & chaos events.",
                    checked = settings.soundEnabled,
                    onCheckedChange = {
                        update(settings.copy(soundEnabled = it))
                        if (it) SoundEngine.playSound(SoundEffect.MOVE)
                    }
                )
            }

            if (settings.soundEnabled) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VoidCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SFX Volume", color = WhitePieceColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${(settings.sfxVolume * 100).toInt()}%", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = settings.sfxVolume,
                                onValueChange = {
                                    update(settings.copy(sfxVolume = it))
                                },
                                onValueChangeFinished = {
                                    SoundEngine.playSound(SoundEffect.UI_CLICK)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary,
                                    inactiveTrackColor = VoidCardLight
                                )
                            )
                        }
                    }
                }
            }

            // Gameplay Section
            item {
                SectionHeader("GAMEPLAY ASSISTANCE")
            }

            item {
                SettingsToggleCard(
                    title = "Show Legal Move Dots",
                    description = "Highlight legal destination squares and capture rings.",
                    checked = settings.showLegalMoves,
                    onCheckedChange = { update(settings.copy(showLegalMoves = it)) }
                )
            }

            item {
                SettingsToggleCard(
                    title = "Show Board Coordinates",
                    description = "Display rank numbers (1-8) and file letters (a-h).",
                    checked = settings.showCoordinates,
                    onCheckedChange = { update(settings.copy(showCoordinates = it)) }
                )
            }

            // Visual & Accessibility Section
            item {
                SectionHeader("VISUALS & ACCESSIBILITY")
            }

            item {
                SettingsToggleCard(
                    title = "Wood Board",
                    description = "Switch from tournament green to warm maple and walnut.",
                    checked = settings.highContrastMode,
                    onCheckedChange = { update(settings.copy(highContrastMode = it)) }
                )
            }

            item {
                SettingsToggleCard(
                    title = "Reduced Motion",
                    description = "Minimize screen shake and modal animation transitions.",
                    checked = settings.reducedMotion,
                    onCheckedChange = { update(settings.copy(reducedMotion = it)) }
                )
            }

            // Data Section
            item {
                SectionHeader("DATA MANAGEMENT")
            }

            item {
                Button(
                    onClick = {
                        repository.clearAllData()
                        update(GameSettings())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DragonCrimson.copy(alpha = 0.2f),
                        contentColor = DragonCrimson
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("clear_data_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET SETTINGS & CLEAR SAVED REPLAYS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MysticPurpleLight,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = WhitePieceColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, color = WhitePieceColor.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VoidDark,
                    checkedTrackColor = GoldPrimary,
                    uncheckedThumbColor = WhitePieceColor.copy(alpha = 0.6f),
                    uncheckedTrackColor = VoidCardLight
                )
            )
        }
    }
}
