package com.example.cocaro;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity  {
    ListView lv_info;
    SQLiteDatabase sqlitedb ;
    ArrayList<String> list_value;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        sqlitedb = openOrCreateDatabase("ResultHistory.db", MODE_PRIVATE, null);
        lv_info= findViewById(R.id.lv_infoHistory);
        list_value = new ArrayList<>();
        adapter = new ArrayAdapter<>(HistoryActivity.this, R.layout.color_listview, list_value);
        lv_info.setAdapter(adapter);
        list_value.clear();
        Cursor cursor = sqlitedb.query("tblhistory", null, null,null, null , null, null);
        cursor.moveToFirst();
        String data = "";
        while(cursor.isAfterLast() == false){
            int number = Integer.parseInt(cursor.getString(0));
            if(number <= 9)
                data = "   " + cursor.getString(0 ) + "                                 "  + cursor.getString(1) + "                      " + cursor.getString(2);
            else
                data = "   " +cursor.getString(0 ) + "                               "  + cursor.getString(1) + "                      " + cursor.getString(2);

            cursor.moveToNext();
            list_value.add(data);
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        Button backToMain = findViewById(R.id.btnback);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}