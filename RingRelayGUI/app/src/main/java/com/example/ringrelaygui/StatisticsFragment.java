package com.example.ringrelaygui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private RecyclerView recyclerView;
    private AlarmDatabase alarmDatabase;
    private AlarmAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        alarmDatabase = AlarmDatabase.getInstance(getContext());
        AsyncTask.execute(() -> {
            List<CompletedRelayEntity> relays = alarmDatabase.alarmDao().getAllCompletedRelays();
            ArrayList<CompletedRelayEntity> relayArrayList = new ArrayList<>(relays);
            getActivity().runOnUiThread(() -> {
                RelayHistoryAdapter adapter = new RelayHistoryAdapter(getContext(), relayArrayList);
                recyclerView.setAdapter(adapter);
            });
        });

        return view;
    }
}