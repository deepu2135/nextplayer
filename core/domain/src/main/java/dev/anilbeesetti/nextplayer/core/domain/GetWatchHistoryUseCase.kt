package dev.anilbeesetti.nextplayer.core.domain

import android.net.Uri
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.data.repository.MediaRepository
import dev.anilbeesetti.nextplayer.core.database.dao.MediumStateDao
import dev.anilbeesetti.nextplayer.core.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Date
import javax.inject.Inject

class GetWatchHistoryUseCase @Inject constructor(
    private val mediumStateDao: MediumStateDao,
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<List<Video>> {
        return combine(
            mediumStateDao.getAll(),
            mediaRepository.observeVideos(null)
        ) { states, localVideos ->
            val localVideosMap = localVideos.associateBy { it.uriString }
            states.filter { it.lastPlayedTime != null && !Utils.isLocalServerUrl(it.uriString) }
                .sortedByDescending { it.lastPlayedTime }
                .map { state ->
                    localVideosMap[state.uriString] ?: run {
                        val name = Uri.parse(state.uriString).lastPathSegment ?: state.uriString
                        Video(
                            id = state.uriString.hashCode().toLong(),
                            path = state.uriString,
                            parentPath = "",
                            duration = 0L,
                            uriString = state.uriString,
                            nameWithExtension = name,
                            width = 0,
                            height = 0,
                            size = 0L,
                            playbackPosition = state.playbackPosition,
                            lastPlayedAt = state.lastPlayedTime?.let { Date(it) }
                        )
                    }
                }
        }
    }
}
