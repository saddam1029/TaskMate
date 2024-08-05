package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        val taskAdapter = TaskAdapter(
            { task ->
                val intent = Intent(this, CreateTaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            },
            { task ->
                taskViewModel.delete(task)
            }
        )
        binding.rvTasks.adapter = taskAdapter
        binding.rvTasks.layoutManager = LinearLayoutManager(this)

        val highPriorityAdapter = HighPriorityTaskAdapter(
            { task ->
                val intent = Intent(this, CreateTaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            },
            { task ->
                taskViewModel.delete(task)
            }
        )
        binding.rvHigh.adapter = highPriorityAdapter
        binding.rvHigh.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        taskViewModel.allTasks.observe(this) { tasks ->
            tasks?.let {
                // Filter tasks based on priority
                val highPriorityTasks = it.filter { task -> task.priority == "High" }
                val normalPriorityTasks = it.filter { task -> task.priority != "High" }

                highPriorityAdapter.submitList(highPriorityTasks)
                taskAdapter.submitList(normalPriorityTasks)
            }
        }

        binding.btAddTasks.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
