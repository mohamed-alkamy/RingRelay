package com.example.ringrelaygui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

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

        // DEBUG LOG
        Log.d("DEBUG", "Toggling fragment: " + tag);

        View homeLayout = findViewById(R.id.homeLayout);

        if (existingFragment != null && existingFragment.isVisible()) {
            // DEBUG LOG
            Log.d("DEBUG", "Fragment is visible, removing it...");

            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);  // Temporary fade effect
            transaction.remove(existingFragment);
            homeLayout.setVisibility(View.VISIBLE);  // Show home screen again
        } else {
            // DEBUG LOG
            Log.d("DEBUG", "Fragment is not visible, adding it...");

            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);  // Temporary fade effect
            transaction.replace(R.id.fragment_container, fragment, tag);
            homeLayout.setVisibility(View.GONE);  // Hide home UI when switching
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
}
