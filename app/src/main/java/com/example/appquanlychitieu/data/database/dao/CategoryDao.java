package com.example.appquanlychitieu.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.TransactionType;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Insert
    void insertAll(List<Category> categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories ORDER BY type, name")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    LiveData<List<Category>> getCategoriesByType(TransactionType type);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    List<Category> getCategoriesByTypeSync(TransactionType type);

    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(long id);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}
