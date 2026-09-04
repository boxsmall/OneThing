package com.boxsmall.onething.data

import androidx.room.withTransaction
import com.boxsmall.onething.core.DateProvider
import com.boxsmall.onething.data.local.CompletionDao
import com.boxsmall.onething.data.local.CompletionEntity
import com.boxsmall.onething.data.local.GoalDao
import com.boxsmall.onething.data.local.GoalEntity
import com.boxsmall.onething.data.local.OneThingDatabase
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.domain.GoalNamePolicy
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class GoalRepository(
    private val database: OneThingDatabase,
    private val goalDao: GoalDao,
    private val completionDao: CompletionDao,
    private val dateProvider: DateProvider,
) {
    fun observeActiveGoal(): Flow<GoalSnapshot?> = goalDao.observeActive().flatMapLatest { goal ->
        if (goal == null) {
            flowOf(null)
        } else {
            completionDao.observeDates(goal.id).map { dates -> goal.toSnapshot(dates) }
        }
    }

    fun observeHistory(): Flow<List<GoalSnapshot>> = goalDao.observeEnded().flatMapLatest { goals ->
        if (goals.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(goals.map { goal ->
                completionDao.observeDates(goal.id).map { dates -> goal.toSnapshot(dates) }
            }) { snapshots -> snapshots.toList() }
        }
    }

    suspend fun createGoal(rawName: String): Long = database.withTransaction {
        check(goalDao.getActive() == null) { "An active goal already exists" }
        goalDao.insert(newGoal(rawName))
    }

    suspend fun completeToday(): Boolean = database.withTransaction {
        val goal = goalDao.getActive() ?: return@withTransaction false
        completionDao.insert(
            CompletionEntity(
                goalId = goal.id,
                dateEpochDay = dateProvider.today().toEpochDay(),
                completedAtMillis = System.currentTimeMillis(),
            ),
        ) != -1L
    }

    suspend fun endActiveGoal(): Boolean = database.withTransaction {
        val active = goalDao.getActive() ?: return@withTransaction false
        goalDao.end(active.id, dateProvider.today().toEpochDay()) == 1
    }

    suspend fun renameActiveGoal(rawName: String): Boolean = database.withTransaction {
        val normalizedName = GoalNamePolicy.normalize(rawName)
        goalDao.renameActive(normalizedName) == 1
    }

    suspend fun replaceActiveGoal(rawName: String, completeNewGoalToday: Boolean = false): Long =
        database.withTransaction {
            val today = dateProvider.today()
            goalDao.getActive()?.let { current ->
                check(goalDao.end(current.id, today.toEpochDay()) == 1)
            }
            val newGoalId = goalDao.insert(newGoal(rawName, today))
            if (completeNewGoalToday) {
                completionDao.insert(
                    CompletionEntity(
                        goalId = newGoalId,
                        dateEpochDay = today.toEpochDay(),
                        completedAtMillis = System.currentTimeMillis(),
                    ),
                )
            }
            newGoalId
        }

    private fun newGoal(rawName: String, today: LocalDate = dateProvider.today()): GoalEntity {
        val normalizedName = GoalNamePolicy.normalize(rawName)
        return GoalEntity(name = normalizedName, startEpochDay = today.toEpochDay())
    }

    private fun GoalEntity.toSnapshot(dates: List<Long>): GoalSnapshot = GoalSnapshot(
        id = id,
        name = name,
        startDate = LocalDate.ofEpochDay(startEpochDay),
        endDate = endEpochDay?.let(LocalDate::ofEpochDay),
        completionDates = dates.mapTo(mutableSetOf(), LocalDate::ofEpochDay),
    )
}
