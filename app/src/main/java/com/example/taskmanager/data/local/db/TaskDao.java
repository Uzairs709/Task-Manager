package com.example.taskmanager.data.local.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskmanager.data.local.model.TaskEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    LiveData<List<TaskEntity>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY deadline ASC")
    LiveData<List<TaskEntity>> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE deadline <= :date ORDER BY deadline ASC")
    LiveData<List<TaskEntity>> getTasksDueBefore(Date date);
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    LiveData<TaskEntity> getTaskById(int taskId);

}
