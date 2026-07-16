package dev.anilbeesetti.nextplayer.feature.videopicker.screens.history

import android.net.Uri
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.domain.MediaHolder
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.MediaView

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onPlayVideo: (uri: Uri) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)

    HistoryScreen(
        uiState = uiState,
        onVideoClick = onPlayVideo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onVideoClick: (Uri) -> Unit = {},
) {
    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.history),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaffoldPadding.calculateTopPadding())
                .padding(start = scaffoldPadding.calculateStartPadding(LocalLayoutDirection.current)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background),
            ) {
                MediaView(
                    recentlyPlayedVideo = null,
                    recentlyPlayedFolder = null,
                    mediaHolder = MediaHolder(
                        videos = uiState.videos,
                        folders = emptyList(),
                    ),
                    preferences = ApplicationPreferences(),
                    restoredFocusKey = null,
                    onItemFocused = {},
                    onFolderClick = {},
                    onVideoClick = onVideoClick,
                    showHeaders = false,
                    contentPadding = scaffoldPadding.copy(top = 0.dp, start = 0.dp),
                )
            }
        }
    }
}
