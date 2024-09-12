package com.example.taskmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.HighTaskItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class HighPriorityTaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onUpdateClick: (Task) -> Unit) :
    ListAdapter<Task, HighPriorityTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(private val binding: HighTaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskName.text = task.title
            binding.tvDate.text = task.date
            binding.tvPriority.text = task.priority

            // Compare task date and time with the current date and time
            val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy hh:mm a", Locale.getDefault())
            val taskDateTime = dateFormat.parse("${task.date} ${task.time}")
            val currentDateTime = Calendar.getInstance().time

            // If the task date and time match the current date and time, change the text color to red
            if (taskDateTime != null && taskDateTime.before(currentDateTime) || taskDateTime.equals(currentDateTime)) {
                binding.tvDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
            } else {
                binding.tvDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.light_app_color)) // Reset to default color
            }

            binding.ivMenu.setOnClickListener {
                showBottomSheetDialog(binding.root, task)
            }

            binding.root.setOnClickListener {
                onItemClick(task)
            }
        }

        private fun showBottomSheetDialog(view: View, task: Task) {
            val bottomSheetDialog = BottomSheetDialog(view.context)
            val bottomSheetView = LayoutInflater.from(view.context).inflate(R.layout.bottom_dialog_home_layout, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            bottomSheetView.findViewById<LinearLayout>(R.id.layoutFinishTask).setOnClickListener {
                task.status = "Complete"
                onUpdateClick(task)  // Update the task to reflect the "Complete" status
                bottomSheetDialog.dismiss()
            }

            bottomSheetView.findViewById<LinearLayout>(R.id.layoutDeleteTask).setOnClickListener {
                showConfirmationDialog(view, task)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun showConfirmationDialog(view: View, task: Task) {
            val dialogView = LayoutInflater.from(view.context).inflate(R.layout.dialog_delete_task, null)

            val alertDialog = AlertDialog.Builder(view.context, R.style.CustomAlertDialog) // Optional: use custom style
                .setView(dialogView)
                .create()

            val deleteConfirmButton = dialogView.findViewById<Button>(R.id.btnDeleteConfirm)
            val cancelButton = dialogView.findViewById<Button>(R.id.btnDeleteCancel)

            // Set click listeners for buttons
            deleteConfirmButton.setOnClickListener {
                onDeleteClick(task)
                alertDialog.dismiss()
            }

            cancelButton.setOnClickListener {
                alertDialog.dismiss()  // Dismiss dialog when "No" is clicked
            }

            alertDialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = HighTaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}

