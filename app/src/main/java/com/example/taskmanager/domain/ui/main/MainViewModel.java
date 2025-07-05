package com.example.taskmanager.domain.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.data.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> allTasks;
    private final MutableLiveData<List<TaskEntity>> filteredTasks = new MutableLiveData<>();
    private List<TaskEntity> allTasksList = new ArrayList<>();

    public MainViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<TaskEntity>> getFilteredTasks() {
        return filteredTasks;
    }

    public void setAllTasks(List<TaskEntity> tasks) {
        this.allTasksList = tasks;
        filteredTasks.setValue(tasks);
    }

    public void filterTasks(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredTasks.setValue(allTasksList);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        List<TaskEntity> result = new ArrayList<>();

        for (TaskEntity task : allTasksList) {
            if (
                    (task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                            (task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                            (task.getCategory() != null && task.getCategory().toLowerCase().contains(lowerCaseQuery)) ||
                            (task.getDeadline() != null && new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(task.getDeadline()).toLowerCase().contains(lowerCaseQuery))
            ) {
                result.add(task);
            }
        }

        filteredTasks.setValue(result);
    }

    public void deleteTask(TaskEntity task) {
        repository.delete(task);
    }

    public void toggleTaskCompletion(TaskEntity task) {
        task.setCompleted(!task.isCompleted());
        repository.update(task);
    }
}
