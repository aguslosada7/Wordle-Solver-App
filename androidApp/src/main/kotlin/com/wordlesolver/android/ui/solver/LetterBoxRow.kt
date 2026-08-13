package com.wordlesolver.android.ui.solver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordlesolver.domain.model.LetterRow

/**
 * Renders a [LetterRow] as 5 boxes. [onLetterChanged] fires as the user types (position, letter);
 * typing auto-advances focus is left to the caller/parent via [onAutoAdvance] since Compose's
 * FocusManager isn't wired here to keep this composable state-agnostic.
 */
@Composable
fun LetterBoxRow(
    row: LetterRow,
    boxColorFor: (com.wordlesolver.domain.model.LetterState) -> Color,
    onLetterChanged: (position: Int, letter: Char?) -> Unit,
    modifier: Modifier = Modifier,
    onBoxTapped: ((position: Int) -> Unit)? = null
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        row.letters.forEach { boxed ->
            LetterBox(
                letter = boxed.letter,
                color = boxColorFor(boxed.state),
                onLetterChanged = { newLetter -> onLetterChanged(boxed.position, newLetter) },
                onTapped = onBoxTapped?.let { { it(boxed.position) } }
            )
        }
    }
}

@Composable
private fun LetterBox(
    letter: Char?,
    color: Color,
    onLetterChanged: (Char?) -> Unit,
    onTapped: (() -> Unit)?
) {
    var text = letter?.toString().orEmpty()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(48.dp)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (onTapped != null) {
            // Pattern boxes: tap to cycle color instead of typing.
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.background(Color.Transparent)
            )
        } else {
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
                singleLine = true
            )
        }
    }
}
