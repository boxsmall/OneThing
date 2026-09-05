package com.boxsmall.onething.ui

import android.content.Context
import com.airbnb.lottie.LottieCompositionFactory
import com.boxsmall.onething.R
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionAnimationResourceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val resources = listOf(
        R.raw.complete_success,
        R.raw.complete_streak_3,
        R.raw.complete_streak_7,
        R.raw.complete_streak_30,
    )

    @Test
    fun everyAnimationParsesAndMatchesDeliveryContract() {
        resources.forEach { resourceId ->
            val bytes = context.resources.openRawResource(resourceId).use { it.readBytes() }
            val json = JSONObject(bytes.toString(Charsets.UTF_8))
            val layers = json.getJSONArray("layers")
            val names = (0 until layers.length()).map { layers.getJSONObject(it).getString("nm") }

            assertTrue(bytes.size <= 200_000)
            assertEquals(1080, json.getInt("w"))
            assertEquals(1080, json.getInt("h"))
            assertEquals(60, json.getInt("fr"))
            assertEquals(1.8, (json.getDouble("op") - json.getDouble("ip")) / 60.0, 0.001)
            assertEquals(0, json.getJSONArray("assets").length())
            assertFalse((0 until layers.length()).any { layers.getJSONObject(it).getInt("ty") == 5 })
            assertTrue(names.contains("Number_1"))
            assertTrue(names.contains("Status_Point"))
            assertTrue(names.contains("Glow"))
            assertTrue(names.contains("Orbit_Path"))
            assertTrue(names.contains("Success_Check"))
            assertNotNull(LottieCompositionFactory.fromRawResSync(context, resourceId).value)
        }
    }
}
