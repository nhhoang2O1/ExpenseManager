package com.example.appquanlychitieu;

import android.app.Application;

import com.example.appquanlychitieu.data.database.AppDatabase;

public class ExpenseManagerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database (triggers prepopulate callback)
        AppDatabase.getDatabase(this);
    }
}
