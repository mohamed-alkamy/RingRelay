package com.example.RingRelayv3;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class AlarmService extends Service {
    private static MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            String ringtoneUri = intent.getStringExtra("ringtone_uri");
            
            try {
                if (ringtoneUri != null && !ringtoneUri.equals("default")) {
                    mediaPlayer = MediaPlayer.create(this, Uri.parse(ringtoneUri));
                } else {
                    mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                Log.d("AlarmDebug", "Alarm started in AlarmService");
            } catch (Exception e) {
                Log.e("AlarmService", "Error playing ringtone", e);
                mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }

    public static void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("AlarmDebug", "Alarm stopped");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
