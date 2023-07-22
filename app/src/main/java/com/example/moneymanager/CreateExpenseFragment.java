package com.example.moneymanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDao;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateExpenseFragment extends DialogFragment {

    private EditText amountEditText;
    private EditText nameEditText;
    private Button dateButton;
    private Calendar selectedDate;
    private Spinner categorySpinner;
    private OnExpenseSavedListener expenseSavedListener;
    public CreateExpenseFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_create_expense, null);

        amountEditText = view.findViewById(R.id.amountEditText);
        nameEditText = view.findViewById(R.id.nameEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        dateButton = view.findViewById(R.id.dateButton);

        // 初始化選擇的日期為當前日期
        selectedDate = Calendar.getInstance();

        // 初始化消費類別選項
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // 設置日期按鈕點擊監聽器
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        builder.setView(view)
        .setTitle("新增開銷")
        .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 保存消費記錄的邏輯
                if (validateInput()) {
                    saveExpense();
                }
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消操作的邏輯
                dismiss();
            }
        });

        return builder.create();
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
                updateDateButtonText();
            }
        };

        // 創建 DatePickerDialog 並設置選擇的日期
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        // 顯示 DatePickerDialog
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateString = sdf.format(selectedDate.getTime());
        dateButton.setText(selectedDateString);
    }

    private boolean validateInput() {
        String amount = amountEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(getContext(),"輸入資料不全，儲存失敗！",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(),"輸入資料不全，儲存失敗！",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveExpense() {
        // 獲取輸入的消費金額、名稱和類別
        String amount = amountEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        Date date = selectedDate.getTime();

        // 在AsyncTask中執行Room數據庫操作
        AsyncTask<Void, Void, Void> saveExpenseTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ExpenseDatabase expenseDatabase = ExpenseDatabase.getInstance(requireContext());
                ExpenseDao expenseDao = expenseDatabase.expenseDao();

                // 創建Expense對象並插入到數據庫
                Expense expense = new Expense(Double.parseDouble(amount), name, date, category);
                expenseDao.insertExpense(expense);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                // 調用回調方法通知消費記錄已保存
                if (expenseSavedListener != null) {
                    expenseSavedListener.onExpenseSaved();
                }
            }
        };

        // 執行AsyncTask
        saveExpenseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface OnExpenseSavedListener {
        void onExpenseSaved();
    }

    public CreateExpenseFragment(OnExpenseSavedListener listener) {
        this.expenseSavedListener = listener;
    }
}
