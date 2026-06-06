package com.example.appquanlychitieu.data.model;

/**
 * Model đơn giản để nhận kết quả query tổng chi tiêu theo từng danh mục.
 * Dùng trong BudgetFragment để tránh tạo nhiều observers.
 */
public class CategorySpent {
    public long categoryId;
    public double spent;
}
