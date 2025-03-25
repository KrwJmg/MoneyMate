package com.example.moneymate.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.adapter.TransactionAdapter;
import com.example.moneymate.data.AppDatabase;
import com.example.moneymate.data.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyFragment extends Fragment {

    private TextView txtTotalIncome, txtTotalExpense, txtTotalBalance, txtCurrentDate;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<TransactionEntity> transactionList;
    private Calendar currentDate;

    public DailyFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

        // Bind views
        txtTotalIncome = view.findViewById(R.id.txtTotalIncome);
        txtTotalExpense = view.findViewById(R.id.txtTotalExpense);
        txtTotalBalance = view.findViewById(R.id.txtTotalBalance);
        txtCurrentDate = view.findViewById(R.id.txtCurrentDate);
        recyclerView = view.findViewById(R.id.recyclerView);
        ImageButton btnPrev = view.findViewById(R.id.btnPrev);
        ImageButton btnNext = view.findViewById(R.id.btnNext);

        // Setup RecyclerView
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Set current date to today
        currentDate = Calendar.getInstance();

        btnPrev.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            loadTransactionsForSelectedDate();
            Log.d("DATE_CHANGE", "New Date: " + currentDate.getTime());
        });

        btnNext.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            loadTransactionsForSelectedDate();
            Log.d("DATE_CHANGE", "New Date: " + currentDate.getTime());
        });

        loadTransactionsForSelectedDate();

        return view;


    }



    private void loadTransactionsForSelectedDate() {
        new Thread(() -> {
            try {
                long[] dateRange = getSelectedDateRangeMillis();

                List<TransactionEntity> all = AppDatabase.getInstance(requireContext())
                        .transactionDao()
                        .getAllTransactions();

                List<TransactionEntity> filtered = new ArrayList<>();
                double income = 0, expense = 0;

                for (TransactionEntity t : all) {
                    if (t.date >= dateRange[0] && t.date < dateRange[1]) {
                        filtered.add(t);
                        if ("income".equals(t.type)) income += t.amount;
                        else if ("expense".equals(t.type)) expense += t.amount;
                    }
                }

                double total = income - expense;
                String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(currentDate.getTime());

                final double incomeFinal = income;
                final double expenseFinal = expense;
                final double totalFinal = total;



                requireActivity().runOnUiThread(() -> {
                    txtCurrentDate.setText(formattedDate);

                    transactionList.clear();
                    transactionList.addAll(filtered);
                    adapter.notifyDataSetChanged();

                    // ใช้ตัวแปร final ที่ประกาศไว้
                    txtTotalIncome.setText(String.format("+ %.2f", incomeFinal));
                    txtTotalExpense.setText(String.format("- %.2f", expenseFinal));
                    txtTotalBalance.setText(String.format(Locale.getDefault(), "%.2f", totalFinal));
                });


            } catch (Exception e) {
                Log.e("DailyFragment", "Error loading transactions: " + e.getMessage(), e);
            }
        }).start();
    }

    private long[] getSelectedDateRangeMillis() {
        Calendar start = (Calendar) currentDate.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }
}
