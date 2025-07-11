package com.example.taskmanager.domain.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.data.local.model.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final TaskClickListener listener;
    private List<TaskEntity> taskList = new ArrayList<>();

    public TaskAdapter(Context context, TaskClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<TaskEntity> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText, descriptionText, deadlineText, categoryText;
        private final CheckBox checkBox;
        private final LinearLayout taskCardLL;


        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskTitle);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            deadlineText = itemView.findViewById(R.id.taskDeadline);
            categoryText = itemView.findViewById(R.id.taskCategory);
            checkBox = itemView.findViewById(R.id.taskCheckbox);
            taskCardLL=itemView.findViewById(R.id.taskCardLL);
        }


        public void bind(TaskEntity task) {
            titleText.setText(task.getTitle());
            descriptionText.setText(task.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            deadlineText.setText("Due: " + sdf.format(task.getDeadline()));
            categoryText.setText(task.getCategory());

            checkBox.setOnCheckedChangeListener(null);

            checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskCardLL.setBackgroundColor(ContextCompat.getColor(context,R.color.gary_80));
                deadlineText.setTextColor(ContextCompat.getColor(context,R.color.white));
                categoryText.setBackgroundResource(R.drawable.catgory_chip_complete);
            } else {
                titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                taskCardLL.setBackgroundColor(ContextCompat.getColor(context,R.color.white));
                deadlineText.setTextColor(ContextCompat.getColor(context,R.color.gray_88));
                categoryText.setBackgroundResource(R.drawable.category_chip);


            }

            checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
                if (task.isCompleted() != isChecked) {
                    listener.onTaskChecked(task);


                }
            });

            // Other click handlers
            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            itemView.setOnLongClickListener(v -> {
                AlertDialog.Builder dialogueBuilder = getDialogueBuilder(task);
                dialogueBuilder.show();
                return true;

            });
        }

        private AlertDialog.Builder getDialogueBuilder(TaskEntity task) {
            AlertDialog.Builder dialogueBuilder=new AlertDialog.Builder(context);

            dialogueBuilder.setTitle("Confirm Delete");
            dialogueBuilder.setMessage("Are you sure you want to delete?");

            dialogueBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onTaskDelete(task);
                }
            });
            dialogueBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            return dialogueBuilder;
        }

    }

    public interface TaskClickListener {
        void onTaskClick(TaskEntity task);
        void onTaskChecked(TaskEntity task);
        void onTaskDelete(TaskEntity task);
    }
}
