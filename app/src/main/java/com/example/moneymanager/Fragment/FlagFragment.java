package com.example.moneymanager.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDao;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;
import com.example.moneymanager.R;
import com.example.moneymanager.addBalanceActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlagFragment extends Fragment {
    private Button preButton;
    private Button nextButton;
    private TextView monthTextView;
    private CardView cardView1;
    private TextView cardTextView1;
    private int currentMonth;
    private int currentYear;
    private int flag=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flag, container, false);

        preButton = view.findViewById(R.id.previousMonthButtonFlag);
        nextButton = view.findViewById(R.id.nextMonthButtonFlag);
        monthTextView = view.findViewById(R.id.monthTextViewFlag);
        cardView1 = view.findViewById(R.id.CardView1);
        cardTextView1 = view.findViewById(R.id.CardTextView1);

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), addBalanceActivity.class);
                if (flag!=0) intent.putExtra("expenseID",flag);
                startActivity(intent);
            }
        });

        preButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth--;
                if(currentMonth<=-1){
                    currentMonth = 11;
                    currentYear--;
                }
                updateMonthTextView();
                loadExpense(currentYear,currentMonth+1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth++;
                if(currentMonth>=12){
                    currentMonth = 0;
                    currentYear++;
                }
                updateMonthTextView();
                loadExpense(currentYear,currentMonth+1);

            }
        });

        // 初始化當前月份為當月
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);
        updateMonthTextView();

        loadExpense(currentYear,currentMonth+1);
        return view;
    }

    private void loadExpense(int currentYear, int currentMonth) {
        String year = String.valueOf(currentYear);
        String month = String.valueOf(currentMonth);

        if (Integer.parseInt(month)<10)
            month = "0"+month;

        String currentYearAndMonth = year + "-" + month;

        ExecutorService saveExpenseTask = Executors.newSingleThreadExecutor();
        saveExpenseTask.execute(new Runnable() {
            @Override
            public void run() {
                ExpenseDatabase expenseDatabase = ExpenseDatabase.getInstance(requireContext());
                ExpenseDao expenseDao = expenseDatabase.expenseDao();

                // 检查数据库中是否存在特定类别的数据
                List<Expense> expenses = expenseDao.getExpensesByCategory("餘額");

                if (expenses != null && expenses.size() > 0) {
                    for (int i=0;i<expenses.size();i++) {
                        // 如果存在，执行更新操作
                        Expense expense = expenses.get(i);
                        if (formatDate1(expense.getDate()).equals(currentYearAndMonth)) {
                            flag=expense.getId();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {cardTextView1.setText("預算名稱：" + expense.getName() + "\n\n設定預算：$" + expense.getAmount() + "\n\n設定日期："+ formatDate(expense.getDate()));}
                            });
                            break;
                        }
                        else {
                            flag=0;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {cardTextView1.setText("預算名稱：--\n\n設定餘額：$0.0 \n\n設定日期：yyyy-MM-dd");}
                            });
                        }
                    }
                }
                else {
                    flag=0;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {cardTextView1.setText("預算名稱：--\n\n設定餘額：$0.0 \n\n設定日期：yyyy-MM-dd");}
                    });
                }
            }
        });
        saveExpenseTask.shutdown();
    }

    private String formatDate1(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(date);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private void updateMonthTextView() {
        String monthName = getMonthName(currentMonth,currentYear);
        monthTextView.setText(monthName);
    }
    private String getMonthName(int currentMonth,int currentYear) {
        String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        return String.valueOf(currentYear)+ "年 "+monthNames[currentMonth];
    }
}