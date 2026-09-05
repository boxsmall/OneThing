package com.boxsmall.onething.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boxsmall.onething.data.local.OneThingDatabase
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OneThingDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2PreservesGoalsAndCompletionsAndDefaultsIcon() {
        helper.createDatabase(TEST_DB, 1).apply {
            insertV1GoalAndCompletion()
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            OneThingDatabase.MIGRATION_1_2,
        ).use { db ->
            db.query(
                "SELECT name, iconKey, startEpochDay, activeSlot FROM goals WHERE id = 7",
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals("原有目标", cursor.getString(0))
                assertEquals("other", cursor.getString(1))
                assertEquals(20_330L, cursor.getLong(2))
                assertEquals(1, cursor.getInt(3))
            }
            db.query(
                "SELECT goalId, dateEpochDay, completedAtMillis FROM completions WHERE id = 9",
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals(7L, cursor.getLong(0))
                assertEquals(20_330L, cursor.getLong(1))
                assertEquals(123_456L, cursor.getLong(2))
            }
        }
    }

    private fun SupportSQLiteDatabase.insertV1GoalAndCompletion() {
        execSQL(
            "INSERT INTO goals(id, name, startEpochDay, endEpochDay, activeSlot) VALUES(7, '原有目标', 20330, NULL, 1)",
        )
        execSQL(
            "INSERT INTO completions(id, goalId, dateEpochDay, completedAtMillis) VALUES(9, 7, 20330, 123456)",
        )
    }

    private companion object {
        const val TEST_DB = "migration-v1-v2"
    }
}
