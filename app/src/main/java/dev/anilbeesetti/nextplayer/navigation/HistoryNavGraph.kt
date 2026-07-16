package dev.anilbeesetti.nextplayer.navigation

import android.content.Context
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.anilbeesetti.nextplayer.feature.videopicker.navigation.HistoryRoute

fun EntryProviderScope<NavKey>.historyNavGraph(
    context: Context,
    backStack: NavBackStack<NavKey>,
) {
    entry<HistoryRoute> {
        dev.anilbeesetti.nextplayer.feature.videopicker.screens.history.HistoryRoute(
            onPlayVideo = { uri -> context.startPlayback(listOf(uri)) }
        )
    }
}
