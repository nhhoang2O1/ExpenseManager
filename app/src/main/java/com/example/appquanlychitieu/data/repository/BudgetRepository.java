package com.example.appquanlychitieu.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.BudgetDao;
import com.example.appquanlychitieu.data.model.Budget;

import java.util.List;

public class BudgetRepository {
    private final BudgetDao budgetDao;

    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
    }

    public void insert(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.insert(budget));
    }

    public void update(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.update(budget));
    }

    public void delete(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.delete(budget));
    }

    public LiveData<List<Budget>> getBudgetsByMonth(long userId, String monthYear) {
        return budgetDao.getBudgetsByMonth(userId, monthYear);
    }

    public void deleteAllByUser(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.deleteAllByUser(userId));
    }
}
