package net.oraphael.anote.ui.review

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.MIN_FOCUSES
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(app: AnoteApp, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    val pending by remember { app.reviewRepository.pendingTasks(today) }.collectAsState(initial = null)
    // Ordem de trabalho da revisão, inicializada pela idade.
    val order = remember { mutableStateListOf<ListEntry>() }
    LaunchedEffect(pending) {
        val fresh = pending ?: return@LaunchedEffect
        val kept = order.filter { old -> fresh.any { it.entry.id == old.entry.id } }
        val added = fresh.filter { new -> kept.none { it.entry.id == new.entry.id } }
        order.clear()
        order.addAll(kept + added)
    }

    fun move(from: Int, to: Int) {
        if (from == to || to !in order.indices) return
        order.add(to, order.removeAt(from))
    }

    fun saveOrder() = scope.launch { app.reviewRepository.saveManualOrder(order.map { it.entry.id }, today) }

    // Focos de amanhã: começa pelos já escolhidos, se a revisão for refeita.
    var focusIds by rememberSaveable { mutableStateOf<Set<Long>?>(null) }
    LaunchedEffect(Unit) {
        if (focusIds == null) focusIds = app.reviewRepository.focusIdsFor(today.plusDays(1))
    }
    val chosen = focusIds.orEmpty().filter { id -> order.any { it.entry.id == id } }.toSet()
    val minimum = minOf(MIN_FOCUSES, order.size)
    val canComplete = pending != null && focusIds != null && chosen.size >= minimum

    val listState = rememberLazyListState()
    val reorder = remember {
        ReorderState(listState) { fromKey, toKey ->
            move(order.indexOfFirst { it.entry.id == fromKey }, order.indexOfFirst { it.entry.id == toKey })
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_daily_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.padding(Spacing.l).navigationBarsPadding()) {
                Text(
                    text = if (order.isEmpty()) stringResource(R.string.review_empty)
                    else stringResource(R.string.review_focus_hint, minimum),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalColorTokens.current.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.s),
                )
                Button(
                    onClick = {
                        scope.launch {
                            app.reviewRepository.completeDaily(today, order.map { it.entry.id }, chosen)
                            onClose()
                        }
                    },
                    enabled = canComplete,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.review_complete)) }
            }
        },
    ) { innerPadding ->
        if (pending == null) return@Scaffold
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.l),
        ) {
            item(key = "intro") {
                Text(
                    text = stringResource(R.string.review_daily_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = Spacing.s),
                )
                Text(
                    text = stringResource(R.string.review_reorder_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalColorTokens.current.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.l),
                )
            }
            items(order, key = { it.entry.id }) { item ->
                val id = item.entry.id
                val dragging = reorder.draggingKey == id
                val moveUp = stringResource(R.string.review_move_up)
                val moveDown = stringResource(R.string.review_move_down)
                ReviewRow(
                    item = item,
                    dragging = dragging,
                    isFocus = id in chosen,
                    onFocusChange = { on -> focusIds = if (on) chosen + id else chosen - id },
                    modifier = Modifier
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging) reorder.dragOffset else 0f }
                        .then(if (dragging) Modifier else Modifier.animateItem())
                        .pointerInput(id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    reorder.start(id)
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    reorder.drag(amount.y)
                                },
                                onDragEnd = {
                                    reorder.end()
                                    saveOrder()
                                },
                                onDragCancel = { reorder.end() },
                            )
                        }
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(moveUp) {
                                    val index = order.indexOfFirst { it.entry.id == id }
                                    move(index, index - 1)
                                    saveOrder()
                                    true
                                },
                                CustomAccessibilityAction(moveDown) {
                                    val index = order.indexOfFirst { it.entry.id == id }
                                    move(index, index + 1)
                                    saveOrder()
                                    true
                                },
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(
    item: ListEntry,
    dragging: Boolean,
    isFocus: Boolean,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalColorTokens.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .shadow(if (dragging) 6.dp else 0.dp, MaterialTheme.shapes.medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.m)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(text = item.entry.text, style = MaterialTheme.typography.bodyLarge)
                FilterChip(
                    selected = isFocus,
                    onClick = { onFocusChange(!isFocus) },
                    label = { Text(stringResource(R.string.review_focus_toggle)) },
                )
            }
            Icon(
                imageVector = AppIcons.DragHandle,
                contentDescription = stringResource(R.string.review_drag),
                tint = colors.onSurfaceVariant,
            )
        }
    }
}
