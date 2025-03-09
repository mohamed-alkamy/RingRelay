package com.example.ringrelaygui;

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

    private boolean isStatisticsVisible = false;
    private boolean isAlarmsVisible = false;
    private boolean isSettingsVisible = false;
    private Button centralButton;
    private TextView mainTextDisplay;
    private boolean isAlarmRinging = false;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        centralButton = findViewById(R.id.centralButton);
        mainTextDisplay = findViewById(R.id.mainTextDisplay);

        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(widgetReceiver, new IntentFilter("UPDATE_WIDGET_TEXT"));

        findViewById(R.id.statisticsButton).setOnClickListener(v -> toggleFragment(new StatisticsFragment(), "LEFT"));
        findViewById(R.id.alarmsButton).setOnClickListener(v -> toggleFragment(new AlarmsFragment(), "BOTTOM"));
        findViewById(R.id.settingsButton).setOnClickListener(v -> toggleFragment(new SettingsFragment(), "RIGHT"));

        centralButton.setOnClickListener(v -> {
            if (isAlarmRinging) {
                stopAlarmAndStartRelay();
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


    private void startRelayCountdown() {
        countDownTimer = new CountDownTimer(300000, 1000) { // 5 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                mainTextDisplay.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                mainTextDisplay.setText("Start Relay!"); // Reset after timer finishes
                //restartAlarmAfterSnooze(this);
            }
        }.start();
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
