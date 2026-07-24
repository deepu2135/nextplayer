package dev.anilbeesetti.nextplayer.feature.videopicker.screens.history

import android.net.Uri
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.domain.MediaHolder
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.MediaView
import dev.anilbeesetti.nextplayer.feature.videopicker.state.rememberSelectionManager

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onPlayVideo: (uri: Uri) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)

    HistoryScreen(
        uiState = uiState,
        onVideoClick = onPlayVideo,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onVideoClick: (Uri) -> Unit = {},
    onAction: (HistoryAction) -> Unit = {},
) {
    val selectionManager = rememberSelectionManager()

    BackHandler(enabled = selectionManager.isInSelectionMode) {
        selectionManager.exitSelectionMode()
    }

    Scaffold(
        topBar = {
            val selectedItemsSize = selectionManager.selectionItems.size
            val totalItemsSize = uiState.videos.size
            NextTopAppBar(
                title = {
                    if (selectionManager.isInSelectionMode) {
                        Text(
                            text = stringResource(R.string.m_n_selected, selectedItemsSize, totalItemsSize),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.history),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    if (selectionManager.isInSelectionMode) {
                        IconButton(
                            onClick = { selectionManager.exitSelectionMode() },
                        ) {
                            Icon(
                                imageVector = NextIcons.Close,
                                contentDescription = stringResource(id = R.string.navigate_up),
                            )
                        }
                    }
                },
                actions = {
                    if (selectionManager.isInSelectionMode) {
                        IconButton(
                            onClick = {
                                if (selectedItemsSize != totalItemsSize) {
                                    uiState.videos.forEach { selectionManager.selectVideo(it) }
                                } else {
                                    selectionManager.clearSelection()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (selectedItemsSize != totalItemsSize) {
                                    NextIcons.SelectAll
                                } else {
                                    NextIcons.DeselectAll
                                },
                                contentDescription = if (selectedItemsSize != totalItemsSize) {
                                    stringResource(R.string.select_all)
                                } else {
                                    stringResource(R.string.deselect_all)
                                },
                            )
                        }
                        IconButton(
                            onClick = {
                                val uris = selectionManager.selectionItems.map { it.id }
                                onAction(HistoryAction.DeleteVideos(uris))
                                selectionManager.exitSelectionMode()
                            },
                        ) {
                            Icon(
                                imageVector = NextIcons.Delete,
                                contentDescription = stringResource(id = R.string.delete),
                            )
                        }
                    }
                }
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
                    preferences = uiState.preferences,
                    restoredFocusKey = null,
                    selectionManager = selectionManager,
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
