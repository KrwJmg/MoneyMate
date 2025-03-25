package com.example.moneymate.ui;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.example.moneymate.data.AppDatabase;
import com.example.moneymate.data.BudgetEntity;

import java.util.Calendar;

public class SetBudgetActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextAmount;
    private Button btnSave;
    private final String[] categories = {"Food", "Transport", "Apparel", "Social Life", "Other"};
    private Calendar currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        editTextAmount = findViewById(R.id.editTextAmount);
        btnSave = findViewById(R.id.btnSaveBudget);
        currentMonth = Calendar.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveBudget());
    }

    private void saveBudget() {
        String category = spinnerCategory.getSelectedItem().toString();
        String amountStr = editTextAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int month = currentMonth.get(Calendar.MONTH);
        int year = currentMonth.get(Calendar.YEAR);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.budgetDao().deleteSingleBudget(category, month, year); // ลบทับก่อน
            db.budgetDao().insert(new BudgetEntity(category, amount, month, year));

            runOnUiThread(() -> {
                Toast.makeText(this, "Budget saved for " + category, Toast.LENGTH_SHORT).show();
                finish(); // กลับไปหน้า Budget
            });
        }).start();
    }
}
