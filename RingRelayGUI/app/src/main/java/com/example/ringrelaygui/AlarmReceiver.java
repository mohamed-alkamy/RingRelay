package com.example.ringrelaygui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmDebug", "AlarmReceiver triggered!");

        // Play alarm sound
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            Log.e("AlarmDebug", "MediaPlayer is NULL");
        }

        // Show Toast
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();
    }
}
