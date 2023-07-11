package com.example.moneymanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button loginButton;
    ExpenseDatabase expenseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expenseDatabase= Room.databaseBuilder(this, ExpenseDatabase.class, "expense-db")
                .build();
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                startActivity(intent);

                // 使用 SharedPreferences 將 MainActivity2 設置為主頁
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isMainPageSet", true);
                editor.apply();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isMainPageSet = sharedPreferences.getBoolean("isMainPageSet", false);

        if (isMainPageSet) {
            // 如果 MainActivity2 被設置為主頁，直接跳轉到 MainActivity2
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish(); // 結束 MainActivity，避免返回到它
        }
    }
}