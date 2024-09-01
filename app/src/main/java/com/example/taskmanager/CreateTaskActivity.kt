package com.example.taskmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.databinding.ActivityCreateTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private var task: Task? = null

    companion object {
        private const val REQUEST_CODE_PICK_SOUND = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        window.statusBarColor = ContextCompat.getColor(this, R.color.app_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.app_background)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true


        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        // Initialize with current date and time
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        binding.tvDate.text = currentDate
        binding.tvTime.text = currentTime.first
        binding.tvTimeState.text = currentTime.second

        task = intent.getParcelableExtra("task")
        task?.let {
            binding.etTitle.setText(it.title)
            binding.tvDescription.setText(it.description)
            binding.tvDate.text = it.date
            binding.tvTime.text = it.time
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

        binding.tvSelect.setOnClickListener {
            openSoundPicker()
        }

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, R.style.CustomDatePicker,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = formatDate(selectedDay, selectedMonth, selectedYear)
                binding.tvDate.text = date

                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }

                val isPastDate = selectedCalendar.before(Calendar.getInstance())
                val isToday = selectedCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        selectedCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        selectedCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)

                when {
                    isToday -> {
                        // Current day - set the color to default (e.g., light blue)
                        binding.tvDate.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                        binding.tvTime.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                        binding.tvTimeState.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                    }

                    isPastDate -> {
                        // Past date - set the color to red
                        binding.tvDate.setTextColor(ContextCompat.getColor(this, R.color.red))
                        binding.tvTime.setTextColor(ContextCompat.getColor(this, R.color.red))
                        binding.tvTimeState.setTextColor(ContextCompat.getColor(this, R.color.red))
                    }

                    else -> {
                        // Future date - set the color to default (e.g., light blue)
                        binding.tvDate.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                        binding.tvTime.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                        binding.tvTimeState.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.light_blue
                            )
                        )
                    }
                }
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
        val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, R.style.CustomTimePicker,
            { _, selectedHour, selectedMinute ->
                val formattedMinute = String.format("%02d", selectedMinute)
                val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val time = "$formattedHour:$formattedMinute $amPm"
                binding.tvTime.text = time
                binding.tvTimeState.text = amPm

                checkTimeValidity()
            },
            hour, minute, false
        )
        timePickerDialog.show()
    }

    private fun checkTimeValidity() {
        val selectedTime = binding.tvTime.text.toString()
        val selectedDate = binding.tvDate.text.toString()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy hh:mm a", Locale.getDefault())

        val currentDateTime = dateFormat.format(calendar.time)
        val selectedDateTime = "$selectedDate $selectedTime"

        val current = dateFormat.parse(currentDateTime)
        val selected = dateFormat.parse(selectedDateTime)

        if (selected != null && selected.before(current)) {
            binding.tvTime.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.tvTimeState.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            binding.tvTime.setTextColor(ContextCompat.getColor(this, R.color.light_blue))
            binding.tvTimeState.setTextColor(ContextCompat.getColor(this, R.color.light_blue))
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    @SuppressLint("DefaultLocale")
    private fun getCurrentTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val formattedMinute = String.format("%02d", minute)
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12
        val amPm = if (hour >= 12) "PM" else "AM"
        return Pair("$formattedHour:$formattedMinute", amPm)
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.tvDescription.text.toString().trim()
        val date = binding.tvDate.text.toString().trim()
        val time = binding.tvTime.text.toString().trim()

        // Check if the date or time fields are red
        val isDateRed = binding.tvDate.currentTextColor == ContextCompat.getColor(this, R.color.red)
        val isTimeRed = binding.tvTime.currentTextColor == ContextCompat.getColor(this, R.color.red)

        if (isDateRed || isTimeRed) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPriorityId = binding.radioGroupPriority.checkedRadioButtonId
        val priority = when (selectedPriorityId) {
            R.id.rbBusiness -> "High"
            R.id.rbCommunication -> "Medium"
            R.id.rbCreativity -> "Low"
            else -> ""
        }

        if (title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && priority.isNotEmpty()) {
            val updatedTask = task?.copy(
                title = title,
                description = description,
                date = date,
                time = time, // Ensure the time includes the AM/PM suffix
                priority = priority
            ) ?: Task(
                title = title,
                description = description,
                date = date,
                time = time,
                priority = priority
            )

            // Check if sbAlarm is on before setting the alarm
            if (binding.sbAlarm.isChecked) {
                // Set the alarm if SwitchButton is on
                setAlarm(updatedTask)
            }

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

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(task: Task) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
            putExtra("alarmSoundUri", task.alarmSoundUri) // Pass the URI string here
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Format the date and time string into a Date object
        val dateTime = "${task.date} ${task.time}"
        val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy hh:mm a", Locale.getDefault())
        val date = dateFormat.parse(dateTime)

        date?.let {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, it.time, pendingIntent)
        }
    }


    // Inside CreateTaskActivity or your relevant class
    private fun openSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
            task?.alarmSoundUri?.let { Uri.parse(it) })

        startActivityForResult(intent, REQUEST_CODE_PICK_SOUND)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_SOUND && resultCode == RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                task = task?.copy(alarmSoundUri = uri.toString())
            }
        }
    }


    private fun saveSoundUriToDatabase(uriString: String) {
        val task = task ?: return // Ensure task is not null
        val updatedTask = task.copy(alarmSoundUri = uriString)
        taskViewModel.update(updatedTask) // Update the task in the database
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()

        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}


