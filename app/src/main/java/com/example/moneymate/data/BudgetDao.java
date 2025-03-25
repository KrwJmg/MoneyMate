package com.example.moneymate.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert
    void insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Delete
    void delete(BudgetEntity budget);

    @Query("DELETE FROM budgets WHERE category = :category AND month = :month AND year = :year")
    void deleteSingleBudget(String category, int month, int year);

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    void deleteBudgetsForMonth(int month, int year);

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    List<BudgetEntity> getBudgetsForMonth(int year, int month);



    @Query("SELECT * FROM budgets WHERE category = :category AND year = :year AND month = :month LIMIT 1")
    BudgetEntity getBudgetForCategory(String category, int year, int month);
}
