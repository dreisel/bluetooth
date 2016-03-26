package com.daniel.myapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.daniel.DBHelper;
import com.daniel.DeviceEntity;
import com.daniel.DeviceViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 25/03/16.
 */
public class DeviceListActivity extends ListActivity {
    DBHelper dbHelper = DBHelper.getInstance(this);
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // use your custom layout
        DeviceViewAdapter deviceViewAdapter = new DeviceViewAdapter(this, dbHelper.getAll());
        setListAdapter(deviceViewAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DeviceEntity item = (DeviceEntity) getListAdapter().getItem(position);
        Toast.makeText(this,"id: "+item.id,Toast.LENGTH_LONG);
    }

}
