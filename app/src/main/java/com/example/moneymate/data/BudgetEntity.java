package com.example.moneymate.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class BudgetEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String category;
    public double amount;
    public int month;
    public int year;

    public BudgetEntity(String category, double amount, int month, int year) {
        this.category = category;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }
}
