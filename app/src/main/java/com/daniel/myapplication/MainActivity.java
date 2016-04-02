package com.daniel.myapplication;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.daniel.DBHelper;
import com.daniel.MyTestService;
import com.daniel.ScanResultHandler;

import java.util.Map;
import java.util.Set;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;

public class MainActivity extends Activity {
    Button b1, b2, b4;
    ListView lv;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 20000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    ArrayList list = new ArrayList();
    ArrayAdapter madapter;
    Set<String> mySet= new HashSet<>();
    ScanResultHandler mScanCallback = new ScanResultHandler();

    DBHelper mydb;

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MyTestService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.deviceList);
        madapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, list);
        mHandler = new Handler();
        lv.setAdapter(madapter);
        mydb = new DBHelper(this);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            // filters.add(new ScanFilter.Builder().setDeviceName("nut").build());
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */

    public void scan(View v) {
        list.clear();
        mScanCallback.clear();
        mScanCallback.setContext(this);
        mScanCallback.setExpectedResults(new HashSet<String>(Arrays.asList(DBHelper.macAddress)));


        Toast.makeText(this,"Scanning for "+SCAN_PERIOD / 1000 +" seconds",Toast.LENGTH_LONG).show();
        scanLeDevice(true);
    }

    public void edit(View v) {
        Intent i = new Intent(getApplicationContext(), DeviceListActivity.class);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    mLEScanner.flushPendingScanResults(mScanCallback);
                    populateLst();
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(null, (new android.bluetooth.le.ScanSettings.Builder()).setScanMode(2).build(), mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private void populateLst() {
        list.clear();
        Set<String> currentMACSet = new HashSet<>();
        Set<String> activeMACSet = new HashSet<>();
        currentMACSet.addAll(Arrays.asList(DBHelper.macAddress));
        Map<BluetoothDevice,Integer> deviceMap = mScanCallback.getDevices();
        List<BluetoothDevice> lst = new ArrayList(mScanCallback.getDevices().keySet());
        //adding items found to the active set
        for(BluetoothDevice btd : lst) {
            list.add(btd.getName() + " : " + btd.getAddress());
            activeMACSet.add(btd.getAddress());

        }
        //removing all items found from the expected set
        currentMACSet.removeAll(activeMACSet);
        //if set is not empty )
        if(currentMACSet.size() > 1){
            Toast.makeText(getApplicationContext(),"missing something",Toast.LENGTH_LONG).show();
        }
        madapter.notifyDataSetChanged();
    }
}
