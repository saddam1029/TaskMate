package com.example.taskmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.TaskItemsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(private val binding: TaskItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskName.text = task.title
            binding.tvDate.text = task.date

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
                bottomSheetDialog.dismiss()
            }

            bottomSheetView.findViewById<LinearLayout>(R.id.layoutDeleteTask).setOnClickListener {
                showConfirmationDialog(view,task)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun showConfirmationDialog(view: View, task: Task) {
            val alertDialog = AlertDialog.Builder(view.context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes") { dialog, _ ->
                    onDeleteClick(task)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
