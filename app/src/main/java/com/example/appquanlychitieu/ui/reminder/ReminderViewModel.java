package com.example.appquanlychitieu.ui.reminder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.model.Reminder;
import com.example.appquanlychitieu.data.repository.ReminderRepository;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {
    private final ReminderRepository repository;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository(application);
    }

    public LiveData<List<Reminder>> getReminders(long userId) {
        return repository.getRemindersByUser(userId);
    }

    public void insert(Reminder reminder) {
        repository.insert(reminder);
    }

    public void update(Reminder reminder) {
        repository.update(reminder);
    }

    public void delete(Reminder reminder) {
        repository.delete(reminder);
    }
}
