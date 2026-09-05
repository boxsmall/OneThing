package com.boxsmall.onething.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalIconKeyTest {
    @Test
    fun everyStableStorageValueRoundTrips() {
        GoalIconKey.entries.forEach { key ->
            assertEquals(key, GoalIconKey.fromStorage(key.storageValue))
        }
    }

    @Test
    fun missingAndUnknownValuesFallBackToOther() {
        assertEquals(GoalIconKey.OTHER, GoalIconKey.fromStorage(null))
        assertEquals(GoalIconKey.OTHER, GoalIconKey.fromStorage("future-icon"))
    }
}
