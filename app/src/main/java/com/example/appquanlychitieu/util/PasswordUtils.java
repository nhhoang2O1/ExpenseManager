package com.example.appquanlychitieu.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích mã hóa mật khẩu bằng SHA-256.
 * Không cần thêm thư viện ngoài — MessageDigest có sẵn trong Java/Android.
 */
public class PasswordUtils {

    /**
     * Chuyển mật khẩu plain text thành chuỗi hash SHA-256.
     * Ví dụ: "123456" → "8d969eef6ecad3c29a3a629280e686cf..."
     */
    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            // Chuyển mảng byte thành chuỗi hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có sẵn trên Android, trường hợp này không xảy ra
            throw new RuntimeException("SHA-256 không khả dụng", e);
        }
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập có khớp với hash đã lưu không.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return hash(plainPassword).equals(hashedPassword);
    }
}
