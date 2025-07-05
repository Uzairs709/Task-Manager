package com.example.taskmanager.domain.ui.main;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.model.TaskEntity;
import com.example.taskmanager.domain.ui.addedit.AddEditTaskActivity;
import com.example.taskmanager.domain.ui.utils.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private MainViewModel viewModel;
    private FloatingActionButton fabAdd;
    private SearchView searchBar;
    private Spinner categorySpinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestExactAlarmPermissionIfNeeded();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        // Init Views
        recyclerView = findViewById(R.id.recyclerViewTasks);
        fabAdd = findViewById(R.id.fabAddTask);
        searchBar = findViewById(R.id.searchBar);
        categorySpinner = findViewById(R.id.spinnerCategoryFilter);

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBar.setIconified(false);
                searchBar.requestFocusFromTouch();

            }
        });


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

        viewModel.getAllTasks().observe(this, tasks -> {
            viewModel.setAllTasks(tasks); // cache all tasks internally
        });

        viewModel.getFilteredTasks().observe(this, filteredTasks -> {
            taskAdapter.submitList(filteredTasks);
        });
        // FAB Add Button
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);

        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.filterTasks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.filterTasks(newText);
                return true;
            }
        });

        viewModel.getAllUniqueCategories().observe(this, categories -> {
            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add("All Categories"); // Default
            spinnerItems.addAll(categories);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, spinnerItems
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();

                if (selectedCategory.equals("All Categories")) {
                    viewModel.getAllTasks(); // Your existing method to fetch all
                } else {
                    viewModel.filterTasks(selectedCategory); // Your existing filter function
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                viewModel.getAllTasks();
            }
        });


        NotificationHelper.createNotificationChannel(this);

    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
}