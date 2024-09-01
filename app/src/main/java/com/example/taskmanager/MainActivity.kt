package com.example.taskmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        window.statusBarColor = ContextCompat.getColor(this, R.color.app_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.app_background)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

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
            },
            { task ->
                taskViewModel.update(task)
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
            },
            { task ->
                taskViewModel.update(task)
            }
        )
        binding.rvHigh.adapter = highPriorityAdapter
        binding.rvHigh.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Observe incomplete tasks
        taskViewModel.incompleteTasks.observe(this) { tasks ->
            tasks?.let {
                // Filter tasks based on priority
                val highPriorityTasks = it.filter { task -> task.priority == "High" }
                val normalPriorityTasks = it.filter { task -> task.priority != "High" }

                highPriorityAdapter.submitList(highPriorityTasks)
                taskAdapter.submitList(normalPriorityTasks)
            }
        }

        binding.ivAccountPic.setOnClickListener {
            moveToTaskComplete()
        }

        binding.btAddTasks.setOnClickListener {
            moveToTaskCreate()
        }

        binding.ivMenu.setOnClickListener { view ->
            showCustomMenu(view)
        }

        createNotificationChannel()


    }

    companion object {
        const val CHANNEL_ID = "task_notification_channel"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notification"
            val descriptionText = "Channel for Task notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showCustomMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val customMenuView = inflater.inflate(R.layout.custom_menu, null)
        val popupWindow = PopupWindow(
            customMenuView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background drawable to null to apply custom background
        popupWindow.setBackgroundDrawable(null)

        customMenuView.findViewById<TextView>(R.id.tvCompleteTask).setOnClickListener {
            val intent = Intent(this, TaskComplete::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            popupWindow.dismiss()
        }

        customMenuView.findViewById<TextView>(R.id.tvFollow).setOnClickListener {
            // Handle Follow Us click
            popupWindow.dismiss()
            // Add your action here
        }

        customMenuView.findViewById<TextView>(R.id.tvInvite).setOnClickListener {
            // Handle Invite Friend click
            popupWindow.dismiss()
            // Add your action here
        }

        customMenuView.findViewById<TextView>(R.id.tvSetting).setOnClickListener {
            // Handle Setting click
            popupWindow.dismiss()
            // Add your action here
        }

        popupWindow.showAsDropDown(anchor, 0, 0)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (isTaskRoot) {
            // This is the root activity, you can show a confirmation dialog or do something else
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Close the app
                    finish()
                }
                .setNegativeButton("No") { dialog, id ->
                    // If the user cancels, dismiss the dialog and stay in the activity
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        } else {
            // If there is a previous activity in the stack, navigate back to it
            super.onBackPressed()
        }
    }

    private fun moveToTaskCreate() {
        val intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun moveToTaskComplete() {
        val intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}

