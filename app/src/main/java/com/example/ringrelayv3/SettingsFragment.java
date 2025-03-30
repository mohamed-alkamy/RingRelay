package com.example.RingRelayv3;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class SettingsFragment extends Fragment {
    private Spinner minuteSpinner, secondSpinner, stepGoalSpinner;
    private Button updateSettings;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        secondSpinner = view.findViewById(R.id.secondSpinner);
        minuteSpinner = view.findViewById(R.id.minuteSpinner);
        stepGoalSpinner = view.findViewById(R.id.stepGoalSpinner);
        updateSettings = view.findViewById(R.id.updateSettings);

        sharedPreferences = requireContext().getSharedPreferences("RelaySettings", requireContext().MODE_PRIVATE);

        setupSpinners();
        loadSavedPreferences();

        updateSettings.setOnClickListener(v -> savePreferences());

        return view; // Fixed return statement
    }

    private void setupSpinners() {
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMinutes());
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        ArrayAdapter<String> secondAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getSeconds());
        secondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondSpinner.setAdapter(secondAdapter);

        ArrayAdapter<String> stepGoalAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getStepGoals());
        stepGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stepGoalSpinner.setAdapter(stepGoalAdapter);
    }

    private void loadSavedPreferences() {
        int savedMinutes = sharedPreferences.getInt("relay_minutes", 5);
        int savedSeconds = sharedPreferences.getInt("relay_seconds", 0);
        int savedStepGoal = sharedPreferences.getInt("step_goal", 50);

        minuteSpinner.setSelection(getIndexFromArray(getMinutes(), String.valueOf(savedMinutes)));
        secondSpinner.setSelection(getIndexFromArray(getSeconds(), String.valueOf(savedSeconds)));
        stepGoalSpinner.setSelection(getIndexFromArray(getStepGoals(), String.valueOf(savedStepGoal)));
    }

    private void savePreferences() {
        int selectedMinutes = Integer.parseInt(minuteSpinner.getSelectedItem().toString());
        int selectedSeconds = Integer.parseInt(secondSpinner.getSelectedItem().toString());
        int selectedStepGoal = Integer.parseInt(stepGoalSpinner.getSelectedItem().toString());
        long relayLength = getRelayDurationInMillis();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("relay_Length", relayLength);
        editor.putInt("step_goal", selectedStepGoal);
        editor.apply();

        Toast.makeText(requireContext(), "Relay settings updated!", Toast.LENGTH_SHORT).show();
    }

    private int getIndexFromArray(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private String[] getSeconds() {
        String[] seconds = new String[60];
        for (int i = 0; i < 60; i++) {
            seconds[i] = String.valueOf(i);
        }
        return seconds;
    }

    private String[] getMinutes() {
        String[] minutes = new String[10];
        for (int i = 0; i < 10; i++) {
            minutes[i] = String.valueOf(i);
        }
        return minutes;
    }

    private String[] getStepGoals() {
        String[] stepGoals = new String[50];
        for (int i = 0, step = 5; i < 50; i++, step += 5) {
            stepGoals[i] = String.valueOf(step);
        }
        return stepGoals;
    }

    private long getRelayDurationInMillis() {
        int minutes = Integer.parseInt(minuteSpinner.getSelectedItem().toString());
        int seconds = Integer.parseInt(secondSpinner.getSelectedItem().toString());

        return (minutes * 60 + seconds) * 1000L; // Convert to milliseconds
    }

}
