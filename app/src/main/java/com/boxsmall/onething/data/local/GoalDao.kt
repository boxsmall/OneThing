package com.boxsmall.onething.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE activeSlot = 1 LIMIT 1")
    fun observeActive(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE activeSlot = 1 LIMIT 1")
    suspend fun getActive(): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getById(goalId: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE activeSlot IS NULL ORDER BY endEpochDay DESC, id DESC")
    fun observeEnded(): Flow<List<GoalEntity>>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Query("UPDATE goals SET name = :name WHERE activeSlot = 1")
    suspend fun renameActive(name: String): Int

    @Query("UPDATE goals SET iconKey = :iconKey WHERE activeSlot = 1")
    suspend fun updateActiveIcon(iconKey: String): Int

    @Query(
        """
        UPDATE goals
        SET activeSlot = NULL, endEpochDay = :endEpochDay
        WHERE id = :goalId AND activeSlot = 1
        """,
    )
    suspend fun end(goalId: Long, endEpochDay: Long): Int
}
