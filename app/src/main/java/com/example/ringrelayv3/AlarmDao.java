package com.example.RingRelayv3;

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

    @Insert
    void insertCompletedRelay(CompletedRelayEntity relay);

    @Query("SELECT * FROM completed_relays")
    List<CompletedRelayEntity> getAllCompletedRelays();

    @Query("SELECT * FROM alarms")
    List<AlarmEntity> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE time = :alarmTime LIMIT 1")
    AlarmEntity getAlarmByTime(String alarmTime);

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    List<AlarmEntity> getEnabledAlarms();


}
