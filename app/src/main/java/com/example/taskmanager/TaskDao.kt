package com.example.taskmanager

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table ORDER BY id ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE status != 'Complete' ORDER BY id ASC")
    fun getIncompleteTasks(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
}



