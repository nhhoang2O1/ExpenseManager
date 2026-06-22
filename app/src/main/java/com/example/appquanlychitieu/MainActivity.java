package com.example.appquanlychitieu;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.ui.transaction.AddEditTransactionActivity;
import com.example.appquanlychitieu.ui.auth.LoginActivity;
import com.example.appquanlychitieu.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNav;

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
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditTransactionActivity.class)));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destinationId = destination.getId();
                if (destinationId == R.id.navigation_transactions
                        || destinationId == R.id.navigation_statistics) {
                    fabAddTransaction.show();
                } else {
                    fabAddTransaction.hide();
                }
            });
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
