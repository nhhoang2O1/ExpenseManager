package com.example.appquanlychitieu.ui.goals;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.GoalDao;
import com.example.appquanlychitieu.data.model.Goal;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.List;

public class GoalViewModel extends AndroidViewModel {
    private final GoalDao goalDao;
    private final AppDatabase db;
    private final long userId;
    private final LiveData<List<Goal>> goals;

    public GoalViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        goalDao = db.goalDao();
        
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        goals = goalDao.getGoalsByUser(userId);
    }

    public LiveData<List<Goal>> getGoals() {
        return goals;
    }

    public void insertGoal(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.insert(goal));
    }

    public void updateGoal(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> goalDao.update(goal));
    }

    public void deleteGoal(Goal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            goalDao.delete(goal);
            // Delete history is handled by CASCADE in the entity, but if it doesn't work, we can manually delete it here.
        });
    }

    public LiveData<List<com.example.appquanlychitieu.data.model.GoalHistory>> getHistoryForGoal(long goalId) {
        return db.goalHistoryDao().getHistoryByGoalId(goalId);
    }

    public void insertGoalHistory(com.example.appquanlychitieu.data.model.GoalHistory history) {
        AppDatabase.databaseWriteExecutor.execute(() -> db.goalHistoryDao().insert(history));
    }

    public long getUserId() {
        return userId;
    }
}
