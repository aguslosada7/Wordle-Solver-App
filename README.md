# Wordle Solver App

An Android mobile app that allows users to guess the Wordle word of the day by filtering with clues such as misplaced letters, correct letters, letters not in the word, patterns, words that have already appeared before.

# Tech Stack
- Kotlin Multiplatform
- Jetpack Compose

# Filtering

## Basic filtering

All colored boxes behave as inputs. When the user types one letter in an input, they're immediately moved to the next box in the right.
Each input has a type, green ones and yellow ones and position matters. As a group of inputs, they behave as a form that gets submitted by a button in the bottom section which triggers the filtering process.
Green letters describe letters that are in the correct position. When submiting the form, those who do not have the "green" letters in the correct positions get filtered out. Yellow letters represent letters that are misplaced, which means that words that have them in those positions are filtered out.

## Filtering by patterns

A pattern row has no letters of its own — only 5 boxes the user cycles between gray, green, and yellow. The letters that give a pattern meaning always come from the specific word it's being evaluated against.

Given a pattern's colors and a word to evaluate against (the "target"), a General-dictionary word ("guess") satisfies the pattern if grading `guess` against `target` using the real Wordle duplicate-letter algorithm produces exactly that color sequence:
1. Green pass: every position where `guess[i] == target[i]` is green; those target letter instances are removed from the pool available for the next pass.
2. Yellow/gray pass, left to right over the remaining positions: a guess letter is yellow if an unmatched instance of it is still in the target's remaining letter pool (consuming it), otherwise gray.

This correctly handles repeated letters — e.g. for target `GEODE` and pattern yellow-gray-green-green-green, the three greens fix `ODE` at positions 3-5, and because `GEODE` itself only has one `E` left over (already used by the green at position 5) plus a `G`, the only guesses that satisfy the pattern are of the shape `E?ODE` where `?` is any letter except `G` or `E` (e.g. `EPODE`, `ERODE`, `EXODE`) — a word like `ABODE` does NOT satisfy it, since grading `ABODE` against `GEODE` yields gray-gray-green-green-green instead.

Once the user adds a pattern, after submitting the app removes words with the basic filtering, and then, for each remaining candidate word (used as the pattern's target), it counts how many General-dictionary words (used as guesses) satisfy each active pattern; if any pattern's count is below its specified expected amount, that candidate is removed from the list. The resulting list is the one shown on screen.

If filtering by patterns is enabled, when selecting a word from the results, a modal is displayed. This modal shows, separated by pattern (each pattern identified by its color emoji sequence and match count, e.g. "🟨⬜🟩🟩🟩 (3 words)"), all General-dictionary words that satisfy that pattern when evaluated against the selected word as the target.

# Previous Answers

Allows you to visualize a list of words pertaining to the file past-wordle-answers.txt
Above the list, there is an icon that signals whether or not the list has been updated up to yesterday's result.

## Wordle Hints API

The app utilizes a text file (past-wordle-answers.txt) to store information about previous answers and updates. The file contains two parts:
1. The date of the file's last update (YY-MM-DD) 
2. The list of past answers

When opening the app, it fetches all answers starting with the word used on the day of the last update up to the previous day of the current date. Next, current date is set as the date of the last update in past-wordle-answers.txt and all obtained words are appended to the end of the file. All words in this file are separated by a space one after the other.   
Because we do not want today's answer to be accesible to the user (as the app's objective is to help them achieve said result), we do not fetch the result for the current date.
The source of all answers is the following endpoint:

```
GET "https://wordlehintstoday.org/api/v1/wordle/answers?from={dateOfLastUpdate}&to={yesterdaysDate}"
```

Example: https://wordlehintstoday.org/api/v1/wordle/answers?from=2026-01-01&to=2026-01-31

To avoid unnecesary calls to this API, if yesterday's answer has already been updated, then the fetching will not be executed.

### Response Format

```
{
  "source": "wordlehintstoday.org",
  "version": "1.0",
  "count": 50,
  "total": 1506,
  "page": 1,
  "per_page": 50,
  "has_more": true,
  "results": [
    {
      "game": 1506,
      "date": "2026-08-02",
      "day_name": "Sunday",
      "editor": "Wordle Hints Today Editorial Team",
      "answer": "GLOBE",
      "difficulty": 3.8
    },
    {
      "game": 1505,
      "date": "2026-08-01",
      "day_name": "Saturday",
      "editor": "Wordle Hints Today Editorial Team",
      "answer": "CRANE",
      "difficulty": 3.4
    }
  ]
}
```

## Dictionaries

Allows you to visualize a list of words pertaining to either:

 * Wordle dictionary (2341 words)
 * General dictionary (12930 words)

The difference between these dictionaries is that the Wordle dictionary only contains words that can be the solution. It does not contain, for example, plurals ending in "S", abbreviations nor archaic terms. However, it is known that words not in the Wordle dictionary can be answers from time to time.