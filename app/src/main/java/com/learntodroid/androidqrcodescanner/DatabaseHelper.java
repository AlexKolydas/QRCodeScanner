package com.learntodroid.androidqrcodescanner;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "products";
    private static final String id = "ID";
    private static final String productName = "Name";
    private static final String productPrice = "Price";
    private static final String productQuantity = "Quantity";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                productName + " TEXT, " + productPrice + " TEXT, " + productQuantity + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //context.deleteDatabase(TABLE_NAME);
    }

    public boolean addData(String name, String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(productPrice, price);
        contentValues.put(productName, name);
        long result = 0, updateDbValues;

        Cursor data = fetchProduct(name);

        if (data.getCount() == 0) {
            contentValues.put(productQuantity, 1);
            result = db.insert(TABLE_NAME, null, contentValues);
        } else {
            updateProductDetails(name, "quantity", true);
            updateProductDetails(name, price, true);
            Float prodPrice = Float.valueOf(getProductDetails(name, "price"));
            String strFinalPrice= String.format("%.02f",prodPrice); //need to make float with 2 decimals and put it in DB
            Float finalPrice=Float.valueOf(strFinalPrice);
            Log.i(DatabaseHelper.class.getSimpleName(), "PRICE IS: " + finalPrice);

            contentValues.put(productQuantity, getProductDetails(name, "quantity"));
            //contentValues.put(productPrice, finalPrice);

            updateDbValues = db.update(TABLE_NAME, contentValues, "Name = ?", new String[]{name});
        }
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean removeData(String name, String price) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(productPrice, price);
        contentValues.put(productName, name);
        long result = 0, updateDbValues;
        int quantity;
        Cursor data = fetchProduct(name);
        quantity = Integer.parseInt(getProductDetails(name, "quantity"));
        if (quantity == 1) {
            result = db.delete(TABLE_NAME, "Name = ?", new String[]{name});
        } else if (quantity > 1) {
            updateProductDetails(name, "quantity", false);
            //updateProductDetails(name, price, false);

            contentValues.put(productQuantity, getProductDetails(name, "quantity"));
            //contentValues.put(productPrice, getProductDetails(name, "price"));

            updateDbValues = db.update(TABLE_NAME, contentValues, "Name = ?", new String[]{name});
        }
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     *
     * @return
     */
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns quantity data from database
     *
     * @return
     */
    public String getProductDetails(String name, String item) {
        String dbValue = "";
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + productName + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        if (item.equals("quantity")) {
            while (data.moveToNext()) {
                dbValue = data.getString(3);
            }
        } else {
            while (data.moveToNext()) {
                dbValue = data.getString(2);
            }
        }
        return dbValue;
    }

    /**
     * Returns only the ID that matches the name passed in
     *
     * @param name
     * @return
     */
    public Cursor fetchProduct(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT *" + " FROM " + TABLE_NAME +
                " WHERE " + productName + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updateProductDetails(String name, String item, Boolean addedValue) {
        int updatedQuantity = 0;
        double updatedPrice = 0;

        String num = getProductDetails(name, item);

        if (item.equals("quantity") && addedValue) {
            updatedQuantity = Integer.parseInt(num) + 1;
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productQuantity +
                    " = '" + updatedQuantity + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        } else if (!item.equals("quantity") && addedValue) {
            updatedPrice = Double.parseDouble(num) + Double.parseDouble(item);
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productPrice +
                    " = '" + updatedPrice + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        }

        if (item.equals("quantity") && !addedValue) {
            updatedQuantity = Integer.parseInt(num) - 1;
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productQuantity +
                    " = '" + updatedQuantity + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        } else if (!item.equals("quantity") && !addedValue) {
            updatedPrice = Double.parseDouble(num) - Double.parseDouble(item);
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productPrice +
                    " = '" + updatedPrice + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        }
    }


    public void cleanDatabase() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "DELETE FROM " + TABLE_NAME;
            db.execSQL(query);
        } catch (Exception e) {
            toastMessage("Something wrong happened.Could not clean database");
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}


