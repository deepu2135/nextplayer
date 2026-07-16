package dev.anilbeesetti.nextplayer.feature.videopicker.screens.history

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.domain.GetWatchHistoryUseCase
import dev.anilbeesetti.nextplayer.core.model.Video
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWatchHistoryUseCase: GetWatchHistoryUseCase,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(HistoryUiState())
    val uiState = uiStateInternal.asStateFlow()

    private val eventsInternal = Channel<HistoryEvent>()
    val events = eventsInternal.receiveAsFlow()

    init {
        viewModelScope.launch {
            getWatchHistoryUseCase().collect { history ->
                uiStateInternal.update { it.copy(videos = history) }
            }
        }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.PlayVideo -> playVideo(action.video)
        }
    }

    private fun playVideo(video: Video) {
        viewModelScope.launch {
            eventsInternal.send(HistoryEvent.PlayVideo(Uri.parse(video.uriString)))
        }
    }
}

@Stable
data class HistoryUiState(
    val videos: List<Video> = emptyList(),
)

sealed interface HistoryAction {
    data class PlayVideo(val video: Video) : HistoryAction
}

sealed interface HistoryEvent {
    data class PlayVideo(val uri: Uri) : HistoryEvent
}
