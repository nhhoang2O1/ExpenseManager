package com.example.appquanlychitieu.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Tiện ích hash mật khẩu bằng BCrypt.
 */
public class PasswordUtils {
    private static final int COST = 12;

    /**
     * Chuyển mật khẩu plain text thành chuỗi hash BCrypt có salt riêng.
     */
    public static String hash(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập có khớp với hash đã lưu không.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified;
    }
}
