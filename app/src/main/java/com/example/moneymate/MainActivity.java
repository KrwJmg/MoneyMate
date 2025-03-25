package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moneymate.ui.AddTransactionActivity;
import com.example.moneymate.ui.BudgetFragment;
import com.example.moneymate.ui.DailyFragment;
import com.example.moneymate.ui.StatsFragment; // <== เพิ่ม Fragment ที่มี
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Load Fragment แรกตอนเข้าแอป
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DailyFragment())
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fab สำหรับเพิ่มรายการ
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        // ✅ จัดการการเปลี่ยน Fragment จากเมนูล่าง
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_daily) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DailyFragment())
                        .commit();
                return true;

            } else if (itemId == R.id.nav_stats) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatsFragment())
                        .commit();
                return true;

            } else if (itemId == R.id.nav_budget ) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BudgetFragment()) // ใช้ StatsFragment ชั่วคราว
                        .commit();
                return true;
            }

            return false;
        });
    }
}
