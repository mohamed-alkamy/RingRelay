package com.example.RingRelayv3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private AlarmDatabase alarmDatabase;
    private ImageButton centralButton;
    private TextView mainTextDisplay, stepCountDisplay;
    private boolean isAlarmRinging = false;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBar;
    private Relay currentRelay;
    private SensorManager sensorManager;
    private Sensor stepDetector;
    private int currentSteps = 0;
    private int stepGoal = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmDatabase = AlarmDatabase.getInstance(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        centralButton = findViewById(R.id.centralButton);
        mainTextDisplay = findViewById(R.id.mainTextDisplay);
        stepCountDisplay = findViewById(R.id.stepCountDisplay);
        progressBar = findViewById(R.id.progressBar);

        LocalBroadcastManager.getInstance(this).registerReceiver(widgetReceiver, 
            new IntentFilter("UPDATE_WIDGET_TEXT"));

        findViewById(R.id.statisticsButton).setOnClickListener(v -> 
            toggleFragment(new StatisticsFragment(), "LEFT"));
        findViewById(R.id.alarmsButton).setOnClickListener(v -> 
            toggleFragment(new AlarmsFragment(), "BOTTOM"));
        findViewById(R.id.settingsButton).setOnClickListener(v -> 
            toggleFragment(new SettingsFragment(), "RIGHT"));

        centralButton.setOnClickListener(v -> {
            if (isAlarmRinging) {
                stopAlarmAndStartRelay();
                stepCountDisplay.setText("Steps: " + currentSteps + "/" + stepGoal);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        stepGoal = prefs.getInt("current_step_goal", 50);
        checkIfAlarmWasRinging();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR && currentRelay != null && currentRelay.isActive()) {
            currentSteps++;
            runOnUiThread(() -> {
                currentRelay.setCurrentSteps(currentSteps);
                stepCountDisplay.setText("Steps: " + currentSteps + "/" + stepGoal);
                progressBar.setProgress((int) (((float) currentSteps / stepGoal) * 100));
                
                if (currentSteps >= stepGoal) {
                    completeRelay();
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void toggleFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        View homeLayout = findViewById(R.id.homeLayout);

        switch (tag) {
            case "LEFT": transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left); break;
            case "BOTTOM": transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom); break;
            case "RIGHT": transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right); break;
        }

        if (existingFragment != null && existingFragment.isVisible()) {
            transaction.remove(existingFragment);
            homeLayout.setVisibility(View.VISIBLE);
        } else {
            transaction.replace(R.id.fragment_container, fragment, tag);
            homeLayout.setVisibility(View.GONE);
        }

        try { transaction.commit(); } 
        catch (IllegalStateException e) { Log.e("FragmentError", e.getMessage()); }
    }

    private final BroadcastReceiver widgetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newText = intent.getStringExtra("newText");
            if (newText != null) {
                mainTextDisplay.setText(newText);
                isAlarmRinging = true;
                startNewRelay();
                currentSteps = 0;
            }
        }
    };

    private void checkIfAlarmWasRinging() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean wasRinging = prefs.getBoolean("isAlarmRinging", false);

        if (wasRinging) {
            prefs.edit().putBoolean("isAlarmRinging", false).apply();
            mainTextDisplay.setText("Start Relay");
            isAlarmRinging = true;
            startNewRelay();
            currentSteps = 0;
        }
    }

    private void stopAlarmAndStartRelay() {
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopService(stopIntent);
        AlarmService.stopAlarm();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("isAlarmRinging", false).apply();

        isAlarmRinging = false;
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("RelaySettings", MODE_PRIVATE);
        startRelayCountdown(sharedPreferences.getLong("relay_Length", 300000));
    }

    private void startNewRelay() {
        if (currentRelay == null || !currentRelay.isActive()) {
            SharedPreferences sharedPreferences = getSharedPreferences("RelaySettings", MODE_PRIVATE);
            currentRelay = new Relay(stepGoal, "07:00 AM");
        }
    }

    private void startRelayCountdown(long length) {
        countDownTimer = new CountDownTimer(length, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                mainTextDisplay.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (currentRelay != null && !currentRelay.isStepGoalMet()) {
                    mainTextDisplay.setText("RELAY FAILED!");
                    restartAlarmAfterSnooze();
                    currentRelay.setCurrentSteps(0);
                    stepCountDisplay.setText("Steps: " + currentRelay.getCurrentSteps() + "/" + stepGoal);
                    currentSteps = 0;
                } else {
                    completeRelay();
                }
            }
        }.start();
    }

    private void restartAlarmAfterSnooze() {
        if (currentRelay != null) {
            currentRelay.snooze();
            Intent serviceIntent = new Intent(this, AlarmService.class);
            startService(serviceIntent);

            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                long triggerTime = System.currentTimeMillis() + 1000;
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    private void completeRelay() {
        if (currentRelay != null) {
            currentRelay.completeRelay();
            if (countDownTimer != null) countDownTimer.cancel();
            
            mainTextDisplay.setText("Relay Complete!");
            stepCountDisplay.setText("");
            progressBar.setVisibility(View.INVISIBLE);

            CompletedRelayEntity relay = new CompletedRelayEntity(
                currentRelay.getFormattedStartTime(),
                currentRelay.getFormattedEndTime(),
                currentRelay.getFormattedDate(),
                currentSteps + "/" + stepGoal
            );

            AsyncTask.execute(() -> alarmDatabase.alarmDao().insertCompletedRelay(relay));

            StatisticsFragment statisticsFragment = new StatisticsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, statisticsFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            currentRelay = null;
            currentSteps = 0;
            mainTextDisplay.setText("RINGRELAY");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(widgetReceiver);
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
