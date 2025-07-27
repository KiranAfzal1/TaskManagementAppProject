package com.example.mytask.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val date: String,
    val priority: String,
    val category: String,
    var completed: Boolean = false
)
