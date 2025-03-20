package com.example.basicalarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmDebug", "AlarmReceiver triggered!");

        // Retrieve the saved ringtone from SharedPreferences
        String ringtonePath = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
                .getString("alarm_ringtone", null);
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            if (ringtonePath != null) {
                // Play user-selected ringtone
                Uri ringtoneUri = Uri.parse(ringtonePath);
                mediaPlayer.setDataSource(context, ringtoneUri);
                mediaPlayer.prepare();
            } else {
                // Play default alarm sound from res/raw
                mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);
            }
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("AlarmDebug", "Error playing alarm sound: " + e.getMessage());
        }

        // Show Toast
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show();
    }
}
