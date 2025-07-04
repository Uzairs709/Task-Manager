package com.example.taskmanager.domain.ui.addedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.data.repository.TaskRepository;

import java.util.Date;

public class AddEditTaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;

    public AddEditTaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
    }

    // Save new task
    public void insertTask(String title, String description, String category, Date deadline, boolean isCompleted) {
        TaskEntity newTask = new TaskEntity(title, description, category, deadline, isCompleted);
        repository.insert(newTask);
    }

    // Update existing task
    public void updateTask(TaskEntity task) {
        repository.update(task);
    }

    // Load existing task for editing
    public LiveData<TaskEntity> getTaskById(int taskId) {
        return repository.getTaskById(taskId);
    }
}