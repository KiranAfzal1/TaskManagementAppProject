package com.example.mytask

import android.content.Intent
import android.widget.TextView
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mytask.adapter.TaskAdapter
import com.example.mytask.data.TaskDatabase
import com.example.mytask.repository.TaskRepository
import com.example.mytask.viewmodel.TaskViewModel
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.os.Build
import android.content.res.ColorStateList
import android.view.View
import com.example.mytask.viewmodel.TaskViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ViewTask : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TaskViewModel
    private lateinit var subtitleText: TextView

    private lateinit var buttonAll: Button
    private lateinit var buttonWork: Button
    private lateinit var buttonPersonal: Button
    private lateinit var buttonStudy: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_task)

        window.statusBarColor = ContextCompat.getColor(this, R.color.light_green_bg)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    )
        }

        subtitleText = findViewById(R.id.subtitleText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val addButton = findViewById<FloatingActionButton>(R.id.add_button)

        buttonAll = findViewById(R.id.button_all)
        buttonWork = findViewById(R.id.button_work)
        buttonPersonal = findViewById(R.id.button_personal)
        buttonStudy = findViewById(R.id.button_study)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val dao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        taskAdapter = TaskAdapter(
            onEdit = { task ->
                val intent = Intent(this, AddEditTaskActivity::class.java)
                intent.putExtra("task_id", task.id)
                startActivity(intent)
            },
            onDelete = { task -> viewModel.delete(task) },
            onCompletedChange = { task ->
                viewModel.update(task)
                updateList(getCurrentCategory())
            }
        )

        recyclerView.adapter = taskAdapter

        buttonAll.setOnClickListener {
            setActiveButton(buttonAll)
            updateList(null)
        }
        buttonWork.setOnClickListener {
            setActiveButton(buttonWork)
            updateList("Work")
        }
        buttonPersonal.setOnClickListener {
            setActiveButton(buttonPersonal)
            updateList("Personal")
        }
        buttonStudy.setOnClickListener {
            setActiveButton(buttonStudy)
            updateList("Study")
        }

        addButton.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java))
        }

        setActiveButton(buttonAll)
        updateList(null)
    }

    private fun updateList(category: String?) {
        val liveData = when (category) {
            "Work", "Personal", "Study" -> viewModel.getTasksByCategory(category)
            else -> viewModel.allTasks
        }
        liveData.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
            subtitleText.text = "${tasks.size} tasks  ${tasks.count { it.completed }} completed"
        }
    }

    private fun resetButtonColors() {
        val normalColor = Color.parseColor("#1AFFFFFF")
        val textColor = ContextCompat.getColor(this, R.color.black)
        listOf(buttonAll, buttonWork, buttonPersonal, buttonStudy).forEach {
            it.setBackgroundTintList(ColorStateList.valueOf(normalColor))
            it.setTextColor(textColor)
        }
    }

    private fun setActiveButton(button: Button) {
        resetButtonColors()
        val green = ContextCompat.getColor(this, R.color.task_card_bg)
        button.setBackgroundTintList(ColorStateList.valueOf(green))
        button.setTextColor(ContextCompat.getColor(this, R.color.task_category_text))
    }

    private fun getCurrentCategory(): String? {
        return when {
            buttonWork.currentTextColor == ContextCompat.getColor(this, R.color.task_category_text) -> "Work"
            buttonPersonal.currentTextColor == ContextCompat.getColor(this, R.color.task_category_text) -> "Personal"
            buttonStudy.currentTextColor == ContextCompat.getColor(this, R.color.task_category_text) -> "Study"
            else -> null
        }
    }
}
