package com.boxsmall.onething.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boxsmall.onething.core.DateProvider
import com.boxsmall.onething.data.local.CompletionEntity
import com.boxsmall.onething.data.local.GoalEntity
import com.boxsmall.onething.data.local.OneThingDatabase
import com.boxsmall.onething.domain.GoalNamePolicy
import com.boxsmall.onething.domain.GoalIconKey
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseConstraintsTest {
    private lateinit var database: OneThingDatabase
    private val today = LocalDate.of(2026, 9, 2)

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, OneThingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun databaseAllowsOnlyOneActiveGoal() = runBlocking {
        database.goalDao().insert(GoalEntity(name = "目标一", startEpochDay = today.toEpochDay()))
        database.goalDao().insert(GoalEntity(name = "目标二", startEpochDay = today.toEpochDay()))
        Unit
    }

    @Test
    fun completionIsUniquePerGoalAndDate() = runBlocking {
        val goalId = database.goalDao().insert(
            GoalEntity(name = "每天运动", startEpochDay = today.toEpochDay()),
        )
        val completion = CompletionEntity(
            goalId = goalId,
            dateEpochDay = today.toEpochDay(),
            completedAtMillis = 1L,
        )

        val first = database.completionDao().insert(completion)
        val duplicate = database.completionDao().insert(completion.copy(completedAtMillis = 2L))

        assertNotEquals(-1L, first)
        assertEquals(-1L, duplicate)
        assertEquals(1, database.completionDao().countForGoal(goalId))
    }

    @Test
    fun replacingGoalOnSameDayEndsOldAndCanCompleteNew() = runBlocking {
        val repository = GoalRepository(
            database = database,
            goalDao = database.goalDao(),
            completionDao = database.completionDao(),
            dateProvider = DateProvider { today },
        )
        val oldGoalId = repository.createGoal("旧目标")
        repository.completeToday()

        val newGoalId = repository.replaceActiveGoal("新目标", completeNewGoalToday = true)

        val oldGoal = database.goalDao().getById(oldGoalId)
        val newGoal = database.goalDao().getById(newGoalId)
        assertNull(oldGoal?.activeSlot)
        assertEquals(today.toEpochDay(), oldGoal?.endEpochDay)
        assertEquals(GoalEntity.ACTIVE_SLOT, newGoal?.activeSlot)
        assertEquals(listOf(today.toEpochDay()), database.completionDao().getDates(oldGoalId))
        assertEquals(listOf(today.toEpochDay()), database.completionDao().getDates(newGoalId))
    }

    @Test
    fun renamingActiveGoalKeepsIdentityStartDateAndCompletions() = runBlocking {
        val repository = GoalRepository(
            database = database,
            goalDao = database.goalDao(),
            completionDao = database.completionDao(),
            dateProvider = DateProvider { today },
        )
        val goalId = repository.createGoal("  每天走路  ")
        repository.completeToday()

        val renamed = repository.renameActiveGoal("  每天读书 📚  ")

        val goal = database.goalDao().getById(goalId)
        assertEquals(true, renamed)
        assertEquals(goalId, goal?.id)
        assertEquals("每天读书 📚", goal?.name)
        assertEquals(today.toEpochDay(), goal?.startEpochDay)
        assertEquals(GoalEntity.ACTIVE_SLOT, goal?.activeSlot)
        assertEquals(listOf(today.toEpochDay()), database.completionDao().getDates(goalId))
    }

    @Test
    fun changingActiveGoalIconKeepsIdentityAndCompletions() = runBlocking {
        val repository = GoalRepository(
            database = database,
            goalDao = database.goalDao(),
            completionDao = database.completionDao(),
            dateProvider = DateProvider { today },
        )
        val goalId = repository.createGoal("每天走路")
        repository.completeToday()

        assertEquals(true, repository.updateActiveGoalIcon(GoalIconKey.WALK))

        val goal = database.goalDao().getById(goalId)
        assertEquals(GoalIconKey.WALK.storageValue, goal?.iconKey)
        assertEquals(goalId, goal?.id)
        assertEquals(listOf(today.toEpochDay()), database.completionDao().getDates(goalId))
    }

    @Test
    fun visibleCharacterPolicyMatchesEmojiOnAndroid() {
        assertEquals(1, GoalNamePolicy.visibleLength("👨‍👩‍👧‍👦"))
    }
}
