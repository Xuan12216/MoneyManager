package com.example.moneymanager.Fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.moneymanager.Expense;
import com.example.moneymanager.ExpenseDatabase;
import com.example.moneymanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    private class LoadExpensesTask extends AsyncTask<Void, Void, List<Expense>> {
        private TextView dayIncome;
        private TextView dayOutcome;
        private TextView dayBalance;
        private String currentDate;
        double totalIncome = 0;
        double totalOutcome = 0;

        public LoadExpensesTask(TextView dayIncome, TextView dayOutcome,TextView dayBalance ,String currentDate) {
            this.dayIncome = dayIncome;
            this.dayOutcome = dayOutcome;
            this.dayBalance = dayBalance;
            this.currentDate = currentDate;

        }

        @Override
        protected List<Expense> doInBackground(Void... voids) {
            return expenseDatabase.expenseDao().getAllExpenses();
        }

        @Override
        protected void onPostExecute(List<Expense> expenses) {

            for (Expense expense : expenses) {
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                String expenseDate = sdf1.format(expense.getDate());

                //檢查Database目前的日期和當前Calender生成的日期一不一樣
                if (expenseDate.equals(currentDate))
                {
                    if (expense.getCategory().equals("收入")){
                        totalIncome += expense.getAmount();
                        totalBalance += expense.getAmount();
                    }
                    else if (expense.getCategory().equals("支出")){
                        totalOutcome += expense.getAmount();
                        totalBalance -= expense.getAmount();
                    }

                    else if (expense.getCategory().equals("餘額"))
                        totalBalance += expense.getAmount();

                    dayIncome.setText(String.valueOf(totalIncome));
                    dayOutcome.setText(String.valueOf(totalOutcome));
                    dayBalance.setText(String.valueOf(totalBalance));
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.calendar_item, parent, false);
        }

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

        if (day.isEmpty()) {
            day = "0";
        } else if (Integer.parseInt(day) < 10) {
            day = "0" + day;
        }

        if (Integer.parseInt(Month) < 10)
            currentDate = day + " 0" + Month + " " + Year;
        else
            currentDate = day + " " + Month + " " + Year;
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

        LoadExpensesTask loadExpensesTask = new LoadExpensesTask(dayIncome, dayOutcome,dayBalance,currentDate);
        loadExpensesTask.execute();

        return convertView;
    }
}
