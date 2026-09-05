package com.boxsmall.onething.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boxsmall.onething.core.DateProvider
import com.boxsmall.onething.data.local.OneThingDatabase
import com.boxsmall.onething.domain.GoalIconKey
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalIconPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val today = LocalDate.of(2026, 9, 5)

    @Before
    fun clearDatabase() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @After
    fun removeDatabase() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun selectedIconSurvivesDatabaseReopen() = runBlocking {
        val firstDatabase = openDatabase()
        try {
            val database = firstDatabase
            repository(database).createGoal("每天阅读", GoalIconKey.READ)
        } finally {
            firstDatabase.close()
        }

        val reopenedDatabase = openDatabase()
        try {
            val database = reopenedDatabase
            val restored = repository(database).observeActiveGoal().first()
            assertEquals(GoalIconKey.READ, restored?.iconKey)
            assertEquals("每天阅读", restored?.name)
        } finally {
            reopenedDatabase.close()
        }
    }

    private fun openDatabase(): OneThingDatabase =
        Room.databaseBuilder(context, OneThingDatabase::class.java, TEST_DATABASE)
            .addMigrations(OneThingDatabase.MIGRATION_1_2)
            .build()

    private fun repository(database: OneThingDatabase) = GoalRepository(
        database = database,
        goalDao = database.goalDao(),
        completionDao = database.completionDao(),
        dateProvider = DateProvider { today },
    )

    private companion object {
        const val TEST_DATABASE = "goal-icon-persistence.db"
    }
}
