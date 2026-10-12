package net.oraphael.anote.ui.review

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Arrastar para reordenar numa LazyColumn. Só itens com chave do tipo Long
// (os registros) participam; cabeçalhos ficam fixos.
class ReorderState(
    private val listState: LazyListState,
    private val onMove: (fromKey: Long, toKey: Long) -> Unit,
) {
    var draggingKey by mutableStateOf<Long?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set

    private fun info(key: Long): LazyListItemInfo? =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }

    fun start(key: Long) {
        draggingKey = key
        dragOffset = 0f
    }

    fun drag(delta: Float) {
        val key = draggingKey ?: return
        dragOffset += delta
        val current = info(key) ?: return
        val middle = current.offset + dragOffset + current.size / 2f
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key is Long && it.key != key && middle.toInt() in it.offset..(it.offset + it.size)
        } ?: return
        onMove(key, target.key as Long)
        // O item muda de lugar na lista; o deslocamento passa a contar da nova posição.
        dragOffset += current.offset - target.offset
    }

    fun end() {
        draggingKey = null
        dragOffset = 0f
    }
}
