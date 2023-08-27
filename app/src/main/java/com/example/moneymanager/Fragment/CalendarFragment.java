package com.example.moneymanager.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;
import com.example.moneymanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment {

    private Button preMonthBtn;
    private Button nxtMonthBtn;
    private TextView monthTxtV;
    private TextView incomeTxt;
    private TextView outcomeTxt;
    private TextView balanceTxt;
    private int currentMonth;
    private int currentYear;

    private ExpenseDatabase expenseDatabase;
    private List<Expense> expenseList;

    private Double income = 0.0;
    private Double outcome = 0.0;
    private Double balance = 0.0;
    private GridView calendarGridView;
    private CalendarAdapter calendarAdapter;
    private List<String> calendarDaysList;
    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expenseDatabase = ExpenseDatabase.getInstance(requireContext());
        expenseList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        preMonthBtn = view.findViewById(R.id.previousMonthButton_calendar);
        nxtMonthBtn = view.findViewById(R.id.nextMonthButton_calendar);
        monthTxtV = view.findViewById(R.id.monthTextView_calendar);
        incomeTxt = view.findViewById(R.id.calendarIncome);
        outcomeTxt = view.findViewById(R.id.calendarOutcome);
        balanceTxt = view.findViewById(R.id.calendarBalance);
        calendarGridView = view.findViewById(R.id.calendarGridView);

        preMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth--;
                if(currentMonth<=-1){
                    currentMonth = 11;
                    currentYear--;
                }
                updateHomeIncomeAndOutCome();
                updateMonthTextView();
                updateCalendar(); // 更新日历日期
            }
        });

        nxtMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth++;
                if(currentMonth>=12){
                    currentMonth = 0;
                    currentYear++;
                }
                updateHomeIncomeAndOutCome();
                updateMonthTextView();
                updateCalendar(); // 更新日历日期

            }
        });

        // 初始化當前月份為當月
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);
        updateMonthTextView();

        updateHomeIncomeAndOutCome();
        // 初始化日历数据
        calendarDaysList = new ArrayList<>();
        calendarAdapter = new CalendarAdapter(requireContext(), calendarDaysList,expenseDatabase, this);


        // 设置日历GridView的适配器
        calendarGridView.setAdapter(calendarAdapter);

        // 更新日历显示
        updateCalendar();
        return view;
    }

    private void updateCalendar() {
        // 清空日历数据列表
        calendarDaysList.clear();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 获取当前年份和月份
                Calendar calendar = Calendar.getInstance();
                int year = currentYear;
                int month = currentMonth;

                // 设置日历为当前年份和月份
                calendar.set(year, month, 1);

                // 获取当前月份的第一天是星期几
                int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // 获取当前月份的总天数
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                // 添加空白的占位符，直到第一天之前的位置
                for (int i = 1; i < firstDayOfWeek; i++) {
                    calendarDaysList.add("");
                }

                // 添加当前月份的日期
                for (int day = 1; day <= daysInMonth; day++) {
                    calendarDaysList.add(String.valueOf(day));
                }

                // 添加空白的占位符，直到列表长度是7的倍数
                while (calendarDaysList.size() % 7 != 0) {
                    calendarDaysList.add("");
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 重新设置适配器的数据源
                        calendarAdapter = new CalendarAdapter(requireContext(), calendarDaysList, expenseDatabase,CalendarFragment.this);

                        // 设置日历GridView的适配器
                        calendarGridView.setAdapter(calendarAdapter);

                        // 通知适配器数据源改变
                        calendarAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        executor.shutdown();
    }


    private void updateMonthTextView() {
        String monthName = getMonthName(currentMonth,currentYear);
        monthTxtV.setText(monthName);
    }

    private String getMonthName(int currentMonth,int currentYear) {
        String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        return String.valueOf(currentYear)+ "年 "+monthNames[currentMonth];
    }

    private void updateHomeIncomeAndOutCome() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                income = 0.0;
                outcome = 0.0;
                balance = 0.0;

                List<Expense> expenses = expenseDatabase.expenseDao().getAllExpenses();
                expenseList.clear();
                expenseList.addAll(expenses);

                for (Expense expense : expenseList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                    String monthString = sdf.format(expense.getDate());
                    String year = monthString.substring(0,4);
                    String month = monthString.substring(5,7);

                    if (Integer.parseInt(month) == currentMonth + 1 && Integer.parseInt(year) == currentYear) {
                        if (expense.getCategory().equals("收入")) {
                            income += expense.getAmount();
                        } else if (expense.getCategory().equals("支出")) {
                            outcome += expense.getAmount();
                        } else if (expense.getCategory().equals("餘額")) {
                            balance += expense.getAmount();
                        }
                    }
                }

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        incomeTxt.setText("收入\nNT$" + String.format(Locale.getDefault(), "%.2f", income));
                        outcomeTxt.setText("支出\nNT$" + String.format(Locale.getDefault(), "%.2f", outcome));
                        balanceTxt.setText("餘額\nNT$" + String.format(Locale.getDefault(), "%.2f", balance-outcome+income));
                    }
                });
            }
        });
        executor.shutdown();
    }

    public int getCurrentMonth() {
        return currentMonth+1;
    }

    public int getCurrentYear() {return currentYear;}
}