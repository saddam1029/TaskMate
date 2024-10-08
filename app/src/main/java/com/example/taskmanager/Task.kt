package com.example.taskmanager

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val priority: String,
    var status: String = "Incomplete",  // New field to indicate task status
    val alarmSoundUri: String? = null // Ensure this field is included
) : Parcelable



