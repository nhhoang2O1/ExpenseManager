package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND monthYear = :monthYear")
    LiveData<List<Budget>> getBudgetsByMonth(long userId, String monthYear);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear AND userId = :userId")
    Budget getBudgetByCategoryAndMonth(long categoryId, String monthYear, long userId);

    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(long id);

    @Query("DELETE FROM budgets WHERE userId = :userId")
    void deleteAllByUser(long userId);

    @Query("DELETE FROM budgets")
    void deleteAll();
}
