package com.example.taskmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>
    val incompleteTasks: LiveData<List<Task>>

    init {
        val tasksDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(tasksDao)
        allTasks = repository.allTasks
        incompleteTasks = repository.getIncompleteTasks()
    }

    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}
