package com.example.taskmanager.domain.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.domain.ui.addedit.AddEditTaskActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private MainViewModel viewModel;
    private FloatingActionButton fabAdd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Views
        recyclerView = findViewById(R.id.recyclerViewTasks);
        fabAdd = findViewById(R.id.fabAddTask);

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        taskAdapter = new TaskAdapter(this, new TaskAdapter.TaskClickListener() {
            @Override
            public void onTaskChecked(TaskEntity task) {
                viewModel.toggleTaskCompletion(task);
            }

            @Override
            public void onTaskDelete(TaskEntity task) {
                viewModel.deleteTask(task);
            }

            @Override
            public void onTaskClick(TaskEntity task) {
                // Open AddEditTaskActivity with task data
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                intent.putExtra("task_id", task.getId()); // You'd need to fetch it later in AddEdit
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(taskAdapter);

        // ViewModel & LiveData setup
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(MainViewModel.class);

        viewModel.getAllTasks().observe(this, taskAdapter::submitList);

        // FAB Add Button
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);

        });
    }
}