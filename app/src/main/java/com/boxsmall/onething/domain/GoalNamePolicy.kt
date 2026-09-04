package com.boxsmall.onething.domain

import java.text.BreakIterator
import java.util.Locale

object GoalNamePolicy {
    const val MAX_VISIBLE_CHARACTERS = 20

    fun normalize(rawName: String): String {
        val normalized = rawName.trim()
        require(normalized.isNotBlank()) { "Goal name cannot be blank" }
        require('\n' !in normalized && '\r' !in normalized) { "Goal name must be one line" }
        require(visibleLength(normalized) <= MAX_VISIBLE_CHARACTERS) {
            "Goal name cannot exceed $MAX_VISIBLE_CHARACTERS visible characters"
        }
        return normalized
    }

    fun visibleLength(text: String): Int {
        if (text.isEmpty()) return 0
        val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
        iterator.setText(text)
        var count = 0
        var boundary = iterator.first()
        while (boundary != BreakIterator.DONE) {
            val next = iterator.next()
            if (next == BreakIterator.DONE) break
            count += 1
            boundary = next
        }
        return count
    }
}
