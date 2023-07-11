package com.example.moneymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {

    private EditText editExpenseName;
    private EditText editExpenseAmount;
    private Button updateExpenseButton;
    private Spinner editExpenseCategory;
    private Button editBackButton;
    private Button editDateButton;
    private Calendar selectedDate;
    private Button deleteExpenseButton;

    private ExpenseDatabase expenseDatabase;
    private Expense expense;
    private Integer flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        // 获取expenseId
        int expenseId = getIntent().getIntExtra("expenseId", -1);

        // 初始化UI控件
        editExpenseName = findViewById(R.id.editExpenseName);
        editExpenseAmount = findViewById(R.id.editExpenseAmount);
        updateExpenseButton = findViewById(R.id.updateExpenseButton);
        editExpenseCategory = findViewById(R.id.editExpenseSpinner);
        editBackButton = findViewById(R.id.backButton);
        editDateButton = findViewById(R.id.editExpenseDateButton);
        deleteExpenseButton = findViewById(R.id.deleteExpenseButton);

        selectedDate = Calendar.getInstance();
        // 初始化数据库
        expenseDatabase = ExpenseDatabase.getInstance(this);

        // 使用异步任务从数据库获取Expense对象
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                expense = expenseDatabase.expenseDao().getExpenseById(expenseId);
                // 在UI线程更新UI控件的数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 将Expense对象的数据填充到UI控件中
                        editExpenseName.setText(expense.getName());
                        editExpenseAmount.setText(String.valueOf(expense.getAmount()));

                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String dateString1 = sdf1.format(expense.getDate());
                        editDateButton.setText(dateString1);

                        // 设置消费类别的Spinner
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                EditExpenseActivity.this,
                                R.array.expense_categories,
                                android.R.layout.simple_spinner_item
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        editExpenseCategory.setAdapter(adapter);
                        int categoryPosition = adapter.getPosition(expense.getCategory());
                        editExpenseCategory.setSelection(categoryPosition);
                    }
                });
            }
        });

        deleteExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 执行数据库更新操作
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        expenseDatabase.expenseDao().deleteExpense(expense);
                    }
                });
                Intent intent = new Intent(EditExpenseActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        editDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        // 点击更新按钮时的逻辑
        updateExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取修改后的数据
                String updatedName = editExpenseName.getText().toString();
                double updatedAmount = Double.parseDouble(editExpenseAmount.getText().toString());
                String updatedCategory = editExpenseCategory.getSelectedItem().toString();
                Date updatedDate = selectedDate.getTime();

                if (flag==0)
                    updatedDate = expense.getDate();
                else if (flag==1) {
                    updatedDate = selectedDate.getTime();
                }
                // 更新Expense对象的数据
                expense.setName(updatedName);
                expense.setAmount(updatedAmount);
                expense.setCategory(updatedCategory);
                expense.setDate(updatedDate);

                // 执行数据库更新操作
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        expenseDatabase.expenseDao().updateExpense(expense);
                    }
                });
                Intent intent = new Intent(EditExpenseActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        // 点击返回按钮时关闭当前Activity
        editBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showDatePicker() {
        flag=1;
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 更新選擇的日期
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, monthOfYear);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // 更新日期按鈕的文本
                updateDateButtonText();
            }
        };

        // 創建 DatePickerDialog 並設置選擇的日期
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        // 顯示 DatePickerDialog
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateString = sdf.format(selectedDate.getTime());
        editDateButton.setText(selectedDateString);
    }
}
