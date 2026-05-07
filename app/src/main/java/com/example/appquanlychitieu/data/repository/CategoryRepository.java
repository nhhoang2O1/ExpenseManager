package com.example.appquanlychitieu.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.database.dao.CategoryDao;
import com.example.appquanlychitieu.data.model.Category;
import com.example.appquanlychitieu.data.model.TransactionType;

import java.util.List;

public class CategoryRepository {
    private final CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryDao = db.categoryDao();
    }

    public void insert(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.insert(category));
    }

    public void update(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.update(category));
    }

    public void delete(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.delete(category));
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(TransactionType type) {
        return categoryDao.getCategoriesByType(type);
    }
}
