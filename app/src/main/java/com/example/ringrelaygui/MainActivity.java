package com.example.ringrelaygui;

import com.example.ringrelaygui.AlarmDatabase;
import com.example.ringrelaygui.AlarmDao;
import com.example.ringrelaygui.AlarmEntity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.Manifest;


import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private AlarmDatabase alarmDatabase;
    private ImageButton centralButton;
    private TextView mainTextDisplay, stepCountDisplay;
    private boolean isAlarmRinging = false;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBar;
    private Relay currentRelay;
    private Sensor stepDetectorSensor;
    private SensorEventListener stepListener;
    private boolean isStepListenerActive = false;


    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmDatabase = AlarmDatabase.getInstance(this);

        requestActivityRecognitionPermission();

        centralButton = findViewById(R.id.centralButton);
        mainTextDisplay = findViewById(R.id.mainTextDisplay);
        stepCountDisplay = findViewById(R.id.stepCountDisplay);
        progressBar = findViewById(R.id.progressBar);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetectorSensor != null) {
            stepListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                        if (currentRelay != null && currentRelay.isActive()) {
                            incrementSteps();
                        }
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Not needed for step detection
                }
            };
        }




        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(widgetReceiver, new IntentFilter("UPDATE_WIDGET_TEXT"));

        findViewById(R.id.statisticsButton).setOnClickListener(v -> toggleFragment(new StatisticsFragment(), "LEFT"));
        findViewById(R.id.alarmsButton).setOnClickListener(v -> toggleFragment(new AlarmsFragment(), "BOTTOM"));
        findViewById(R.id.settingsButton).setOnClickListener(v -> toggleFragment(new SettingsFragment(), "RIGHT"));

        centralButton.setOnClickListener(v -> {
            if (isAlarmRinging) {
                stopAlarmAndStartRelay();
                stepCountDisplay.setText("Steps: " + currentRelay.getCurrentSteps() + "/" + currentRelay.getStepGoal());





            } else if (currentRelay != null && currentRelay.isActive()) {
                incrementSteps();

            }
        });
        checkIfAlarmWasRinging();
    }

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1001;

    private void requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Only required for Android 10+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Activity Recognition permission granted!");
            } else {
                Log.e("Permissions", "Activity Recognition permission denied!");
            }
        }
    }



    private void toggleFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) {
            Log.e("FragmentError", "Attempted to commit a fragment transaction after the activity was destroyed.");
            return; // Prevent crashes
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        View homeLayout = findViewById(R.id.homeLayout);

        switch (tag) {
            case "LEFT":
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case "BOTTOM":
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
            case "RIGHT":
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                break;
        }

        if (existingFragment != null && existingFragment.isVisible()) {
            transaction.remove(existingFragment);
            homeLayout.setVisibility(View.VISIBLE);
        } else {
            transaction.replace(R.id.fragment_container, fragment, tag);
            homeLayout.setVisibility(View.GONE);
        }

        try {
            transaction.commit();
        } catch (IllegalStateException e) {
            Log.e("FragmentError", "Failed to commit fragment transaction: " + e.getMessage());
        }
    }


    private final BroadcastReceiver widgetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newText = intent.getStringExtra("newText");
            Log.d("AlarmDebug", "Broadcast received: " + newText);
            if (newText != null) {
                mainTextDisplay.setText(newText);
                isAlarmRinging = true; // Mark that alarm is ringing
                startNewRelay(); // Start a relay when the first snooze happens
            }
        }
    };

    private void checkIfAlarmWasRinging() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean wasRinging = prefs.getBoolean("isAlarmRinging", false);

        if (wasRinging) {
            Log.d("AlarmDebug", "Detected ringing alarm on app open, preparing relay.");

            // Reset the flag since we are handling it now
            prefs.edit().putBoolean("isAlarmRinging", false).apply();

            // Update UI and start relay
            mainTextDisplay.setText("Start Relay");
            isAlarmRinging = true;
            startNewRelay();
        }
    }

    private void stopAlarmAndStartRelay() {
        Log.d("AlarmDebug", "Stopping alarm and starting relay countdown");

        // Stop the alarm service
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopService(stopIntent);

        // Ensure the alarm stops
        AlarmService.stopAlarm();

        // Clear alarm flag
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("isAlarmRinging", false).apply();

        // Reset alarm state
        isAlarmRinging = false;
        progressBar.setVisibility(View.VISIBLE);

        // Start 5-minute countdown
        SharedPreferences sharedPreferences = getSharedPreferences("RelaySettings", MODE_PRIVATE);
        startRelayCountdown(sharedPreferences.getLong("relay_Length", 300000));
    }

    private void startNewRelay() {
        if (currentRelay == null || !currentRelay.isActive()) {
            SharedPreferences sharedPreferences = getSharedPreferences("RelaySettings", MODE_PRIVATE);
            currentRelay = new Relay(sharedPreferences.getInt("step_goal", 50), "07:00 AM");

            // Register the step sensor listener when relay starts
            if (stepDetectorSensor != null && !isStepListenerActive) {
                sensorManager.registerListener(stepListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
                isStepListenerActive = true;
            }
        }
    }


    private void startRelayCountdown(long length) {
        countDownTimer = new CountDownTimer(length, 1000) { // 5 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                mainTextDisplay.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (currentRelay != null && !currentRelay.isStepGoalMet()) {
                    Log.d("RelayDebug", "Step goal not met, restarting alarm");

                    mainTextDisplay.setText("RELAY FAILED!"); // Reset after timer finishes
                    restartAlarmAfterSnooze();
                    currentRelay.setCurrentSteps(0);
                    stepCountDisplay.setText("Steps: " + currentRelay.getCurrentSteps() + "/" + currentRelay.getStepGoal());
                } else {
                    completeRelay();
                }
            }
        }.start();
    }

    private void incrementSteps() {
        if (currentRelay != null && currentRelay.isActive()) {
            currentRelay.incrementSteps();
            stepCountDisplay.setText("Steps: " + currentRelay.getCurrentSteps() + "/" + currentRelay.getStepGoal());

            //need to make progress bar work universally

            progressBar.setProgress((progressBar.getProgress())+((100/currentRelay.getStepGoal())));
            if (currentRelay.isStepGoalMet()) {
                Log.d("RelayDebug", "Step goal met!");
                completeRelay();
            }
        }
    }

    private void restartAlarmAfterSnooze() {
        Log.d("AlarmDebug", "Restarting alarm after snooze");
        if (currentRelay != null) {
            currentRelay.snooze();

            // Start alarm sound immediately
            Intent serviceIntent = new Intent(this, AlarmService.class);
            startService(serviceIntent);

            // Create an intent for the AlarmReceiver
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set up the AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                long triggerTime = System.currentTimeMillis() + 1000; // Backup trigger after 1s
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    private void completeRelay() {
        if (currentRelay != null) {
            Log.d("RelayDebug", "Relay completed successfully!");
            currentRelay.completeRelay();

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            mainTextDisplay.setText("Relay Complete!");
            stepCountDisplay.setText("");
            progressBar.setVisibility(View.INVISIBLE);

            // Unregister step sensor when relay is done
            if (isStepListenerActive) {
                sensorManager.unregisterListener(stepListener);
                isStepListenerActive = false;
            }

            // Save relay stats
            String startTime = currentRelay.getFormattedStartTime();
            String endTime = currentRelay.getFormattedEndTime();
            String date = currentRelay.getFormattedDate();
            String stepCount = currentRelay.getCurrentSteps() + "/" + currentRelay.getStepGoal();

            Log.d("RelayDebug", "Saving Relay - Start: " + startTime + ", End: " + endTime);

            CompletedRelayEntity relay = new CompletedRelayEntity(startTime, endTime, date, stepCount);
            AsyncTask.execute(() -> alarmDatabase.alarmDao().insertCompletedRelay(relay));

            StatisticsFragment statisticsFragment = new StatisticsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, statisticsFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            currentRelay = null;
            mainTextDisplay.setText("RINGRELAY");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(widgetReceiver);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
