package com.wordlesolver.android.ui.solver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordlesolver.domain.model.LetterRow

/**
 * Renders a [LetterRow] as 5 boxes that share the available width equally (each box is
 * `weight(1f)` and square via `aspectRatio(1f)`), so the row always spans the full screen
 * width and adapts to any device size.
 *
 * When typable (onBoxTapped == null), typing a letter auto-advances focus to the next box,
 * per spec: "When the user types one letter in an input, they're immediately moved to the
 * next box in the right."
 */
@Composable
fun LetterBoxRow(
    row: LetterRow,
    boxColorFor: (com.wordlesolver.domain.model.LetterState) -> Color,
    onLetterChanged: (position: Int, letter: Char?) -> Unit,
    modifier: Modifier = Modifier,
    onBoxTapped: ((position: Int) -> Unit)? = null
) {
    val focusRequesters = remember(row.letters.size) { List(row.letters.size) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        row.letters.forEach { boxed ->
            LetterBox(
                letter = boxed.letter,
                color = boxColorFor(boxed.state),
                focusRequester = focusRequesters[boxed.position],
                onLetterChanged = { newLetter ->
                    onLetterChanged(boxed.position, newLetter)
                    if (newLetter != null && boxed.position < focusRequesters.lastIndex) {
                        focusRequesters[boxed.position + 1].requestFocus()
                    }
                },
                onTapped = onBoxTapped?.let { { it(boxed.position) } },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Renders a Patterns row: 5 boxes that are colored only — no letter input at all.
 * Tapping a box cycles its color (gray -> green -> yellow -> gray); the letters that
 * ultimately matter come from whichever word the pattern is evaluated against, not
 * from anything typed here.
 */
@Composable
fun PatternColorRow(
    colors: List<com.wordlesolver.domain.model.LetterState>,
    boxColorFor: (com.wordlesolver.domain.model.LetterState) -> Color,
    onBoxTapped: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEachIndexed { position, state ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(boxColorFor(state))
                    .clickable { onBoxTapped(position) }
            )
        }
    }
}

@Composable
private fun LetterBox(
    letter: Char?,
    color: Color,
    focusRequester: FocusRequester,
    onLetterChanged: (Char?) -> Unit,
    onTapped: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val text = letter?.toString().orEmpty()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        // Pattern boxes are still typable (BasicTextField) so the user can enter the
        // letter for that box; onTapped, when present, is wired to a small dedicated
        // corner control below instead of the whole box, so tapping to cycle the
        // color never fights with tapping to focus the field and type.
        BasicTextField(
            value = text,
            onValueChange = { newValue ->
                val newChar = newValue.trim().uppercase().lastOrNull()
                onLetterChanged(if (newChar?.isLetter() == true) newChar else null)
            },
            textStyle = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            modifier = Modifier.focusRequester(focusRequester)
        )

        if (onTapped != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .size(14.dp)
                    .background(Color.White.copy(alpha = 0.35f))
                    .clickable { onTapped() }
            )
        }
    }
}