package com.example.ringrelaygui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class SettingsFragment extends Fragment {
    private Spinner minuteSpinner, secondSpinner, stepGoalSpinner;
    private Button updateSettings;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        secondSpinner = view.findViewById(R.id.secondSpinner);
        minuteSpinner = view.findViewById(R.id.minuteSpinner);
        stepGoalSpinner = view.findViewById(R.id.stepGoalSpinner);
        updateSettings = view.findViewById(R.id.updateSettings);

        setupSpinners();
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    private void setupSpinners() {
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMinutes());
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        ArrayAdapter<String> secondAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getSeconds());
        secondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondSpinner.setAdapter(secondAdapter);

        ArrayAdapter<String> amPmAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"AM", "PM"});
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stepGoalSpinner.setAdapter(amPmAdapter);
    }

    private String[] getSeconds() {
        String[] hours = new String[60];
        for (int i = 0; i < 60; i++) {
            hours[i] = String.valueOf(i + 1);
        }
        return hours;
    }

    private String[] getMinutes() {
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
        return minutes;
    }

    private String[] getStepGoal() {
        String[] minutes = new String[50];
        for (int i = 5; i < 250; i+=5) {
            minutes[i] = String.format("%02d", i);
        }
        return minutes;
    }
}



