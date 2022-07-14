package it.passolimirko.memorandum.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Objects;

import it.passolimirko.memorandum.room.models.Memo;

@Dao
public interface MemoDao {
    @Insert
    ListenableFuture<Long[]> insertAll(Memo ...memo);

    @Delete
    ListenableFuture<Integer> delete(Memo memo);

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

    @Query("UPDATE memo SET location=:location WHERE id = :id")
    ListenableFuture<Integer> updateLocation(Integer id, String location);
}
