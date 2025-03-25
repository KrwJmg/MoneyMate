package com.example.moneymate.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moneymate.R;
import com.example.moneymate.data.AppDatabase;
import com.example.moneymate.data.TransactionEntity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsFragment extends Fragment {

    private PieChart pieChart;
    private Calendar currentMonth;
    private TextView txtMonthLabel, txtTotalExpense;
    private ImageButton btnPrev, btnNext;
    private LinearLayout layoutCategoryBreakdown;

    private final int[] COLORS = {
            Color.parseColor("#FF6384"),
            Color.parseColor("#FF9F40"),
            Color.parseColor("#FFCD56"),
            Color.parseColor("#4BC0C0"),
            Color.parseColor("#36A2EB"),
            Color.parseColor("#9966FF")
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Bind views
        pieChart = view.findViewById(R.id.pieChart);
        txtMonthLabel = view.findViewById(R.id.txtMonthLabel);
        txtTotalExpense = view.findViewById(R.id.txtTotalExpense);
        btnPrev = view.findViewById(R.id.btnPrevMonth);
        btnNext = view.findViewById(R.id.btnNextMonth);
        layoutCategoryBreakdown = view.findViewById(R.id.layoutCategoryBreakdown);

        currentMonth = Calendar.getInstance();

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadExpenseDataForMonth();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadExpenseDataForMonth();
        });

        loadExpenseDataForMonth();

        return view;
    }

    private void loadExpenseDataForMonth() {
        new Thread(() -> {
            Calendar start = (Calendar) currentMonth.clone();
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);

            long startMillis = start.getTimeInMillis();
            long endMillis = end.getTimeInMillis();

            List<TransactionEntity> transactions = AppDatabase.getInstance(requireContext())
                    .transactionDao()
                    .getTransactionsBetween(startMillis, endMillis);

            Map<String, Double> categoryTotals = new LinkedHashMap<>();
            double totalExpense = 0;

            for (TransactionEntity t : transactions) {
                if (!"expense".equals(t.type)) continue;
                categoryTotals.put(t.category, categoryTotals.getOrDefault(t.category, 0.0) + t.amount);
                totalExpense += t.amount;
            }

            List<PieEntry> entries = new ArrayList<>();
            List<Map.Entry<String, Double>> topCategories = new ArrayList<>(categoryTotals.entrySet());

            for (Map.Entry<String, Double> entry : topCategories) {
                float percent = (float) ((entry.getValue() / totalExpense) * 100f);
                entries.add(new PieEntry(percent, entry.getKey()));
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(COLORS);
            PieData pieData = new PieData(dataSet);
            pieData.setValueTextColor(Color.WHITE);
            pieData.setValueTextSize(12f);

            String monthYear = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(currentMonth.getTime());
            String expenseText = String.format(Locale.getDefault(), "Total Expenses: ฿%.2f", totalExpense);

            final double totalExpenseFinal = totalExpense;

            requireActivity().runOnUiThread(() -> {
                txtMonthLabel.setText(monthYear);
                txtTotalExpense.setText(expenseText);

                pieChart.setData(pieData);
                pieChart.setUsePercentValues(true);
                pieChart.setDrawHoleEnabled(false);
                pieChart.getDescription().setEnabled(false);
                pieChart.getLegend().setEnabled(false);
                pieChart.invalidate();

                layoutCategoryBreakdown.removeAllViews(); // ล้างก่อนเพิ่มใหม่

                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    View categoryItem = LayoutInflater.from(requireContext()).inflate(R.layout.item_category_stat, layoutCategoryBreakdown, false);

                    TextView txtCategory = categoryItem.findViewById(R.id.txtCategoryName);
                    TextView txtAmount = categoryItem.findViewById(R.id.txtCategoryAmount);
                    TextView txtPercent = categoryItem.findViewById(R.id.txtCategoryPercent);

                    txtCategory.setText(entry.getKey());
                    txtAmount.setText(String.format(Locale.getDefault(), "฿%.2f", entry.getValue()));
                    txtPercent.setText(String.format(Locale.getDefault(), "%.0f%%", (entry.getValue() / totalExpenseFinal) * 100));

                    layoutCategoryBreakdown.addView(categoryItem);
                }
            });

        }).start();
    }
}
