package com.daniel.myapplication;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.daniel.DBHelper;
import com.daniel.DeviceEntity;
import com.daniel.DeviceViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by daniel on 25/03/16.
 */
public class DeviceListActivity extends ListActivity {
    DBHelper dbHelper = new DBHelper(this);
    List<DeviceEntity> devices;
    DeviceViewAdapter deviceViewAdapter;
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        devices = new ArrayList<DeviceEntity>();
        // use your custom layout
        deviceViewAdapter = new DeviceViewAdapter(this, devices);
        setListAdapter(deviceViewAdapter);
        refreshList();
    }
    String m_Text = "";
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final DeviceEntity item = (DeviceEntity) getListAdapter().getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                item.name = m_Text;
                dbHelper.updateDevice(item);
                refreshList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void refreshList(){
        devices.clear();
        devices.addAll(dbHelper.getAll());
        deviceViewAdapter.notifyDataSetChanged();
    }

}
