package com.example.moneymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDao;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class addBalanceActivity extends AppCompatActivity {

    private EditText balanceEditText;
    private EditText nameEditText;
    private Button addBalanceBackButton;
    private Button buttonEnter;
    private Button addBalanceDateBtn;
    private Button addBalanceDelBtn;
    private Calendar selectedDate;
    private ExpenseDatabase expenseDatabase;
    private Expense expense;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        // 获取expenseId
        int expenseID = getIntent().getIntExtra("expenseID",-1);
        System.out.println(expenseID);
        balanceEditText = findViewById(R.id.balanceEditText);
        nameEditText = findViewById(R.id.nameEditText);
        addBalanceBackButton = findViewById(R.id.addBalanceBackButton);
        buttonEnter = findViewById(R.id.buttonEnter);
        addBalanceDateBtn = findViewById(R.id.addBalanceDateBtn);
        addBalanceDelBtn = findViewById(R.id.addBalanceDelBtn);

        selectedDate = Calendar.getInstance();

        expenseDatabase = ExpenseDatabase.getInstance(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                if (expenseID!=-1) {
                    expense = expenseDatabase.expenseDao().getExpenseById(expenseID);
                    if (expense!=null) setText(nameEditText,balanceEditText,addBalanceDateBtn,expense);
                }
            }
        }).start();

        addBalanceDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expenseID==-1) Toast.makeText(getApplicationContext(),"沒有資料刪除！！",Toast.LENGTH_SHORT).show();
                else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Expense expense1 = expenseDatabase.expenseDao().getExpenseById(expenseID);
                            if (expense1!=null) expenseDatabase.expenseDao().deleteExpense(expense1);
                        }
                    }).start();
                }
                Intent intent = new Intent(addBalanceActivity.this,MainActivity2.class);
                intent.putExtra("Reload",1);
                startActivity(intent);
            }
        });

        addBalanceDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        addBalanceBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckEditText()){
                    saveExpense();
                    Intent intent = new Intent(addBalanceActivity.this,MainActivity2.class);
                    intent.putExtra("Reload",1);
                    startActivity(intent);
                }

            }
        });
    }

    private void setText(EditText nameEditText, EditText balanceEditText, Button addBalanceDateBtn, Expense expense) {
        nameEditText.setText(String.valueOf(expense.getName()));
        balanceEditText.setText(String.valueOf(expense.getAmount()));
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString1 = sdf1.format(expense.getDate());
        addBalanceDateBtn.setText(dateString1);
        selectedDate.setTime(expense.getDate());
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 更新選擇的日期
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, monthOfYear);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // 更新日期按鈕的文本
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDateString = sdf.format(selectedDate.getTime());
                addBalanceDateBtn.setText(selectedDateString);
            }
        };

        // 創建 DatePickerDialog 並設置選擇的日期
        DatePickerDialog datePickerDialog = new DatePickerDialog(addBalanceActivity.this, dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        // 顯示 DatePickerDialog
        datePickerDialog.show();
    }

    private void saveExpense() {
        // 獲取輸入的消費金額、名稱和類別
        String amount = balanceEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String category = "餘額";
        Date date = selectedDate.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String selectedDateString = sdf.format(date);//addBalanceDateBtn選中的日期

        ExecutorService saveExpenseTask = Executors.newSingleThreadExecutor();
        saveExpenseTask.execute(new Runnable() {
            @Override
            public void run() {
                ExpenseDao expenseDao = expenseDatabase.expenseDao();

                // 检查数据库中是否存在特定类别的数据
                List<Expense> expenses = expenseDao.getExpensesByCategory(category);

                if (expenses != null && expenses.size() > 0) {
                    // 如果存在，执行更新操作
                    for (int i=0;i<expenses.size();i++) {
                        Expense expense = expenses.get(i);

                        String expenseDate = sdf.format(expense.getDate());
                        if (expenseDate.equals(selectedDateString)){
                            expense.setAmount(Double.parseDouble(amount));
                            expense.setName(name);
                            expense.setDate(date);
                            expenseDao.updateExpense(expense);
                        }
                        else {
                            Expense expense1 = new Expense(Double.parseDouble(amount), name, date, category);
                            expenseDao.insertExpense(expense1);
                        }
                    }
                }
                else {
                    // 如果不存在，执行插入操作
                    Expense expense = new Expense(Double.parseDouble(amount), name, date, category);
                    expenseDao.insertExpense(expense);
                }

            }
        });
        saveExpenseTask.shutdown();
    }


    private boolean CheckEditText() {
        if (balanceEditText.getText().toString().trim().isEmpty() || nameEditText.getText().toString().trim().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"請輸入金額或名稱！",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}