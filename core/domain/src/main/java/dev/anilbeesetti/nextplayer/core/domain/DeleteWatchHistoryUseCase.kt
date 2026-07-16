package dev.anilbeesetti.nextplayer.core.domain

import dev.anilbeesetti.nextplayer.core.data.repository.MediaRepository
import javax.inject.Inject

class DeleteWatchHistoryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(uris: List<String>) {
        mediaRepository.deleteWatchHistory(uris)
    }
}
