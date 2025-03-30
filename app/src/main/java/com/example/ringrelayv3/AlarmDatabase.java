package com.example.RingRelayv3;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AlarmEntity.class, CompletedRelayEntity.class}, version = 4)  //version = 1 (or higher if upgrading)
public abstract class AlarmDatabase extends RoomDatabase {
    private static volatile AlarmDatabase instance;

    public abstract AlarmDao alarmDao();

    public static AlarmDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AlarmDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AlarmDatabase.class, "alarm_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}

