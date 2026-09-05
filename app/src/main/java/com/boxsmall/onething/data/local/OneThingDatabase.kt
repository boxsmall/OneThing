package com.boxsmall.onething.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GoalEntity::class, CompletionEntity::class],
    version = 2,
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
            )
                .addMigrations(MIGRATION_1_2)
                .build()

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE goals ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'other'",
                )
            }
        }
    }
}
