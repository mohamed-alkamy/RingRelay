package com.example.ringrelaygui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmDebug", "AlarmReceiver triggered!");

        // Start the AlarmService to play the sound
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.startService(serviceIntent);

        // Update widget text
        Intent updateIntent = new Intent("UPDATE_WIDGET_TEXT");
        updateIntent.putExtra("newText", "Start Relay");
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);

        Log.d("AlarmDebug", "Broadcast sent: Start Relay");

    }
}
