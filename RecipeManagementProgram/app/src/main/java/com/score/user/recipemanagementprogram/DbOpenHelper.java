package com.score.user.recipemanagementprogram;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper
{
    private static final String DATABASE_NAME = "costDB.db";
    private static final int DATABASE_VERSION = 2;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void create(){
        mDBHelper.onCreate(mDB);
    }

    public void close(){
        mDB.close();
    }

    public long insertColumn(String name, double price, double weight, double pricePerGram){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.PRICE, price);
        values.put(DataBases.CreateDB.WEIGHT, weight);
        values.put(DataBases.CreateDB.PRICEPERGRAM, pricePerGram);
        values.put(DataBases.CreateDB.USES,"false");
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
    }
    public Cursor selectColumns(){
        return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    public boolean updateColumn(String name, double price, double weight , double pricePerGram){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.PRICE, price);
        values.put(DataBases.CreateDB.WEIGHT, weight);
        values.put(DataBases.CreateDB.PRICEPERGRAM, pricePerGram);
        values.put(DataBases.CreateDB.USES,"true");
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "name="+"'"+name+"'", null) > 0;
    }

    public boolean deleteColumn(String name){
        return mDB.delete(DataBases.CreateDB._TABLENAME, "name="+"'"+name+"'", null) > 0;
    }

}

