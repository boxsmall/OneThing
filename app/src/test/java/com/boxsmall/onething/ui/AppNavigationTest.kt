package com.boxsmall.onething.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppNavigationTest {
    @Test
    fun homeLetsTheActivityHandleBack() {
        assertNull(backDestination(AppPage.HOME, AppPage.HOME))
    }

    @Test
    fun primaryPagesReturnToTheirExpectedParent() {
        assertEquals(AppPage.HOME, backDestination(AppPage.RECORD, AppPage.HOME))
        assertEquals(AppPage.HOME, backDestination(AppPage.SETTINGS, AppPage.HOME))
        assertEquals(AppPage.SETTINGS, backDestination(AppPage.ABOUT, AppPage.HOME))
        assertEquals(AppPage.HOME, backDestination(AppPage.END_RESULT, AppPage.HOME))
    }

    @Test
    fun historyReturnsToItsRecordedSource() {
        AppPage.entries.forEach { source ->
            assertEquals(source, backDestination(AppPage.HISTORY, source))
        }
    }
}
