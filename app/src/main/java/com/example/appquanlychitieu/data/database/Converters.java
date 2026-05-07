package com.example.appquanlychitieu.data.database;

import androidx.room.TypeConverter;

import com.example.appquanlychitieu.data.model.TransactionType;

public class Converters {
    @TypeConverter
    public static String fromTransactionType(TransactionType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static TransactionType toTransactionType(String value) {
        return value == null ? null : TransactionType.valueOf(value);
    }
}
