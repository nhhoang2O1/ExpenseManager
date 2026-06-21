package com.example.appquanlychitieu.ui.goals;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.model.Goal;
import com.example.appquanlychitieu.data.model.GoalHistory;
import com.example.appquanlychitieu.data.repository.GoalRepository;
import com.example.appquanlychitieu.util.SessionManager;

import java.util.List;

public class GoalViewModel extends AndroidViewModel {
    private final GoalRepository repository;
    private final long userId;
    private final LiveData<List<Goal>> goals;

    public GoalViewModel(@NonNull Application application) {
        super(application);
        repository = new GoalRepository(application);
        
        SessionManager session = new SessionManager(application);
        userId = session.getUserId();

        goals = repository.getGoalsByUser(userId);
    }

    public LiveData<List<Goal>> getGoals() {
        return goals;
    }

    public void insertGoal(Goal goal) {
        repository.insert(goal);
    }

    public void updateGoal(Goal goal) {
        repository.update(goal);
    }

    public void deleteGoal(Goal goal) {
        repository.delete(goal);
    }

    public LiveData<List<GoalHistory>> getHistoryForGoal(long goalId) {
        return repository.getHistoryByGoalId(goalId);
    }

    public void insertGoalHistory(GoalHistory history) {
        repository.insertHistory(history);
    }

    public long getUserId() {
        return userId;
    }
}
