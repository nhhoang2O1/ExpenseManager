package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.appquanlychitieu.data.model.GoalHistory;

import java.util.List;

@Dao
public interface GoalHistoryDao {
    @Insert
    void insert(GoalHistory history);

    @Query("SELECT * FROM goal_history WHERE goalId = :goalId ORDER BY date DESC")
    LiveData<List<GoalHistory>> getHistoryByGoalId(long goalId);
    
    @Query("DELETE FROM goal_history WHERE goalId = :goalId")
    void deleteByGoalId(long goalId);
}
