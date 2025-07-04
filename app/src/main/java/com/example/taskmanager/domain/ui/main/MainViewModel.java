package com.example.taskmanager.domain.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.data.repository.TaskRepository;

import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> allTasks;

    public MainViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

//    public LiveData<List<TaskEntity>> getTasksByCategory(String category) {
//        return repository.getTasksByCategory(category);
//    }

//    public LiveData<List<TaskEntity>> getTasksDueBefore(Date date) {
//        return repository.getTasksDueBefore(date);
//    }

    public void deleteTask(TaskEntity task) {
        repository.delete(task);
    }

    public void toggleTaskCompletion(TaskEntity task) {
        task.setCompleted(!task.isCompleted());
        repository.update(task);
    }
}
