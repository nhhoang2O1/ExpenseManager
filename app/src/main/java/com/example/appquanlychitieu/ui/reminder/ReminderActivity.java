package com.example.appquanlychitieu.ui.reminder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.Reminder;
import com.example.appquanlychitieu.receiver.ReminderManager;
import com.example.appquanlychitieu.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

public class ReminderActivity extends AppCompatActivity {
    private ReminderViewModel viewModel;
    private SessionManager sessionManager;
    private ReminderAdapter adapter;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    
    private MaterialToolbar toolbar;
    private RecyclerView rvReminders;
    private TextView tvEmpty;
    
    private TextView tvDialogTitle, tvSelectedTime;
    private EditText etContent, etDay;
    private Button btnCancel, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvReminders = findViewById(R.id.rv_reminders);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new ReminderAdapter();
        rvReminders.setAdapter(adapter);

        viewModel.getReminders(sessionManager.getUserId()).observe(this, reminders -> {
            adapter.setReminders(reminders);
            if (reminders == null || reminders.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvReminders.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvReminders.setVisibility(View.VISIBLE);
            }
        });

        adapter.setOnReminderClickListener(new ReminderAdapter.OnReminderClickListener() {
            @Override
            public void onReminderClick(Reminder reminder) {
                showAddEditDialog(reminder);
            }

            @Override
            public void onReminderLongClick(Reminder reminder) {
                new AlertDialog.Builder(ReminderActivity.this)
                        .setTitle("Xoá nhắc nhở")
                        .setMessage("Bạn có chắc chắn muốn xoá nhắc nhở này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            ReminderManager.cancelReminder(ReminderActivity.this, reminder);
                            viewModel.delete(reminder);
                            Toast.makeText(ReminderActivity.this, "Đã xoá nhắc nhở", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onReminderSwitchToggle(Reminder reminder, boolean isChecked) {
                reminder.setActive(isChecked);
                viewModel.update(reminder);
                if (isChecked) {
                    ReminderManager.scheduleReminder(ReminderActivity.this, reminder);
                    Toast.makeText(ReminderActivity.this, "Đã bật nhắc nhở", Toast.LENGTH_SHORT).show();
                } else {
                    ReminderManager.cancelReminder(ReminderActivity.this, reminder);
                    Toast.makeText(ReminderActivity.this, "Đã tắt nhắc nhở", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(Reminder reminder) {
                new AlertDialog.Builder(ReminderActivity.this)
                        .setTitle("Xoá nhắc nhở")
                        .setMessage("Bạn có chắc chắn muốn xoá nhắc nhở này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            ReminderManager.cancelReminder(ReminderActivity.this, reminder);
                            viewModel.delete(reminder);
                            Toast.makeText(ReminderActivity.this, "Đã xoá nhắc nhở", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        findViewById(R.id.fab_add_reminder).setOnClickListener(v -> showAddEditDialog(null));

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private int selectedHour = 8;
    private int selectedMinute = 0;

    @SuppressLint("DefaultLocale")
    private void showAddEditDialog(Reminder reminder) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_reminder);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        tvDialogTitle = dialog.findViewById(R.id.tv_dialog_title);
        etContent = dialog.findViewById(R.id.et_content);
        etDay = dialog.findViewById(R.id.et_day);
        tvSelectedTime = dialog.findViewById(R.id.tv_selected_time);
        btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnSave = dialog.findViewById(R.id.btn_save);

        if (reminder != null) {
            tvDialogTitle.setText("Sửa nhắc nhở");
            etContent.setText(reminder.getContent());
            etDay.setText(String.valueOf(reminder.getDayOfMonth()));
            selectedHour = reminder.getHour();
            selectedMinute = reminder.getMinute();
        } else {
            selectedHour = 8;
            selectedMinute = 0;
        }

        tvSelectedTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));

        tvSelectedTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        tvSelectedTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }, selectedHour, selectedMinute, true);
            timePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            String dayStr = etDay.getText().toString().trim();

            if (TextUtils.isEmpty(content)) {
                etContent.setError("Vui lòng nhập nội dung");
                return;
            }

            if (TextUtils.isEmpty(dayStr)) {
                etDay.setError("Vui lòng nhập ngày");
                return;
            }

            int day;
            try {
                day = Integer.parseInt(dayStr);
                if (day < 1 || day > 31) {
                    etDay.setError("Ngày phải từ 1 đến 31");
                    return;
                }
            } catch (NumberFormatException e) {
                etDay.setError("Ngày không hợp lệ");
                return;
            }

            if (reminder == null) {
                Reminder newReminder = new Reminder(content, day, selectedHour, selectedMinute, sessionManager.getUserId(), true);
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    com.example.appquanlychitieu.data.repository.ReminderRepository repo = new com.example.appquanlychitieu.data.repository.ReminderRepository(getApplication());
                    long id = repo.insertSync(newReminder);
                    newReminder.setId(id);
                    runOnUiThread(() -> {
                        ReminderManager.scheduleReminder(ReminderActivity.this, newReminder);
                        Toast.makeText(ReminderActivity.this, "Đã thêm nhắc nhở", Toast.LENGTH_SHORT).show();
                    });
                });
            } else {
                reminder.setContent(content);
                reminder.setDayOfMonth(day);
                reminder.setHour(selectedHour);
                reminder.setMinute(selectedMinute);
                viewModel.update(reminder);
                if (reminder.isActive()) {
                    ReminderManager.scheduleReminder(this, reminder);
                }
                Toast.makeText(this, "Đã cập nhật nhắc nhở", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}
