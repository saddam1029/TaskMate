package com.example.taskmanager

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    fun getIncompleteTasks(): LiveData<List<Task>> {
        return taskDao.getIncompleteTasks()
    }

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }
}



