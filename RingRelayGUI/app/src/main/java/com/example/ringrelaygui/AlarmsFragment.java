package com.example.ringrelaygui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AlarmsFragment extends Fragment {
    private Spinner hourSpinner, minuteSpinner, amPmSpinner;
    private Button setAlarmButton;
    private RecyclerView savedAlarmsRecyclerView;
    private ArrayList<AlarmItem> savedAlarms = new ArrayList<>();
    private AlarmAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        // Initialize UI elements
        hourSpinner = view.findViewById(R.id.hourSpinner);
        minuteSpinner = view.findViewById(R.id.minuteSpinner);
        amPmSpinner = view.findViewById(R.id.amPmSpinner);
        setAlarmButton = view.findViewById(R.id.setAlarmButton);
        savedAlarmsRecyclerView = view.findViewById(R.id.savedAlarmsRecyclerView);

        // Setup Spinners
        setupSpinners();

        // Setup RecyclerView
        savedAlarmsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlarmAdapter(savedAlarms);
        savedAlarmsRecyclerView.setAdapter(adapter);

        // Set Alarm Button Click
        setAlarmButton.setOnClickListener(v -> {
            String alarmTime = getSelectedTime();
            savedAlarms.add(new AlarmItem(alarmTime, true)); // Default to enabled
            adapter.notifyItemInserted(savedAlarms.size() - 1);
            Toast.makeText(getActivity(), "Alarm Set: " + alarmTime, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getHours());
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getMinutes());
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        ArrayAdapter<String> amPmAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"AM", "PM"});
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amPmSpinner.setAdapter(amPmAdapter);
    }

    private String[] getHours() {
        String[] hours = new String[12];
        for (int i = 0; i < 12; i++) {
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

    private String getSelectedTime() {
        return hourSpinner.getSelectedItem().toString() + ":" +
                minuteSpinner.getSelectedItem().toString() + " " +
                amPmSpinner.getSelectedItem().toString();
    }
}
