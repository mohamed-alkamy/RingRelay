package com.example.ringrelaygui;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    void insert(AlarmEntity alarm);

    @Delete
    void delete(AlarmEntity alarm);

    @Update
    void update(AlarmEntity alarm);

    @Query("SELECT * FROM alarms")
    List<AlarmEntity> getAllAlarms();
}
