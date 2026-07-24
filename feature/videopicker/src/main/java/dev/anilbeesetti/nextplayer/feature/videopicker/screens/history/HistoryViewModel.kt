package dev.anilbeesetti.nextplayer.feature.videopicker.screens.history

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.domain.DeleteWatchHistoryUseCase
import dev.anilbeesetti.nextplayer.core.domain.GetWatchHistoryUseCase
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Video
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWatchHistoryUseCase: GetWatchHistoryUseCase,
    private val deleteWatchHistoryUseCase: DeleteWatchHistoryUseCase,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(HistoryUiState())
    val uiState = uiStateInternal.asStateFlow()

    private val eventsInternal = Channel<HistoryEvent>()
    val events = eventsInternal.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                getWatchHistoryUseCase(),
                preferencesRepository.applicationPreferences,
            ) { history, preferences ->
                HistoryUiState(videos = history, preferences = preferences)
            }.collect { state ->
                uiStateInternal.value = state
            }
        }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.PlayVideo -> playVideo(action.video)
            is HistoryAction.DeleteVideos -> deleteVideos(action.uris)
        }
    }

    private fun playVideo(video: Video) {
        viewModelScope.launch {
            eventsInternal.send(HistoryEvent.PlayVideo(Uri.parse(video.uriString)))
        }
    }

    private fun deleteVideos(uris: List<String>) {
        viewModelScope.launch {
            deleteWatchHistoryUseCase(uris)
        }
    }
}

@Stable
data class HistoryUiState(
    val videos: List<Video> = emptyList(),
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface HistoryAction {
    data class PlayVideo(val video: Video) : HistoryAction
    data class DeleteVideos(val uris: List<String>) : HistoryAction
}

sealed interface HistoryEvent {
    data class PlayVideo(val uri: Uri) : HistoryEvent
}
