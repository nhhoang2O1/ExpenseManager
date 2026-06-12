package com.example.appquanlychitieu;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.ui.auth.LoginActivity;
import com.example.appquanlychitieu.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        validateSessionAndSetup();
    }

    private void validateSessionAndSetup() {
        AppDatabase db = AppDatabase.getDatabase(this);
        long userId = sessionManager.getUserId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean validSession = userId > 0 && db.userDao().getUserById(userId) != null;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (validSession) {
                    setupMainContent();
                } else {
                    sessionManager.logout();
                    navigateToLogin();
                }
            });
        });
    }

    private void setupMainContent() {
        setContentView(R.layout.activity_main);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
