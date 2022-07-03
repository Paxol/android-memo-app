package it.passolimirko.memorandum.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Date;
import java.util.List;

import it.passolimirko.memorandum.room.models.Memo;

@Dao
public interface MemoDao {
    @Insert
    void insertAll(Memo...memos);

    @Delete
    void delete(Memo memo);

    @Query("DELETE FROM memo")
    ListenableFuture<Integer> deleteAll();

    @Query("SELECT * FROM memo")
    LiveData<List<Memo>> getAll();

    @Query("SELECT * FROM memo WHERE status = 0 AND date >= strftime('%s', 'now') ORDER BY date ASC")
    LiveData<List<Memo>> getActive();

    @Query("SELECT * FROM memo WHERE status = 0 AND date < strftime('%s', 'now') ORDER BY date ASC")
    LiveData<List<Memo>> getExpired();

    @Query("SELECT * FROM memo WHERE status = 1 ORDER BY date DESC")
    LiveData<List<Memo>> getCompleted();
}
