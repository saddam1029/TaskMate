package com.example.taskmanager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.TaskItemsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CompletedTaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onRevertClick: (Task) -> Unit
) : ListAdapter<Task, CompletedTaskAdapter.TaskViewHolder>(TaskDiffCallback()), Filterable {

    private var taskList: List<Task> = listOf()
    private var filteredList: List<Task> = listOf()

    override fun submitList(list: List<Task>?) {
        super.submitList(list)
        taskList = list ?: listOf()
        filteredList = taskList // Set the filtered list initially as the full list
    }

    inner class TaskViewHolder(private val binding: TaskItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskName.text = task.title
            binding.tvDate.text = task.date

            // Compare task date and time with the current date and time
            val dateFormat = SimpleDateFormat("EEE, dd, MMM yyyy hh:mm a", Locale.getDefault())
            val taskDateTime = dateFormat.parse("${task.date} ${task.time}")
            val currentDateTime = Calendar.getInstance().time

            // If the task date and time match the current date and time, change the text color to red
            if (taskDateTime != null) {
                if (taskDateTime.before(currentDateTime) || taskDateTime.equals(currentDateTime)) {
                    binding.tvDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
                } else {
                    binding.tvDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.light_app_color)) // Reset to default color
                }
            }


            binding.ivMenu.setOnClickListener {
                showBottomSheetDialog(binding.root, task)
            }
        }


        @SuppressLint("InflateParams")
        private fun showBottomSheetDialog(view: View, task: Task) {
            val bottomSheetDialog = BottomSheetDialog(view.context)
            val bottomSheetView = LayoutInflater.from(view.context)
                .inflate(R.layout.bottom_dialog_task_finish_layout, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            bottomSheetView.findViewById<LinearLayout>(R.id.layoutNotFinishTask).setOnClickListener {
                onRevertClick(task)
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
        val binding = TaskItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    results.values = taskList
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    results.values = taskList.filter { it.title.lowercase().contains(filterPattern) }
                }
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<Task>
                notifyDataSetChanged()
            }
        }
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
