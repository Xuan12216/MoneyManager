package com.example.moneymanager.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.moneymanager.CreateExpenseFragment;
import com.example.moneymanager.EditExpenseActivity;
import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;
import com.example.moneymanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements CreateExpenseFragment.OnExpenseSavedListener{

    private Button previousMonthButton;
    private Button nextMonthButton;
    private Button addExpenseButton;
    private TextView monthTextView;
    private ListView expenseListView;
    private int currentMonth;
    private int currentYear;
    private ExpenseDatabase expenseDatabase;
    public ArrayAdapter<Expense> expenseAdapter;
    private List<Expense> expenseList;
    private TextView homeIncome;
    private TextView homeOutcome;
    private TextView homeBalance;

    private Double income = 0.0;
    private Double outcome = 0.0;
    private Double balance = 0.0;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expenseDatabase = ExpenseDatabase.getInstance(requireContext());
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(requireContext(), expenseList);
        loadExpenses();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        previousMonthButton = view.findViewById(R.id.previousMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);
        addExpenseButton = view.findViewById(R.id.addExpenseButton);
        monthTextView = view.findViewById(R.id.monthTextView);
        expenseListView = view.findViewById(R.id.expenseListView);
        homeIncome = view.findViewById(R.id.homeIncome);
        homeOutcome = view.findViewById(R.id.homeOutcome);
        homeBalance = view.findViewById(R.id.homeBalance);

        expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击的Expense对象
                Expense clickedExpense = expenseAdapter.getItem(position);

                // 创建Intent并传递数据
                Intent intent = new Intent(requireContext(), EditExpenseActivity.class);
                intent.putExtra("expenseId", clickedExpense.getId());

                // 启动目标Activity
                startActivity(intent);
            }
        });

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 彈出 CreateExpenseFragment
                CreateExpenseFragment createExpenseFragment = new CreateExpenseFragment(HomeFragment.this);
                createExpenseFragment.show(getParentFragmentManager(), "create_expense_dialog");
            }
        });

        previousMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth--;
                if(currentMonth<=-1) {
                    currentMonth = 11;
                    currentYear-=1;
                }
                updateHomeIncomeAndOutCome();
                updateMonthTextView();
                loadExpenses();
            }
        });

        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth++;
                if(currentMonth>=12){
                    currentMonth = 0;
                    currentYear+=1;
                }
                updateHomeIncomeAndOutCome();
                updateMonthTextView();
                loadExpenses();
            }
        });

        // 初始化當前月份為當月
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);
        updateMonthTextView();

        expenseListView.setAdapter(expenseAdapter);

        updateHomeIncomeAndOutCome();

        return view;
    }
    private void updateMonthTextView() {
        String monthName = getMonthName(currentMonth,currentYear);
        expenseList.clear();
        monthTextView.setText(monthName);
    }
    private String getMonthName(int month,int year) {
        String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        return String.valueOf(year) + "年 " + monthNames[month];
    }
    private void updateHomeIncomeAndOutCome() {
        income = 0.0;
        outcome = 0.0;
        balance = 0.0;

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
                } else if (expense.getCategory().equals("餘額")){
                    balance += expense.getAmount();
                }
            }
        }
        homeIncome.setText("收入\nNT$" + String.format(Locale.getDefault(), "%.2f", income));
        homeOutcome.setText("支出\nNT$" + String.format(Locale.getDefault(), "%.2f", outcome));
        homeBalance.setText("餘額\nNT$" + String.format(Locale.getDefault(), "%.2f", balance-outcome+income));
    }
    private void loadExpenses() {
        // 使用 AsyncTask 在後台執行 Room Database 操作
        AsyncTask<Void, Void, List<Expense>> loadExpensesTask = new AsyncTask<Void, Void, List<Expense>>() {
            @Override
            protected List<Expense> doInBackground(Void... voids) {
                return expenseDatabase.expenseDao().getAllExpenses();
            }

            @Override
            protected void onPostExecute(List<Expense> expenses) {
                expenseList.clear();
                expenseList.addAll(expenses);
                expenseAdapter.notifyDataSetChanged();

                updateHomeIncomeAndOutCome();
            }
        };

        // 執行 AsyncTask
        loadExpensesTask.execute();
    }

    @Override
    public void onExpenseSaved() {
        loadExpenses();
    }

    private class ExpenseAdapter extends ArrayAdapter<Expense> {
        public ExpenseAdapter(Context context, List<Expense> expenses) {
            super(context, 0, expenses);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.expense_list_item, parent, false);
            }

            Expense currentExpense = getItem(position);

            // 檢查消費記錄的月份是否與當前月份一致
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String monthString = sdf.format(currentExpense.getDate());
            String year = monthString.substring(0,4);
            String month = monthString.substring(5, 7);

            TextView nameTextView = itemView.findViewById(R.id.nameTextView);
            TextView amountTextView = itemView.findViewById(R.id.amountTextView);
            TextView dateTextView = itemView.findViewById(R.id.dateTextView);
            TextView dateBigTextView = itemView.findViewById(R.id.dateBigTextView);
            ConstraintLayout constraintLayout = itemView.findViewById(R.id.constraintLayout);
            FrameLayout cardView = itemView.findViewById(R.id.cardView);
            LinearLayout layout = itemView.findViewById(R.id.LinearLayout);
            View view = itemView.findViewById(R.id.view);

            if (currentMonth + 1 == Integer.parseInt(month) && currentYear == Integer.parseInt(year)) {
                constraintLayout.setVisibility(View.VISIBLE);
                nameTextView.setVisibility(View.VISIBLE);
                amountTextView.setVisibility(View.VISIBLE);
                dateTextView.setVisibility(View.VISIBLE);
                dateBigTextView.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.VISIBLE);

                nameTextView.setText("消費名稱：" + currentExpense.getName());
                amountTextView.setText("NT$"+String.valueOf(currentExpense.getAmount())+" " + currentExpense.getCategory());

                if(currentExpense.getCategory().equals("收入"))
                    amountTextView.setTextColor(ContextCompat.getColor(this.getContext(), R.color.green));
                else if(currentExpense.getCategory().equals("支出"))
                    amountTextView.setTextColor(ContextCompat.getColor(this.getContext(), R.color.red));

                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String dateString2 = sdf2.format(currentExpense.getDate());
                dateTextView.setText(dateString2.substring(0,10) + "    " + dateString2.substring(11,16));

                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateString1 = sdf1.format(currentExpense.getDate());

                Expense nextExpense;
                String nextDateString = null;

                if (position + 1 < expenseList.size()){
                    nextExpense = getItem(position + 1);
                    nextDateString = sdf1.format(nextExpense.getDate());
                }

                // 檢查是否與前一項目的日期相同
                if (position > 0) {
                    Expense previousExpense = getItem(position - 1);
                    String previousDateString = sdf1.format(previousExpense.getDate());

                    if (dateString1.equals(previousDateString)) {
                        // 相同日期，隱藏日期的分割線
                        dateBigTextView.setVisibility(View.GONE);

                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                        layoutParams.setMargins(0,0,0,0);
                        cardView.setLayoutParams(layoutParams);

                        if (dateString1.equals(nextDateString) && nextDateString != null){
                            cardView.setBackgroundResource(R.drawable.cardview_no_round);
                            view.setVisibility(View.VISIBLE);
                        }
                        else {
                            cardView.setBackgroundResource(R.drawable.cardview_half_round);
                            view.setVisibility(View.GONE);
                        }
                    }
                    else {
                        // 不同日期，顯示日期的分割線並更新日期
                        dateBigTextView.setVisibility(View.VISIBLE);
                        dateBigTextView.setText(dateString1);

                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                        layoutParams.setMargins(0,25,0,0);
                        cardView.setLayoutParams(layoutParams);

                        if (dateString1.equals(nextDateString) && nextDateString != null) {
                            cardView.setBackgroundResource(R.drawable.cardview_half_round_1);
                            view.setVisibility(View.VISIBLE);
                        }
                        else {
                            cardView.setBackgroundResource(R.drawable.cardview_round);
                            view.setVisibility(View.GONE);
                        }
                    }
                }
                else {
                    // 第一項目，直接顯示日期的分割線
                    dateBigTextView.setVisibility(View.VISIBLE);
                    dateBigTextView.setText(dateString1);

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                    layoutParams.setMargins(0,25,0,0);
                    cardView.setLayoutParams(layoutParams);

                    if (nextDateString == null) {
                        cardView.setBackgroundResource(R.drawable.cardview_round);
                        view.setVisibility(View.GONE);
                    }
                    else if (dateString1.equals(nextDateString)) {
                        cardView.setBackgroundResource(R.drawable.cardview_half_round_1);
                        view.setVisibility(View.VISIBLE);
                    }
                    else{
                        cardView.setBackgroundResource(R.drawable.cardview_round);
                        view.setVisibility(View.GONE);
                    }

                }
            }
            else {
                constraintLayout.setVisibility(View.GONE);
                nameTextView.setVisibility(View.GONE);
                amountTextView.setVisibility(View.GONE);
                dateTextView.setVisibility(View.GONE);
                dateBigTextView.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
            }
            return itemView;
        }
    }
}