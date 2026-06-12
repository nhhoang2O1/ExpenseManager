package com.example.appquanlychitieu.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.ui.auth.LoginActivity;
import com.example.appquanlychitieu.util.SessionManager;
import com.example.appquanlychitieu.util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        View cardReset = view.findViewById(R.id.card_reset_data);
        View cardLogout = view.findViewById(R.id.card_logout);
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        // Dark mode
        switchDarkMode.setChecked(ThemeManager.isDarkMode(requireContext()));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(requireContext(), isChecked);
        });

        // Reset data
        cardReset.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.reset_data)
                    .setMessage(R.string.confirm_reset)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        AppDatabase db = AppDatabase.getDatabase(requireContext());
                        long userId = sessionManager.getUserId();
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            db.transactionDao().deleteAllByUser(userId);
                            db.budgetDao().deleteAllByUser(userId);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), R.string.success, Toast.LENGTH_SHORT).show());
                        });
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        // Logout
        cardLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có muốn đăng xuất?")
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }
}
