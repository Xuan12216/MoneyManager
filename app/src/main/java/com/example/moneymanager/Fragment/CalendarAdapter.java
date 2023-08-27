package com.example.moneymanager.Fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.moneymanager.ExpenseDB.Expense;
import com.example.moneymanager.ExpenseDB.ExpenseDatabase;
import com.example.moneymanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private List<String> calendarDaysList;
    private CalendarFragment calendarFragment;
    private ExpenseDatabase expenseDatabase;
    double totalBalance = 0.0;

    public CalendarAdapter(Context context, List<String> calendarDaysList, ExpenseDatabase expenseDatabase,CalendarFragment calendarFragment) {
        this.context = context;
        this.calendarDaysList = calendarDaysList;
        this.calendarFragment = calendarFragment;
        this.expenseDatabase = expenseDatabase;
    }

    public void notifyDataSetChanged() {
    }

    @Override
    public int getCount() {
        return calendarDaysList.size();
    }

    @Override
    public Object getItem(int position) {
        return calendarDaysList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(context)
                .inflate(R.layout.calendar_item, parent, false);

        TextView dayTextView = convertView.findViewById(R.id.dayTextView);
        TextView dayIncome = convertView.findViewById(R.id.dayTextViewIncome);
        TextView dayOutcome = convertView.findViewById(R.id.dayTextViewOutcome);
        TextView dayBalance = convertView.findViewById(R.id.dayTextViewBalance);

        String day = calendarDaysList.get(position);
        dayTextView.setText(day);

        LinearLayout rootLayout = convertView.findViewById(R.id.calendarItemLayout);

        // 判断当前日期是否为当天日期
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentMonth = sdf.format(calendar.getTime());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentYear = sdf1.format(calendar.getTime());

        String Month = String.valueOf(calendarFragment.getCurrentMonth());
        String Year = String.valueOf(calendarFragment.getCurrentYear());
        String currentDate = "";

        if (day.isEmpty()) day = "0";
        else if (Integer.parseInt(day) < 10) day = "0" + day;

        if (Integer.parseInt(Month) < 10) currentDate = day + " 0" + Month + " " + Year;
        else currentDate = day + " " + Month + " " + Year;

        String todayDate = String.valueOf(currentDay) + " " + currentMonth + " " + currentYear;

        Drawable background;
        //檢查Calendar目前生成的日期是不是今天的日期
        if (currentDate.equals(todayDate)) {
            // 设置当天日期的特殊背景样式
            background = context.getResources().getDrawable(R.drawable.date_background_today);
        } else {
            // 设置其他日期的默认背景样式
            background = context.getResources().getDrawable(R.drawable.date_background);
        }

        rootLayout.setBackground(background);

        loadExpensesTask(dayIncome, dayOutcome, dayBalance, currentDate);

        return convertView;
    }

    private void loadExpensesTask(TextView dayIncome, TextView dayOutcome, TextView dayBalance, String currentDate){
        final double[] totalIncome = {0};
        final double[] totalOutcome = {0};

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Expense> expenses = expenseDatabase.expenseDao().getAllExpenses();
                for (Expense expense : expenses) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                    String expenseDate = sdf1.format(expense.getDate());

                    //檢查Database目前的日期和當前Calender生成的日期一不一樣
                    if (expenseDate.equals(currentDate))
                    {
                        if (expense.getCategory().equals("收入")){
                            totalIncome[0] += expense.getAmount();
                            totalBalance += expense.getAmount();
                        }
                        else if (expense.getCategory().equals("支出")){
                            totalOutcome[0] += expense.getAmount();
                            totalBalance -= expense.getAmount();
                        }
                        else if (expense.getCategory().equals("餘額")) totalBalance += expense.getAmount();

                        // 使用Handler在UI线程上运行代码
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dayIncome.setText(String.format("%.2f", totalIncome[0]));
                                dayOutcome.setText(String.format("%.2f", totalOutcome[0]));
                                dayBalance.setText(String.format("%.2f", totalBalance));
                            }
                        });
                    }
                }
            }
        });
        executor.shutdown();
    }
}
