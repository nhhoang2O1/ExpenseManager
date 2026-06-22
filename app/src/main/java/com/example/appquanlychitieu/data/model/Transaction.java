package com.example.appquanlychitieu.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index(value = "categoryId"))
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private double amount;
    private String note;
    private long date; 
    private Long categoryId; 
    private TransactionType type;
    private long userId;

    public Transaction() {}

    public Transaction(double amount, String note, long date, Long categoryId, TransactionType type, long userId) {
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.categoryId = categoryId;
        this.type = type;
        this.userId = userId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
