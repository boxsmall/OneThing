package com.boxsmall.onething.data

import android.content.Context
import androidx.annotation.XmlRes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boxsmall.onething.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class BackupPolicyDeviceTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun android12PolicyDisablesCloudAndTransfersRoomAndDataStore() {
        val rules = readRules(R.xml.data_extraction_rules)

        assertEquals(
            setOf(
                Rule("cloud-backup", "exclude", "root", "."),
                Rule("cloud-backup", "exclude", "file", "."),
                Rule("cloud-backup", "exclude", "database", "."),
                Rule("cloud-backup", "exclude", "sharedpref", "."),
                Rule("cloud-backup", "exclude", "external", "."),
                Rule("device-transfer", "include", "database", "."),
                Rule("device-transfer", "include", "file", "datastore/"),
                Rule("device-transfer", "include", "sharedpref", "."),
            ),
            rules,
        )
    }

    @Test
    fun legacyFullBackupExcludesEveryAppDataDomain() {
        assertEquals(
            setOf(
                Rule("full-backup-content", "exclude", "root", "."),
                Rule("full-backup-content", "exclude", "file", "."),
                Rule("full-backup-content", "exclude", "database", "."),
                Rule("full-backup-content", "exclude", "sharedpref", "."),
                Rule("full-backup-content", "exclude", "external", "."),
            ),
            readRules(R.xml.backup_rules),
        )
    }

    private fun readRules(@XmlRes resourceId: Int): Set<Rule> = buildSet {
        context.resources.getXml(resourceId).use { parser ->
            val parents = ArrayDeque<String>()
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name
                        if (tag == "include" || tag == "exclude") {
                            add(
                                Rule(
                                    section = parents.last(),
                                    action = tag,
                                    domain = parser.getAttributeValue(null, "domain"),
                                    path = parser.getAttributeValue(null, "path"),
                                ),
                            )
                        }
                        parents.addLast(tag)
                    }

                    XmlPullParser.END_TAG -> parents.removeLast()
                }
                parser.next()
            }
        }
    }

    private data class Rule(
        val section: String,
        val action: String,
        val domain: String,
        val path: String,
    )
}
