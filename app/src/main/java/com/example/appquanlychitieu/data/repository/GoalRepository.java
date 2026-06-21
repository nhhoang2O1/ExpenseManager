package com.example.appquanlychitieu.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.GoalDao;
import com.example.appquanlychitieu.data.database.dao.GoalHistoryDao;
import com.example.appquanlychitieu.data.model.Goal;
import com.example.appquanlychitieu.data.model.GoalHistory;

import java.util.List;

public class GoalRepository {
    private final GoalDao goalDao;
    private final GoalHistoryDao goalHistoryDao;

    public GoalRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        goalDao = db.goalDao();
        goalHistoryDao = db.goalHistoryDao();
    }

    public void insert(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.insert(goal));
    }

    public void update(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.update(goal));
    }

    public void delete(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.delete(goal));
    }

    public LiveData<List<Goal>> getGoalsByUser(long userId) {
        return goalDao.getGoalsByUser(userId);
    }

    public LiveData<List<GoalHistory>> getHistoryByGoalId(long goalId) {
        return goalHistoryDao.getHistoryByGoalId(goalId);
    }

    public void insertHistory(GoalHistory history) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalHistoryDao.insert(history));
    }

    public void deleteAllByUser(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.deleteAllByUser(userId));
    }
}
