package com.example.moneymate.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.example.moneymate.data.AppDatabase;
import com.example.moneymate.data.TransactionEntity;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText editDate, editAmount, editAsset, editNote;
    private Spinner spinnerCategory;
    private RadioGroup radioGroupType;
    private Button btnSave;

    private long selectedTimestamp = System.currentTimeMillis(); // Default: now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Bind Views
        editDate = findViewById(R.id.editDate);
        editAmount = findViewById(R.id.editAmount);

        editNote = findViewById(R.id.editNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        radioGroupType = findViewById(R.id.radioGroupType);
        btnSave = findViewById(R.id.btnSave);

        // Set up Date Picker
        editDate.setOnClickListener(v -> showDatePicker());

        // Set up Spinner Categories
        String[] categories = {"Food", "Transport", "Salary", "Apparel","Social Life","Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_dark,
                categories
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_dark);
        spinnerCategory.setAdapter(adapter);

        // Save Button
        btnSave.setOnClickListener(v -> saveTransaction());

    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                new ContextThemeWrapper(this, R.style.SpotifyDatePickerDialog),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedTimestamp = calendar.getTimeInMillis();
                    editDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveTransaction() {
        String type = "expense"; // Default
        int checkedId = radioGroupType.getCheckedRadioButtonId();
        if (checkedId == R.id.radioIncome) type = "income";

        String amountStr = editAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        String note = editNote.getText().toString().trim();  // note from editText

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter the value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (note.isEmpty()) {
            // If note is empty, you can choose to set a default value or let it be blank
            note = "No notes provided"; // Default value for note
        }

        Log.d("AddTransaction", "Timestamp to save: " + selectedTimestamp);

        double amount = Double.parseDouble(amountStr);

        TransactionEntity entity = new TransactionEntity();
        entity.type = type;
        entity.amount = amount;
        entity.category = category;
        entity.note = note;  // Updated note handling
        entity.date = selectedTimestamp;

        new Thread(() -> {
            AppDatabase.getInstance(this).transactionDao().insert(entity);
            runOnUiThread(() -> {
                Toast.makeText(this, "Recorded successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
