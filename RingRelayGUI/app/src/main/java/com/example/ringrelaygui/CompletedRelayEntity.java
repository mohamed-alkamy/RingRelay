package com.example.ringrelaygui;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completed_relays")
public class CompletedRelayEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String startTime;
    public String endTime;
    public String date;

    public String stepCount;

    public CompletedRelayEntity(String startTime, String endTime, String date, String stepCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.stepCount = stepCount;
    }
}