package com.wordlesolver.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.wordlesolver.android.ui.theme.WordleColors
import kotlin.math.max

/** Border color used around each word card, per the reference design. */
private val WordCardBorder = Color(0xFFC9C0E0)
private val WordCardText = Color(0xFF3A3358)

/** Minimum width a word card should get before the grid adds another column. */
private val WordCardMinWidth = 84.dp

/**
 * Renders [words] as a responsive grid of rounded "pill" cards (light lavender
 * background, subtle border, italic lowercase text) instead of a plain vertical list.
 * The number of columns adapts to the available screen width, so it works on any device.
 *
 * Intended for use as a screen's own scroll container (it is itself a LazyVerticalGrid).
 * For use *inside* another LazyColumn (e.g. the Solver results, which sit below other
 * scrollable content), use [WordGridRows] instead to avoid nesting two scrollables.
 */
@Composable
fun WordGrid(
    words: List<String>,
    modifier: Modifier = Modifier,
    backgroundColorFor: (String) -> Color = { WordleColors.ResultBackground },
    onWordClick: ((String) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = WordCardMinWidth),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words) { word ->
            WordCard(word = word, backgroundColor = backgroundColorFor(word), onClick = onWordClick)
        }
    }
}

/**
 * Same visual grid as [WordGrid], but pre-chunked into plain (non-lazy) rows so it can be
 * dropped into an existing `LazyColumn` as a sequence of `item { }` rows without nesting
 * two scrollable containers (which Compose does not support well).
 *
 * Usage: `items(WordGridRows.chunk(words, columns)) { row -> WordGridRow(row, ...) }`,
 * or simply call [WordGridRows] as a Composable to render every row directly.
 */
@Composable
fun WordGridRows(
    words: List<String>,
    modifier: Modifier = Modifier,
    backgroundColorFor: (String) -> Color = { WordleColors.ResultBackground },
    onWordClick: ((String) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = max(1, (maxWidth / (WordCardMinWidth + 8.dp)).toInt())
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.chunked(columns).forEach { rowWords ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowWords.forEach { word ->
                        WordCard(
                            word = word,
                            backgroundColor = backgroundColorFor(word),
                            onClick = onWordClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Pad the last row so its cards don't stretch to fill the row alone.
                    repeat(columns - rowWords.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun WordCard(
    word: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WordleColors.ResultBackground,
    onClick: ((String) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1.9f)
            .let { base -> if (onClick != null) base.clickable { onClick(word) } else base }
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, WordCardBorder), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word.lowercase(),
            fontStyle = FontStyle.Italic,
            color = WordCardText
        )
    }
}