package com.example.appquanlychitieu.data.model;

public class MonthlySummary {
    private String monthYear;
    private double totalIncome;
    private double totalExpense;

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getBalance() { return totalIncome - totalExpense; }
}
