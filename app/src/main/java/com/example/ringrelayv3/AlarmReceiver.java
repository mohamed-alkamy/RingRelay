package com.example.RingRelayv3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getStringExtra("alarmTime");
        Log.d("AlarmReceiver", "Alarm triggered for: " + alarmTime);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("isAlarmRinging", true).apply();

        new Thread(() -> {
            AlarmDatabase db = AlarmDatabase.getInstance(context);
            AlarmEntity alarm = db.alarmDao().getAlarmByTime(alarmTime);

            if (alarm == null || !alarm.isEnabled()) {
                Log.d("AlarmReceiver", "Alarm no longer exists or is disabled. Skipping.");
                return;
            }

            prefs.edit().putInt("current_step_goal", alarm.getStepGoal()).apply();

            new Handler(Looper.getMainLooper()).post(() -> {
                Intent serviceIntent = new Intent(context, AlarmService.class);
                serviceIntent.putExtra("ringtone_uri", alarm.getRingtoneUri());
                context.startService(serviceIntent);

                Intent updateIntent = new Intent("UPDATE_WIDGET_TEXT");
                updateIntent.putExtra("newText", "Start Relay");
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
            });
        }).start();
    }
}
