package com.example.appquanlychitieu.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.TransactionDao;
import com.example.appquanlychitieu.data.model.CategorySpent;
import com.example.appquanlychitieu.data.model.CategorySummary;
import com.example.appquanlychitieu.data.model.MonthlySummary;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;

import java.util.List;

public class TransactionRepository {
    private final TransactionDao transactionDao;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
    }

    public void insert(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.insert(transaction));
    }

    public void update(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.delete(transaction));
    }

    public LiveData<List<Transaction>> getAllTransactions(long userId) {
        return transactionDao.getAllTransactions(userId);
    }

    public LiveData<List<Transaction>> getTransactionsByDateRange(long userId, long startDate, long endDate) {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate);
    }

    public LiveData<List<Transaction>> getTransactionsByType(long userId, TransactionType type) {
        return transactionDao.getTransactionsByType(userId, type);
    }

    public LiveData<List<Transaction>> getTransactionsByTypeAndDateRange(long userId, TransactionType type, long startDate, long endDate) {
        return transactionDao.getTransactionsByTypeAndDateRange(userId, type, startDate, endDate);
    }

    public LiveData<Double> getTotalByTypeAndDateRange(long userId, TransactionType type, long startDate, long endDate) {
        return transactionDao.getTotalByTypeAndDateRange(userId, type, startDate, endDate);
    }

    public LiveData<List<CategorySummary>> getCategorySummary(long userId, TransactionType type, long startDate, long endDate) {
        return transactionDao.getCategorySummary(userId, type, startDate, endDate);
    }

    public LiveData<List<Transaction>> getRecentTransactions(long userId, long startDate, long endDate, int limit) {
        return transactionDao.getRecentTransactions(userId, startDate, endDate, limit);
    }

    public LiveData<Double> getSpentByCategory(long userId, long categoryId, long startDate, long endDate) {
        return transactionDao.getSpentByCategory(userId, categoryId, startDate, endDate);
    }

    public LiveData<List<CategorySpent>> getSpentPerCategory(long userId, long startDate, long endDate) {
        return transactionDao.getSpentPerCategory(userId, startDate, endDate);
    }

    public LiveData<List<MonthlySummary>> getMonthlySummary(long userId) {
        return transactionDao.getMonthlySummary(userId);
    }

    public void deleteAllByUser(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> transactionDao.deleteAllByUser(userId));
    }
}
