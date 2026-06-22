package com.example.appquanlychitieu.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = {"categoryId", "monthYear", "userId"}, unique = true))
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long categoryId;
    private double amount;
    private String monthYear; 
    private long userId;

    public Budget() {}

    public Budget(long categoryId, double amount, String monthYear, long userId) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.monthYear = monthYear;
        this.userId = userId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
