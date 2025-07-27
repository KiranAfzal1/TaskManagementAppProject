package com.example.mytask.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mytask.R
import com.example.mytask.data.Task

class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onCompletedChange: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_completed)
        private val titleText: TextView = itemView.findViewById(R.id.text_task_title)
        private val descriptionText: TextView = itemView.findViewById(R.id.text_task_description)
        private val categoryText: TextView = itemView.findViewById(R.id.text_category)
        private val dateText: TextView = itemView.findViewById(R.id.text_due_date)
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)
        private val completedLayout: LinearLayout = itemView.findViewById(R.id.layout_completed)
        private val priorityIndicator: View = itemView.findViewById(R.id.view_priority_indicator)

        fun bind(task: Task) {
            titleText.text = task.title
            descriptionText.text = task.description
            categoryText.text = task.category
            dateText.text = task.date

            descriptionText.visibility =
                if (task.description.isNotEmpty()) View.VISIBLE else View.GONE

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.completed

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val updatedTask = task.copy(completed = isChecked)
                onCompletedChange(updatedTask)
            }

            if (task.completed) {
                completedLayout.visibility = View.VISIBLE
                titleText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                titleText.paintFlags = titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                editButton.visibility = View.GONE
                deleteButton.visibility=View.GONE
            } else {
                completedLayout.visibility = View.GONE
                titleText.setTextColor(itemView.context.getColor(android.R.color.black))
                titleText.paintFlags = titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
                editButton.setOnClickListener { onEdit(task) }
                deleteButton.setOnClickListener { onDelete(task) }
            }

            when (task.priority) {
                "High" -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.red)
                "Medium" -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.yellow)
                "Low" -> priorityIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.green)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
        oldItem == newItem
}
