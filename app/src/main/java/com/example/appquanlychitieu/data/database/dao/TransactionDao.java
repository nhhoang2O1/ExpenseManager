package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.CategorySummary;
import com.example.appquanlychitieu.data.model.Transaction;
import com.example.appquanlychitieu.data.model.TransactionType;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions(long userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(long userId, long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(long userId, TransactionType type);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByTypeAndDateRange(long userId, TransactionType type, long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(long userId, long categoryId);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(long id);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalByTypeAndDateRange(long userId, TransactionType type, long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    double getTotalByTypeAndDateRangeSync(long userId, TransactionType type, long startDate, long endDate);

    @Query("SELECT c.id as categoryId, c.name as categoryName, c.color as categoryColor, c.icon as categoryIcon, " +
            "SUM(t.amount) as totalAmount, COUNT(t.id) as transactionCount " +
            "FROM transactions t INNER JOIN categories c ON t.categoryId = c.id " +
            "WHERE t.userId = :userId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id ORDER BY totalAmount DESC")
    LiveData<List<CategorySummary>> getCategorySummary(long userId, TransactionType type, long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(long userId, long startDate, long endDate, int limit);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE' AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getSpentByCategory(long userId, long categoryId, long startDate, long endDate);

    // Lấy tổng chi tiêu theo từng categoryId trong một lần query (tránh tạo N observers)
    @Query("SELECT categoryId, COALESCE(SUM(amount), 0) as spent FROM transactions " +
            "WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY categoryId")
    LiveData<List<com.example.appquanlychitieu.data.model.CategorySpent>> getSpentPerCategory(long userId, long startDate, long endDate);

    // Lấy tổng thu/chi theo từng tháng — dùng cho lịch sử tài chính
    @Query("SELECT " +
            "strftime('%Y-%m', datetime(date/1000, 'unixepoch')) as monthYear, " +
            "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) as totalExpense " +
            "FROM transactions WHERE userId = :userId " +
            "GROUP BY monthYear ORDER BY monthYear DESC")
    LiveData<List<com.example.appquanlychitieu.data.model.MonthlySummary>> getMonthlySummary(long userId);

    @Query("DELETE FROM transactions WHERE userId = :userId")
    void deleteAllByUser(long userId);

    @Query("DELETE FROM transactions")
    void deleteAll();
}
