package com.example.ringrelaygui;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AlarmEntity.class}, version = 2)  // ✅ Ensure version = 1 (or higher if upgrading)
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
                            ).fallbackToDestructiveMigration() // ✅ Deletes & recreates if the schema changes
                            .build();
                }
            }
        }
        return instance;
    }
}

