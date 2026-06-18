package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY dayOfMonth ASC, hour ASC, minute ASC")
    LiveData<List<Reminder>> getRemindersByUser(long userId);

    @Query("SELECT * FROM reminders WHERE isActive = 1")
    List<Reminder> getActiveReminders();
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(long id);
}
