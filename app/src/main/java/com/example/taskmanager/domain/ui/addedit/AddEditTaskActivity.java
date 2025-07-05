package com.example.taskmanager.domain.ui.addedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.domain.ui.notifications.ReminderReceiver;

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
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker(Calendar calendar) {
        new android.app.TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    selectedDeadline = calendar.getTime(); // ✅ Final Date
                    updateDeadlineUI();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void updateDeadlineUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
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

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleNotification(TaskEntity task) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", task.getTitle());
        intent.putExtra("description", task.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long deadlineTime = task.getDeadline().getTime();
        long oneHourBefore = deadlineTime - 60 * 60 * 1000; // 1 hour in milliseconds
        long currentTime = System.currentTimeMillis();

        // If 1 hour before is in the future, use that, otherwise use the exact deadline
        long notificationTime = (oneHourBefore > currentTime) ? oneHourBefore : deadlineTime;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
            );
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String category = categoryInput.getText().toString().toLowerCase().trim();


        TaskEntity task=new TaskEntity(title,description,category,selectedDeadline,false);
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }
        if (selectedDeadline == null) {
            Toast.makeText(this, "Please pick a deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            task.setCompleted(completedCheck.isChecked());
            task.setId(editingTaskId);
            viewModel.updateTask(task);
        } else {
            viewModel.insertTask(task);
        }
        scheduleNotification(task);
        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish(); // Go back to main screen
    }



}