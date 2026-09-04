package com.boxsmall.onething.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.boxsmall.onething.domain.GoalNamePolicy

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["activeSlot"], unique = true),
    ],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long? = null,
    val activeSlot: Int? = ACTIVE_SLOT,
) {
    init {
        require(name.isNotBlank()) { "Goal name cannot be blank" }
        require(GoalNamePolicy.visibleLength(name) <= MAX_NAME_LENGTH) {
            "Goal name cannot exceed $MAX_NAME_LENGTH characters"
        }
        require(activeSlot == null || activeSlot == ACTIVE_SLOT) { "activeSlot must be null or 1" }
    }

    val isActive: Boolean get() = activeSlot == ACTIVE_SLOT

    companion object {
        const val ACTIVE_SLOT = 1
        const val MAX_NAME_LENGTH = GoalNamePolicy.MAX_VISIBLE_CHARACTERS
    }
}
