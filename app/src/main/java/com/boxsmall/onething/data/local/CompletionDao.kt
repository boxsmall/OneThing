package com.boxsmall.onething.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(completion: CompletionEntity): Long

    @Query("SELECT dateEpochDay FROM completions WHERE goalId = :goalId ORDER BY dateEpochDay ASC")
    fun observeDates(goalId: Long): Flow<List<Long>>

    @Query("SELECT dateEpochDay FROM completions WHERE goalId = :goalId ORDER BY dateEpochDay ASC")
    suspend fun getDates(goalId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM completions WHERE goalId = :goalId")
    suspend fun countForGoal(goalId: Long): Int
}
