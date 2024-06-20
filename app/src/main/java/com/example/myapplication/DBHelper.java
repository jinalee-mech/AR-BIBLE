package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, authority TEXT, name TEXT, storeName TEXT, authCode TEXT)");
        db.execSQL("CREATE TABLE stores (id INTEGER PRIMARY KEY AUTOINCREMENT, storeName TEXT, authCode TEXT)");
        db.execSQL("CREATE TABLE substitute_requests (id INTEGER PRIMARY KEY AUTOINCREMENT, requester TEXT, date TEXT, scheduleType TEXT, status TEXT, acceptor TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS stores");
        db.execSQL("DROP TABLE IF EXISTS substitute_requests");
        onCreate(db);
    }

    // 대타 요청을 추가하는 메서드
    public void addSubstituteRequest(String requester, String date, String scheduleType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("requester", requester);
        values.put("date", date);
        values.put("scheduleType", scheduleType);
        values.put("status", "requested");
        values.put("acceptor", "");
        db.insert("substitute_requests", null, values);
    }

    // 대타 요청을 가져오는 메서드
    public Cursor getSubstituteRequests() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM substitute_requests WHERE status = 'requested'", null);
    }

    // 대타 요청을 수락하는 메서드
    public void acceptSubstituteRequest(int id, String acceptor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "accepted");
        values.put("acceptor", acceptor);
        db.update("substitute_requests", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean storeCodeExists(String authCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stores WHERE authCode = ?", new String[]{authCode});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean storeNameExists(String storeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stores WHERE storeName = ?", new String[]{storeName});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public String findStoreNameByCode(String authCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT storeName FROM stores WHERE authCode = ?", new String[]{authCode});
        if (cursor.moveToFirst()) {
            String storeName = cursor.getString(0);
            cursor.close();
            return storeName;
        } else {
            cursor.close();
            return null;
        }
    }

    public void addStore(String storeName, String authCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("storeName", storeName);
        values.put("authCode", authCode);
        db.insert("stores", null, values);
    }

    public boolean addUser(String username, String password, String authority, String name, String storeName, String authCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("authority", authority);
        values.put("name", name);
        values.put("storeName", storeName);
        values.put("authCode", authCode);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean isValidUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public String getUserName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        } else {
            cursor.close();
            return null;
        }
    }
}
