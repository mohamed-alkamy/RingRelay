package com.example.ringrelaygui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int stepGoal = 50;

    private boolean isStatisticsVisible = false;
    private boolean isAlarmsVisible = false;
    private boolean isSettingsVisible = false;
    private Button centralButton;
    private TextView mainTextDisplay, stepCountDisplay;
    private boolean isAlarmRinging = false;
    private CountDownTimer countDownTimer;

    private Relay currentRelay; // Track the active relay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        centralButton = findViewById(R.id.centralButton);
        mainTextDisplay = findViewById(R.id.mainTextDisplay);
        stepCountDisplay = findViewById(R.id.stepCountDisplay);

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
    }

    private void toggleFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Get reference to the home layout
        View homeLayout = findViewById(R.id.homeLayout);

        // Set animations based on the direction
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
            homeLayout.setVisibility(View.VISIBLE); // Show home screen
        } else {
            transaction.replace(R.id.fragment_container, fragment, tag);
            homeLayout.setVisibility(View.GONE); // Hide home screen
        }

        transaction.commit();
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

    private void stopAlarmAndStartRelay() {
        Log.d("AlarmDebug", "Stopping alarm and starting relay countdown");

        // Stop the alarm service
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopService(stopIntent);

        // Ensure the alarm stops
        AlarmService.stopAlarm();

        // Reset alarm state
        isAlarmRinging = false;

        // Start 5-minute countdown
        startRelayCountdown();
    }

    private void startNewRelay() {
        if (currentRelay == null || !currentRelay.isActive()) {
            currentRelay = new Relay(stepGoal, "07:00 AM"); // Example step goal and time
        }
    }

    private void startRelayCountdown() {
        countDownTimer = new CountDownTimer(30000, 1000) { // 5 minutes
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
            onDestroy();
            mainTextDisplay.setText("Relay Complete!");
            stepCountDisplay.setText("");
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
