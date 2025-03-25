package com.example.moneymate.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;       // income / expense
    public String category;   // เช่น Food, Transport
    public String asset;      // เช่น Bank, Wallet
    public String note;       // คำอธิบาย
    public double amount;

    @ColumnInfo(name = "timestamp")
    public long date;         // timestamp (ใช้ชื่อใน DB ว่า timestamp)
}

