package com.example.ringrelaygui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isStatisticsVisible = false;
    private boolean isAlarmsVisible = false;
    private boolean isSettingsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.statisticsButton).setOnClickListener(v -> toggleFragment(new StatisticsFragment(), "LEFT"));
        findViewById(R.id.alarmsButton).setOnClickListener(v -> toggleFragment(new AlarmsFragment(), "BOTTOM"));
        findViewById(R.id.settingsButton).setOnClickListener(v -> toggleFragment(new SettingsFragment(), "RIGHT"));
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
            // If the fragment is already visible, remove it and show the home UI again
            transaction.remove(existingFragment);
            homeLayout.setVisibility(View.VISIBLE); // Show home screen
        } else {
            // Otherwise, replace with the new fragment and hide the home screen UI
            transaction.replace(R.id.fragment_container, fragment, tag);
            homeLayout.setVisibility(View.GONE); // Hide home screen
        }

        transaction.commit();
    }



    private void handleVisibility(boolean isVisible, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (isVisible) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    // Helper function to convert "HH:MM AM/PM" to total minutes
    private int convertToMinutes(String time) {
        String[] parts = time.split(" ");
        String[] hm = parts[0].split(":");
        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);
        boolean isPM = parts[1].equals("PM");

        if (hour == 12) hour = 0; // 12 AM -> 0, 12 PM -> 12
        if (isPM) hour += 12;

        return hour * 60 + minute;
    }


}
