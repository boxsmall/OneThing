package com.boxsmall.onething.ui

internal enum class AppPage {
    HOME,
    RECORD,
    HISTORY,
    SETTINGS,
    ABOUT,
    END_RESULT,
}

internal fun backDestination(
    page: AppPage,
    historyReturnPage: AppPage,
): AppPage? = when (page) {
    AppPage.HOME -> null
    AppPage.RECORD -> AppPage.HOME
    AppPage.HISTORY -> historyReturnPage
    AppPage.SETTINGS -> AppPage.HOME
    AppPage.ABOUT -> AppPage.SETTINGS
    AppPage.END_RESULT -> AppPage.HOME
}
