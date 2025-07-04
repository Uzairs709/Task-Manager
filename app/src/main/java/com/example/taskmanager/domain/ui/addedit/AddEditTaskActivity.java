package com.example.taskmanager.domain.ui.addedit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.model.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput, categoryInput;
    private TextView deadlineText;
    private CheckBox completedCheck;
    private Button saveButton;

    private AddEditTaskViewModel viewModel;

    private Date selectedDeadline;
    private int editingTaskId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Bind Views
        titleInput = findViewById(R.id.editTextTitle);
        descriptionInput = findViewById(R.id.editTextDescription);
        categoryInput = findViewById(R.id.editTextCategory);
        deadlineText = findViewById(R.id.textViewDeadline);
        completedCheck = findViewById(R.id.checkBoxCompleted);
        saveButton = findViewById(R.id.buttonSave);

        viewModel = new ViewModelProvider(
                this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(AddEditTaskViewModel.class);

        // Check if we're editing
        Intent intent = getIntent();
        editingTaskId = intent.getIntExtra("task_id", -1);
        isEditMode = editingTaskId != -1;

        if (isEditMode) {
            completedCheck.setVisibility(CheckBox.VISIBLE);
            loadTaskForEdit(editingTaskId);
        }

        // Deadline picker
        deadlineText.setOnClickListener(view -> showDatePicker());

        // Save
        saveButton.setOnClickListener(view -> saveTask());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        if (selectedDeadline != null) {
            calendar.setTime(selectedDeadline);
        }

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDeadline = calendar.getTime();
                    updateDeadlineUI();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDeadlineUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        deadlineText.setText("Due: " + sdf.format(selectedDeadline));
    }

    private void loadTaskForEdit(int taskId) {
        viewModel.getTaskById(taskId).observe(this, task -> {
            if (task != null) {
                titleInput.setText(task.getTitle());
                descriptionInput.setText(task.getDescription());
                categoryInput.setText(task.getCategory());
                selectedDeadline = task.getDeadline();
                completedCheck.setChecked(task.isCompleted());
                updateDeadlineUI();
            }
        });
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }
        if (selectedDeadline == null) {
            Toast.makeText(this, "Please pick a deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            TaskEntity updatedTask = new TaskEntity(title, description, category, selectedDeadline, completedCheck.isChecked());
            updatedTask.setId(editingTaskId);
            viewModel.updateTask(updatedTask);
        } else {
            viewModel.insertTask(title, description, category, selectedDeadline, false);
        }

        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish(); // Go back to main screen
    }
}