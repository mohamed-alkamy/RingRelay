package com.example.ringrelaygui;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String time;
    public boolean isEnabled;
    public String startTime;
    public String endTime;
    public AlarmEntity(String time, boolean isEnabled,String startTime, String endTime) {
        this.time = time;
        this.isEnabled = isEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }


}


