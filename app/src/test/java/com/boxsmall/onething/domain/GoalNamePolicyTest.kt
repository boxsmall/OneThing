package com.boxsmall.onething.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GoalNamePolicyTest {
    @Test
    fun trimsNameAndKeepsInternalSpaces() {
        assertEquals("每天 运动", GoalNamePolicy.normalize("  每天 运动  "))
    }

    @Test
    fun rejectsBlankAndMultilineNames() {
        assertThrows(IllegalArgumentException::class.java) { GoalNamePolicy.normalize("   ") }
        assertThrows(IllegalArgumentException::class.java) { GoalNamePolicy.normalize("目标\n备注") }
    }

    @Test
    fun composedEmojiCountsAsOneVisibleCharacter() {
        assertEquals(1, GoalNamePolicy.visibleLength("👨‍👩‍👧‍👦"))
    }

    @Test
    fun rejectsMoreThanTwentyVisibleCharacters() {
        assertThrows(IllegalArgumentException::class.java) {
            GoalNamePolicy.normalize("一".repeat(21))
        }
    }
}
