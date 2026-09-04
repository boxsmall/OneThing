package com.boxsmall.onething.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GoalEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OneThingDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun completionDao(): CompletionDao

    companion object {
        fun create(context: Context): OneThingDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                OneThingDatabase::class.java,
                "onething.db",
            ).build()
    }
}
