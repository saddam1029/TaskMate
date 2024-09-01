package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.databinding.ActivityTaskCompleteBinding

class TaskComplete : AppCompatActivity() {

    private lateinit var binding: ActivityTaskCompleteBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var completedTaskAdapter: CompletedTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        window.statusBarColor = ContextCompat.getColor(this, R.color.app_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.app_background)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true


        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        completedTaskAdapter = CompletedTaskAdapter(
            { task ->
                val intent = Intent(this, CreateTaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            },
            { task -> taskViewModel.delete(task) },
            { task ->
                task.status = "Incomplete"
                taskViewModel.update(task)
            }
        )

        binding.rvTasksComplete.adapter = completedTaskAdapter
        binding.rvTasksComplete.layoutManager = LinearLayoutManager(this)

        taskViewModel.allTasks.observe(this) { tasks ->
            val completedTasks = tasks.filter { it.status == "Complete" }
            completedTaskAdapter.submitList(completedTasks)
        }

        setupSearchView()
    }

    private fun setupSearchView() {
        binding.ivSearch.setOnClickListener {
            binding.searchView.visibility = View.VISIBLE
            binding.searchView.isIconified = false
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                completedTaskAdapter.filter.filter(newText)
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            binding.searchView.visibility = View.GONE
            false
        }

        binding.ivBack.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
