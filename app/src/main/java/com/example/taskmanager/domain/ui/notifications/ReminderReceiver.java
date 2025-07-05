package com.example.taskmanager.domain.ui.notifications;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.example.taskmanager.domain.ui.utils.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        NotificationHelper.showNotification(context, title, description);
        Log.d("Notification Happened","hehe");
    }
}
