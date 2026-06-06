package com.example.appquanlychitieu.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appquanlychitieu.R;
import com.example.appquanlychitieu.data.database.AppDatabase;
import com.example.appquanlychitieu.data.model.User;
import com.example.appquanlychitieu.util.PasswordUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        MaterialButton btnRegister = findViewById(R.id.btn_register);
        TextView tvLogin = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(v -> register());

        tvLogin.setOnClickListener(v -> {
            finish(); // Quay lại LoginActivity
        });
    }

    private void register() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validation
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ tên");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Kiểm tra email đã tồn tại
            int exists = db.userDao().checkEmailExists(email);
            if (exists > 0) {
                runOnUiThread(() -> {
                    etEmail.setError("Email đã được sử dụng");
                    etEmail.requestFocus();
                });
                return;
            }

            // Tạo user mới — hash mật khẩu trước khi lưu vào database
            String hashedPassword = PasswordUtils.hash(password);
            User user = new User(name, email, hashedPassword);
            db.userDao().insert(user);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại LoginActivity
            });
        });
    }
}
