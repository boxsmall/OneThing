package com.boxsmall.onething.core

interface AnalyticsTracker {
    fun track(event: String)
}

object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: String) = Unit
}
