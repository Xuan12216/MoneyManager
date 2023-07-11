package com.example.moneymanager;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insertExpense(Expense expense);

    @Update
    void updateExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY DATE DESC")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE category = :category")
    List<Expense> getExpensesByCategory(String category);

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    Expense getExpenseById(int expenseId);

    // 其他必要的查詢方法等
}
