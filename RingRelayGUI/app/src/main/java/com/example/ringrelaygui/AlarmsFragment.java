package com.example.ringrelaygui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
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

    // Load alarms from database asynchronously
    private void loadSavedAlarms() {
        AsyncTask.execute(() -> {
            List<AlarmEntity> alarms = alarmDatabase.alarmDao().getAllAlarms();

            requireActivity().runOnUiThread(() -> {
                savedAlarms.clear();
                savedAlarms.addAll(alarms);  // No need to convert types anymore!
                adapter.notifyDataSetChanged();
            });
        });
    }




    // Save alarm to database
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
}
