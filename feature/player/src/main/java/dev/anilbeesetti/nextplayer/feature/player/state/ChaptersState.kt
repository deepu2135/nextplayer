package dev.anilbeesetti.nextplayer.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.extractor.metadata.id3.ChapterFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import dev.anilbeesetti.nextplayer.feature.player.model.Chapter

@Composable
fun rememberChaptersState(player: Player): ChaptersState {
    val chaptersState = remember { ChaptersState(player) }
    LaunchedEffect(player) { chaptersState.observe() }
    return chaptersState
}

@Stable
class ChaptersState(private val player: Player) {
    var chapters: List<Chapter> by mutableStateOf(emptyList())
        private set

    suspend fun observe() {
        extractChapters()
        player.listen { events ->
            if (events.containsAny(Player.EVENT_TRACKS_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                extractChapters()
            }
        }
    }

    private fun extractChapters() {
        val extractedChapters = mutableListOf<Chapter>()
        
        val tracks = player.currentTracks
        for (group in tracks.groups) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val metadata = format.metadata ?: continue
                for (j in 0 until metadata.length()) {
                    val entry = metadata.get(j)
                    if (entry is ChapterFrame) {
                        var title = "Chapter ${extractedChapters.size + 1}"
                        for (k in 0 until entry.subFrameCount) {
                            val subFrame = entry.getSubFrame(k)
                            if (subFrame is TextInformationFrame) {
                                val value = subFrame.values.firstOrNull()
                                if (value != null) {
                                    title = value
                                    break
                                }
                            }
                        }
                        extractedChapters.add(
                            Chapter(
                                title = title,
                                startTimeMs = entry.startTimeMs.toLong(),
                                endTimeMs = entry.endTimeMs.toLong()
                            )
                        )
                    } else if (entry is androidx.media3.common.Chapter) {
                        extractedChapters.add(
                            Chapter(
                                title = entry.title?.toString() ?: "Chapter ${extractedChapters.size + 1}",
                                startTimeMs = entry.startTimeMs,
                                endTimeMs = entry.endTimeMs
                            )
                        )
                    }
                }
            }
        }
        
        chapters = extractedChapters.sortedBy { it.startTimeMs }
    }
}
