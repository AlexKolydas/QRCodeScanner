package com.learntodroid.androidqrcodescanner;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";

    DatabaseHelper mDatabaseHelper;
    private ListView mListView;
    private Button cleanDbButton;
    float totalPrice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        mListView = (ListView) findViewById(R.id.listView);
        cleanDbButton = (Button) findViewById(R.id.cleanDb);
        mDatabaseHelper = new DatabaseHelper(this);
        populateListView();
    }

    private void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        String fullProductDetails;
        //get the data and append to a list
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            //get the value from the database in column 1
            //then add it to the ArrayList
            totalPrice=Float.parseFloat(data.getString(2))*Float.parseFloat(data.getString(3));
            fullProductDetails = "Name: "+data.getString(1) + "| Price: " + data.getString(2) + "| Quantity: " + data.getString(3);
            listData.add(fullProductDetails+" Total Price: "+String.format("%.02f",totalPrice));
        }
        //create the list adapter and set the adapter
        ListAdapter  adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listData) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 1) {
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                }
                return view;
            }
        };
        mListView.setAdapter(adapter);

        //Clear all data from Database
        cleanDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseHelper.cleanDatabase();
                toastMessage("All products removed!");
                MainActivity.totalamount.setText("0");
                Intent switchActivityIntent = new Intent(ListDataActivity.this, MainActivity.class);
                startActivity(switchActivityIntent);
            }
        });
    }

    /**
     * customizable toast
     *
     * @param message
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}