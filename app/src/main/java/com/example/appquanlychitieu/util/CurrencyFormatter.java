package com.example.appquanlychitieu.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        formatter = new DecimalFormat("#,###", symbols);
    }

    public static String format(double amount) {
        return formatter.format(amount) + " ₫";
    }

    public static String formatNoSymbol(double amount) {
        return formatter.format(amount);
    }

    public static String formatWithSign(double amount, boolean isExpense) {
        String prefix = isExpense ? "- " : "+ ";
        return prefix + format(Math.abs(amount));
    }
}
