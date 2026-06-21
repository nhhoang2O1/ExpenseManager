package com.example.appquanlychitieu.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.ReminderDao;
import com.example.appquanlychitieu.data.model.Reminder;

import java.util.List;

public class ReminderRepository {
    private final ReminderDao reminderDao;

    public ReminderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
    }

    public LiveData<List<Reminder>> getRemindersByUser(long userId) {
        return reminderDao.getRemindersByUser(userId);
    }

    public long insertSync(Reminder reminder) {
        return reminderDao.insert(reminder);
    }

    public void insert(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = reminderDao.insert(reminder);
            reminder.setId(id);
        });
    }

    public void update(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> reminderDao.update(reminder));
    }

    public void delete(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> reminderDao.delete(reminder));
    }

    public void deleteAllByUser(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> reminderDao.deleteAllByUser(userId));
    }
}
