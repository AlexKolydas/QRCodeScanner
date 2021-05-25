package com.learntodroid.androidqrcodescanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG="DatabaseHelper";

    private static final String TABLE_NAME="products";
    private static final String id="ID";
    private static final String productName="Name";
    private static final String productPrice="Price";
    private static final String productQuantity="Quantity";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                productName +" TEXT, " +productPrice +" TEXT, " +productQuantity +" TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    public boolean addData(String name,String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(productPrice, price);
        contentValues.put(productName, name);
        long result = 0,updateDbValues;

        Cursor data = fetchProduct(name);

        if(data.getCount()==0){
            contentValues.put(productQuantity, 1);
            result = db.insert(TABLE_NAME, null, contentValues);
        }else{
            updateProductDetails(name,"quantity");
            updateProductDetails(name,price);

            contentValues.put(productQuantity, getProductDetails(name,"quantity"));
            contentValues.put(productPrice, getProductDetails(name,"price"));

            updateDbValues = db.update(TABLE_NAME,contentValues,"Name = ?",new String[]{name});

        }

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns quantity data from database
     * @return
     */
    public String getProductDetails(String name,String item){
        String dbValue="";
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + productName + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        if(item.equals("quantity")) {
            while (data.moveToNext()) {
                dbValue = data.getString(3);
            }
        }else{
            while (data.moveToNext()) {
                dbValue = data.getString(2);
            }
        }
        return dbValue;
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param name
     * @return
     */
    public Cursor fetchProduct(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT *" + " FROM " + TABLE_NAME +
                " WHERE " + productName + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemID(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT *" + " FROM " + TABLE_NAME +
                " WHERE " + productPrice + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Updates the name field
     * @param newName
     * @param id
     * @param oldName
     */
    public void updateName(String newName, int id, String oldName){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + productPrice +
                " = '" + newName + "' WHERE " + productName + " = '" + id + "'" +
                " AND " + productPrice + " = '" + oldName + "'";
        Log.d(TAG, "updateName: query: " + query);
        Log.d(TAG, "updateName: Setting name to " + newName);
        db.execSQL(query);
    }

    public void updateProductDetails(String name,String item){
        String num= getProductDetails(name,item);
        if(item.equals("quantity")) {
            int updatedValue = Integer.parseInt(num) + 1;
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productQuantity +
                    " = '" + updatedValue + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        }else{
            double updatedValue = Double.parseDouble(num) + Double.parseDouble(item);
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_NAME + " SET " + productPrice +
                    " = '" + updatedValue + "' WHERE " + productName + " = '" + name + "'";
            db.execSQL(query);
        }
    }

    /**
     * Delete from database
     * @param id
     * @param name
     */
    public void deleteName(int id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + productName + " = '" + id + "'" +
                " AND " + productPrice + " = '" + name + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + name + " from database.");
        db.execSQL(query);
    }
}


