package com.boxsmall.onething.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boxsmall.onething.MainActivity
import com.boxsmall.onething.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashConfigurationTest {
    @Test
    fun launcherActivityUsesStartingThemeWithoutDedicatedSplashActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val component = ComponentName(context, MainActivity::class.java)
        val activityInfo = packageManager.getActivityInfo(component, PackageManager.GET_META_DATA)
        val packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)

        assertEquals(R.style.Theme_OneThing_Starting, activityInfo.themeResource)
        assertFalse(packageInfo.activities.orEmpty().any { it.name.contains("Splash", ignoreCase = true) })
    }
}
