package dev.anilbeesetti.nextplayer.feature.player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import dev.anilbeesetti.nextplayer.feature.player.model.Chapter
import kotlin.time.Duration.Companion.milliseconds
import dev.anilbeesetti.nextplayer.feature.player.extensions.formatted

import dev.anilbeesetti.nextplayer.core.ui.R

@Composable
fun BoxScope.ChaptersSelectorView(
    show: Boolean,
    player: Player,
    chapters: List<Chapter>,
    onDismiss: () -> Unit = {},
) {
    OverlayView(
        show = show,
        title = stringResource(id = R.string.chapters),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(chapters) { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            player.seekTo(chapter.startTimeMs)
                            onDismiss()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = chapter.startTimeMs.milliseconds.formatted(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
