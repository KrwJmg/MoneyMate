package com.example.moneymate.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moneymate.R;
import com.example.moneymate.data.AppDatabase;
import com.example.moneymate.data.BudgetEntity;
import com.example.moneymate.data.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.*;

public class BudgetFragment extends Fragment {

    private Calendar currentMonth;
    private TextView txtMonthLabel, txtRemaining;
    private LinearLayout layoutBudgets;
    private ImageButton btnNext, btnPrev;
    private Button btnSetBudget;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Bind views
        btnPrev = view.findViewById(R.id.btnPrevMonth);
        btnNext = view.findViewById(R.id.btnNextMonth);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);
        txtMonthLabel = view.findViewById(R.id.txtBudgetMonth);
        txtRemaining = view.findViewById(R.id.txtRemainingBudget);
        layoutBudgets = view.findViewById(R.id.layoutBudgetList);

        currentMonth = Calendar.getInstance();

        btnPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadBudgets();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadBudgets();
        });

        btnSetBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SetBudgetActivity.class);
            startActivity(intent);
        });

        loadBudgets();
        return view;
    }

    private void loadBudgets() {
        new Thread(() -> {
            int year = currentMonth.get(Calendar.YEAR);
            int month = currentMonth.get(Calendar.MONTH);

            List<BudgetEntity> budgets = AppDatabase.getInstance(requireContext())
                    .budgetDao()
                    .getBudgetsForMonth(year, month);

            long[] monthRange = getMonthRangeMillis();
            List<TransactionEntity> transactions = AppDatabase.getInstance(requireContext())
                    .transactionDao()
                    .getTransactionsBetween(monthRange[0], monthRange[1]);

            Map<String, Double> spending = new HashMap<>();
            for (TransactionEntity t : transactions) {
                if (!"expense".equals(t.type)) continue;
                spending.put(t.category, spending.getOrDefault(t.category, 0.0) + t.amount);
            }

            double totalBudget = 0, totalSpent = 0;
            for (BudgetEntity b : budgets) {
                totalBudget += b.amount;
                totalSpent += spending.getOrDefault(b.category, 0.0);
            }

            String monthText = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(currentMonth.getTime());
            double remaining = totalBudget - totalSpent;

            Log.d("BudgetCheck", "Budgets found: " + budgets.size());

            requireActivity().runOnUiThread(() -> {
                txtMonthLabel.setText(monthText);
                txtRemaining.setText(String.format(Locale.getDefault(), "Remaining: ฿%.2f", remaining));
                layoutBudgets.removeAllViews();

                for (BudgetEntity b : budgets) {
                    View item = LayoutInflater.from(requireContext()).inflate(R.layout.item_budget_stat, layoutBudgets, false);

                    TextView txtCategory = item.findViewById(R.id.txtCategory);
                    TextView txtUsed = item.findViewById(R.id.txtUsedAmount);
                    TextView txtLimit = item.findViewById(R.id.txtLimitAmount);
                    TextView txtPercent = item.findViewById(R.id.txtPercent);
                    ProgressBar progressBar = item.findViewById(R.id.budgetProgressBar);

                    double used = spending.getOrDefault(b.category, 0.0);
                    double percent = (b.amount == 0) ? 0 : (used / b.amount) * 100;

                    txtCategory.setText(b.category);
                    txtUsed.setText(String.format(Locale.getDefault(), "฿%.2f", used));
                    txtLimit.setText(String.format(Locale.getDefault(), "฿%.2f", b.amount));
                    txtPercent.setText(String.format(Locale.getDefault(), "%.0f%%", percent));
                    progressBar.setProgress((int) percent);

                    layoutBudgets.addView(item);

                }
            });
        }).start();
    }

    private long[] getMonthRangeMillis() {
        Calendar start = (Calendar) currentMonth.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }
}
