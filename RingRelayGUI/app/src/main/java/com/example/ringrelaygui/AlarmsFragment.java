package com.example.ringrelaygui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlarmsFragment extends Fragment {
    private Spinner hourSpinner, minuteSpinner, amPmSpinner;
    private Button setAlarmButton;
    private ListView savedAlarmsList;
    private ArrayList<AlarmEntity> savedAlarms = new ArrayList<>();
    private AlarmAdapter adapter;
    private AlarmDatabase alarmDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        // Initialize UI elements
        hourSpinner = view.findViewById(R.id.hourSpinner);
        minuteSpinner = view.findViewById(R.id.minuteSpinner);
        amPmSpinner = view.findViewById(R.id.amPmSpinner);
        setAlarmButton = view.findViewById(R.id.setAlarmButton);
        savedAlarmsList = view.findViewById(R.id.savedAlarmsList);

        // Initialize Room Database
        alarmDatabase = AlarmDatabase.getInstance(requireContext());

        adapter = new AlarmAdapter(requireContext(), savedAlarms);
        savedAlarmsList.setAdapter(adapter);

        // Setup Spinners
        setupSpinners();

        // Load saved alarms from the database
        loadSavedAlarms();

        // Set Alarm Button Click
        setAlarmButton.setOnClickListener(v -> {
            String alarmTime = getSelectedTime();
            AlarmEntity newAlarm = new AlarmEntity(alarmTime, true);
            saveAlarm(newAlarm);
        });

        return view;
    }

    // Setup dropdowns
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
    // Load alarms from database asynchronously

    private void saveAlarm(AlarmEntity alarm) {
        AsyncTask.execute(() -> {
            alarmDatabase.alarmDao().insert(alarm);
            requireActivity().runOnUiThread(() -> {
                savedAlarms.add(alarm);
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Alarm Saved: " + alarm.getTime(), Toast.LENGTH_SHORT).show();
            });
        });
    }
    public void removeAlarmFromList(int position) {
        requireActivity().runOnUiThread(() -> {
            savedAlarms.remove(position);
            adapter.notifyDataSetChanged();
        });
    }
    private void loadSavedAlarms() {
        AsyncTask.execute(() -> {
            List<AlarmEntity> alarms = alarmDatabase.alarmDao().getAllAlarms();
            for (AlarmEntity alarm : alarms) {
                Log.d("AlarmDebug", "Loaded alarm: " + alarm.getTime());
            }

            // Sort manually
            Collections.sort(alarms, (a, b) -> convertTo24Hour(a.getTime()) - convertTo24Hour(b.getTime()));

            requireActivity().runOnUiThread(() -> {
                savedAlarms.clear();
                savedAlarms.addAll(alarms);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private int convertTo24Hour(String time) {
        String[] parts = time.split(" "); // Split into "hh:mm" and "AM/PM"
        String[] hm = parts[0].split(":"); // Split "hh:mm" into "hh" and "mm"

        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);
        boolean isPM = parts[1].equalsIgnoreCase("PM");

        // Convert to 24-hour format
        if (isPM && hour != 12) {
            hour += 12; // Convert PM hours (except 12 PM) to 24-hour format
        } else if (!isPM && hour == 12) {
            hour = 0; // Midnight case: "12 AM" should become "00"
        }

        return (hour * 60) + minute; // Convert to total minutes since 00:00
    }

    public void toggleAlarm(AlarmEntity alarm, boolean isEnabled) {
        if (isEnabled) {
            scheduleSystemAlarm(alarm);
        } else {
            cancelSystemAlarm(alarm);
        }

        // Update database entry
        AsyncTask.execute(() -> {
            alarm.setEnabled(isEnabled);
            alarmDatabase.alarmDao().update(alarm);
        });
    }

    private void scheduleSystemAlarm(AlarmEntity alarm) {
        return;
    }
    private void cancelSystemAlarm(AlarmEntity alarm) {
        return;
    }



    // Save alarm to database




}
