package com.example.mytask
import android.os.Build
import android.graphics.Color
import android.view.View
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mytask.data.Task
import com.example.mytask.data.TaskDatabase
import com.example.mytask.repository.TaskRepository
import com.example.mytask.viewmodel.TaskViewModel
import com.example.mytask.viewmodel.TaskViewModelFactory
import java.util.*

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private var selectedPriority: String = "Low"
    private var selectedCategory: String = "All"
    private var selectedDate: String = ""
    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_task_activity)
        setContentView(R.layout.add_edit_task_activity)

        window.statusBarColor = ContextCompat.getColor(this, R.color.light_green_bg)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    )
        }

        else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val dao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val titleInput = findViewById<EditText>(R.id.edit_text_title)
        val descriptionInput = findViewById<EditText>(R.id.edit_text_description)
        val dateBtn = findViewById<Button>(R.id.button_select_date)

        val priorityLow = findViewById<Button>(R.id.button_priority_low)
        val priorityMedium = findViewById<Button>(R.id.button_priority_medium)
        val priorityHigh = findViewById<Button>(R.id.button_priority_high)

        val categoryWork = findViewById<Button>(R.id.category_work)
        val categoryPersonal = findViewById<Button>(R.id.category_personal)
        val categoryStudy = findViewById<Button>(R.id.category_study)

        val cancelBtn = findViewById<Button>(R.id.button_cancel)
        val saveBtn = findViewById<Button>(R.id.button_save)
        val goBack = findViewById<ImageView>(R.id.back_arrow)
        val titleText = findViewById<TextView>(R.id.titleText)

        taskId = intent.getIntExtra("task_id", -1)

        if (taskId != -1) {
            titleText.text = "Update Task"
        } else {
            titleText.text = "Add New Task"
        }

        goBack.setOnClickListener {
            startActivity(Intent(this, ViewTask::class.java))
            finish()
        }

        dateBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = "${month + 1}/$day/$year"
                    dateBtn.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        priorityLow.setOnClickListener { selectedPriority = "Low" }
        priorityMedium.setOnClickListener { selectedPriority = "Medium" }
        priorityHigh.setOnClickListener { selectedPriority = "High" }

        categoryWork.setOnClickListener { selectedCategory = "Work" }
        categoryPersonal.setOnClickListener { selectedCategory = "Personal" }
        categoryStudy.setOnClickListener { selectedCategory = "Study" }

        cancelBtn.setOnClickListener { finish() }

        if (taskId != -1) {
            viewModel.allTasks.observe(this) { taskList ->
                val task = taskList.find { it.id == taskId }
                task?.let {
                    titleInput.setText(it.title)
                    descriptionInput.setText(it.description)
                    selectedDate = it.date
                    dateBtn.text = selectedDate
                    selectedPriority = it.priority
                    selectedCategory = it.category
                }
            }
        }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (taskId != -1) {
                val updatedTask = Task(
                    id = taskId,
                    title = title,
                    description = description,
                    date = selectedDate,
                    completed = false,
                    priority = selectedPriority,
                    category = selectedCategory
                )
                viewModel.update(updatedTask)
                Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show()
            } else {
                val newTask = Task(
                    title = title,
                    description = description,
                    date = selectedDate,
                    completed = false,
                    priority = selectedPriority,
                    category = selectedCategory
                )
                viewModel.insert(newTask)
                Toast.makeText(this, "Task Saved", Toast.LENGTH_SHORT).show()
            }

            startActivity(Intent(this, ViewTask::class.java))
            finish()
        }
    }
}
