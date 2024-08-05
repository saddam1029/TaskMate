package com.example.taskmanager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.databinding.ActivityCreateTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        task = intent.getParcelableExtra("task")
        task?.let {
            binding.etTitle.setText(it.title)
            binding.tvDescription.setText(it.description)
            binding.tvDate.setText(it.date)
            binding.tvTime.setText(it.time)
            when (it.priority) {
                "High" -> binding.rbBusiness.isChecked = true
                "Medium" -> binding.rbCommunication.isChecked = true
                "Low" -> binding.rbCreativity.isChecked = true
            }
        }

        binding.cvDate.setOnClickListener {
            showDatePicker()
        }

        binding.cvTime.setOnClickListener {
            showTimePicker()
        }

        binding.btAddTasks.setOnClickListener {
            saveTask()
        }

        // Set click listener to navigate back to Home activity
        binding.ivBack.setOnClickListener {
            navigateToHome()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = formatDate(selectedDay, selectedMonth, selectedYear)
                binding.tvDate.text = date
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
        val dateFormat = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedMinute = String.format("%02d", selectedMinute)
                val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val time = "$formattedHour:$formattedMinute"
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                binding.tvTime.text = time
                binding.tvTimeState.text = amPm
            },
            hour, minute, false
        )
        timePickerDialog.show()
    }


    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.tvDescription.text.toString().trim()
        val date = binding.tvDate.text.toString().trim()
        val time = binding.tvTime.text.toString().trim()

        val selectedPriorityId = binding.radioGroupPriority.checkedRadioButtonId
        val priority = when (selectedPriorityId) {
            R.id.rbBusiness -> "High"
            R.id.rbCommunication -> "Medium"
            R.id.rbCreativity -> "Low"
            else -> ""
        }

        if (title.isNotEmpty() && description.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && priority.isNotEmpty()) {
            val updatedTask = task?.copy(
                title = title,
                description = description,
                date = date,
                time = time,
                priority = priority
            ) ?: Task(title = title, description = description, date = date, time = time, priority = priority)

            if (task != null) {
                taskViewModel.update(updatedTask)
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                taskViewModel.insert(updatedTask)
                Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show()
            }

            finish()
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed(){
        super.onBackPressed()

        navigateToHome()
    }

    private fun navigateToHome(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
