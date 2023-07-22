package com.example.moneymanager.ExpenseDB;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.moneymanager.DateConverter;

@Database(entities = {Expense.class}, version = 1, exportSchema = true)
@TypeConverters({DateConverter.class})
public abstract class ExpenseDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "expense-db";

    private static ExpenseDatabase instance;

    public static synchronized ExpenseDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ExpenseDatabase.class, DATABASE_NAME)
                    .build();
        }
        return instance;
    }
    public abstract ExpenseDao expenseDao();
}
