package com.boxsmall.onething.domain

enum class GoalIconKey(val storageValue: String) {
    WALK("walk"),
    READ("read"),
    SLEEP("sleep"),
    WATER("water"),
    STRETCH("stretch"),
    STUDY("study"),
    MEDICINE("medicine"),
    OTHER("other");

    companion object {
        fun fromStorage(value: String?): GoalIconKey =
            entries.firstOrNull { it.storageValue == value } ?: OTHER
    }
}
