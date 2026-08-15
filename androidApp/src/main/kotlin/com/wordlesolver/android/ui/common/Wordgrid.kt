package com.wordlesolver.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.wordlesolver.android.ui.theme.WordleColors

/** Border color used around each word card, per the reference design. */
private val WordCardBorder = Color(0xFFC9C0E0)
private val WordCardText = Color(0xFF3A3358)

/**
 * Renders [words] as a wrapping flow of small "pill" cards (light lavender background,
 * subtle border) sized to just barely fit their text, instead of large fixed-size grid
 * cells. Wraps its own vertical scroll, so it's meant to be used as a screen's own
 * scroll container (do not nest it inside another scrollable).
 */
@Composable
fun WordGrid(
    words: List<String>,
    modifier: Modifier = Modifier,
    backgroundColorFor: (String) -> Color = { WordleColors.ResultBackground },
    uppercase: Boolean = false,
    onWordClick: ((String) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState()
) {
    FlowRow(
        modifier = modifier.verticalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        words.forEach { word ->
            WordCard(
                word = word,
                backgroundColor = backgroundColorFor(word),
                uppercase = uppercase,
                onClick = onWordClick
            )
        }
    }
}

@Composable
fun WordCard(
    word: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WordleColors.ResultBackground,
    uppercase: Boolean = false,
    onClick: ((String) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .let { base -> if (onClick != null) base.clickable { onClick(word) } else base }
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, WordCardBorder), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (uppercase) word.uppercase() else word.lowercase(),
            fontStyle = if (uppercase) FontStyle.Normal else FontStyle.Italic,
            color = WordCardText
        )
    }
}