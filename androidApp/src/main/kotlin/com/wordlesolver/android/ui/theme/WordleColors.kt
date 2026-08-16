package com.wordlesolver.android.ui.theme

import androidx.compose.ui.graphics.Color

object WordleColors {
    val Green = Color(0xFF6CA866)
    val Yellow = Color(0xFFCBB25A)
    val Gray = Color(0xFF4A4A4A)
    val White = Color.White
    val ResultBackground = Color(0xFFEBE7F4)
    val WordleWordBackground = Color(0xFFB9B0D4)
    /** Background for answers in "Previous Answers" that came out more than once. */
    val RepeatedAnswerBackground = Color(0xFFE3C989)
    /** Darker accent used for the screen's main title, darker than the section titles. */
    val TitleDark = Color(0xFF2D2150)
    /** Border color used around each word card (dictionaries, past answers, results). */
    val WordCardBorder = Color(0xFFC9C0E0)
    /** Text color used inside each word card. */
    val WordCardText = Color(0xFF3A3358)
}