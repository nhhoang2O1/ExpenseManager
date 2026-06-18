package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.Goal;

import java.util.List;

@Dao
public interface GoalDao {
    @Insert
    long insert(Goal goal);

    @Update
    void update(Goal goal);

    @Delete
    void delete(Goal goal);

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<Goal>> getGoalsByUser(long userId);

    @Query("SELECT * FROM goals WHERE id = :id")
    Goal getGoalById(long id);
}
