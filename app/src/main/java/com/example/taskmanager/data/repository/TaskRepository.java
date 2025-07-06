package com.example.taskmanager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskmanager.data.local.db.AppDatabase;
import com.example.taskmanager.data.local.db.TaskDao;
import com.example.taskmanager.data.local.model.TaskEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final Executor executor;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public void insert(TaskEntity task) {
        executor.execute(() -> taskDao.insert(task));
    }

    public void update(TaskEntity task) {
        executor.execute(() -> taskDao.update(task));
    }

    public void delete(TaskEntity task) {
        executor.execute(() -> taskDao.delete(task));
    }


    public LiveData<List<String>> getAllUniqueCategories() {
        return taskDao.getAllUniqueCategories();
    }

    public LiveData<TaskEntity> getTaskById(int taskId) {
        return taskDao.getTaskById(taskId);
    }

}
