package dev.anilbeesetti.nextplayer.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.extractor.metadata.id3.ChapterFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import dev.anilbeesetti.nextplayer.feature.player.model.Chapter
import kotlinx.coroutines.awaitCancellation

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
        val listener = object : Player.Listener {
            override fun onMetadata(metadata: Metadata) {
                val newChapters = parseMetadata(metadata)
                if (newChapters.isNotEmpty()) {
                    val merged = (chapters + newChapters).distinctBy { it.startTimeMs }.sortedBy { it.startTimeMs }
                    chapters = merged
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                extractChapters()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                chapters = emptyList()
                extractChapters()
            }
        }
        player.addListener(listener)
        try {
            awaitCancellation()
        } finally {
            player.removeListener(listener)
        }
    }

    private fun extractChapters() {
        val extractedChapters = mutableListOf<Chapter>()
        
        val tracks = player.currentTracks
        for (group in tracks.groups) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val metadata = format.metadata ?: continue
                extractedChapters.addAll(parseMetadata(metadata))
            }
        }
        
        chapters = extractedChapters.distinctBy { it.startTimeMs }.sortedBy { it.startTimeMs }
    }

    private fun parseMetadata(metadata: Metadata): List<Chapter> {
        val list = mutableListOf<Chapter>()
        for (i in 0 until metadata.length()) {
            val entry = metadata.get(i)
            if (entry is ChapterFrame) {
                var title = "Chapter ${list.size + 1}"
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
                list.add(
                    Chapter(
                        title = title,
                        startTimeMs = entry.startTimeMs.toLong(),
                        endTimeMs = entry.endTimeMs.toLong()
                    )
                )
            } else if (entry.javaClass.simpleName.contains("Chapter", ignoreCase = true)) {
                val title = getProperty(entry, "title")?.toString() ?: "Chapter ${list.size + 1}"
                val startTime = (getProperty(entry, "startTimeMs") as? Number)?.toLong()
                    ?: (getProperty(entry, "startTime") as? Number)?.toLong()
                    ?: 0L
                val endTime = (getProperty(entry, "endTimeMs") as? Number)?.toLong()
                    ?: (getProperty(entry, "endTime") as? Number)?.toLong()
                    ?: 0L

                list.add(
                    Chapter(
                        title = title,
                        startTimeMs = startTime,
                        endTimeMs = if (endTime > startTime) endTime else (startTime + 1000)
                    )
                )
            }
        }
        return list
    }

    private fun getProperty(obj: Any, name: String): Any? {
        val clazz = obj.javaClass
        
        // 1. Try getter method (e.g. getStartTimeMs(), getTitle())
        val getterName = "get" + name.replaceFirstChar { it.uppercase() }
        val method = runCatching { clazz.getMethod(getterName) }.getOrNull()
            ?: runCatching { clazz.getDeclaredMethod(getterName).apply { isAccessible = true } }.getOrNull()
        if (method != null) {
            val res = runCatching { method.invoke(obj) }.getOrNull()
            if (res != null) return res
        }
        
        // 2. Try exact method name (e.g. startTimeMs(), title())
        val exactMethod = runCatching { clazz.getMethod(name) }.getOrNull()
            ?: runCatching { clazz.getDeclaredMethod(name).apply { isAccessible = true } }.getOrNull()
        if (exactMethod != null) {
            val res = runCatching { exactMethod.invoke(obj) }.getOrNull()
            if (res != null) return res
        }
        
        // 3. Try field (e.g. startTimeMs, title)
        val field = runCatching { clazz.getField(name) }.getOrNull()
            ?: runCatching { clazz.getDeclaredField(name).apply { isAccessible = true } }.getOrNull()
        if (field != null) {
            val res = runCatching { field.get(obj) }.getOrNull()
            if (res != null) return res
        }
        
        return null
    }
}
