package it.passolimirko.memorandum.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.passolimirko.memorandum.room.converters.DateConverter;
import it.passolimirko.memorandum.room.daos.MemoDao;
import it.passolimirko.memorandum.room.models.Memo;

@Database(entities = {
        Memo.class
}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "memo-db").build();
        }

        return instance;
    }

    public abstract MemoDao memoDao();
}
