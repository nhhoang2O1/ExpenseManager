package com.example.appquanlychitieu.ui.reminder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.ReminderDao;
import com.example.appquanlychitieu.data.model.Reminder;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {
    private final ReminderDao reminderDao;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
    }

    public LiveData<List<Reminder>> getReminders(long userId) {
        return reminderDao.getRemindersByUser(userId);
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
}
