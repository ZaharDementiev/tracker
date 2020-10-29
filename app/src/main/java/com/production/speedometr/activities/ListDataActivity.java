package com.production.speedometr.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.production.speedometr.R;
import com.production.speedometr.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ListDataActivity extends AppCompatActivity {

    ListView listView;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        listView = findViewById(R.id.listView);
        databaseHelper = new DatabaseHelper(this);

        populateListView();
    }

    private void populateListView() {
        Cursor cursor = databaseHelper.getData();
        List<String> listData = new ArrayList<>();
        while (cursor.moveToNext()) {
            listData.add(cursor.getString(1) + " " + cursor.getString(2));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
    }
}