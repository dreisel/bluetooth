package com.daniel.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.daniel.DBHelper;

public class MainActivity extends Activity {
    ListView lv;
    private BluetoothAdapter btAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 15000;
    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    ArrayList<String> missingDevices = new ArrayList<>();
    ArrayList<String> packedDevices = new ArrayList<>();
    ArrayAdapter madapter;
    ScanResultHandler mScanCallback = new ScanResultHandler();
    private ProgressBar spinner;
    DBHelper mydb;
    boolean test = false;
    public void listDevices(View view){
        if(!isMyServiceRunning(TaskSchedulerService.class)) {
            test = !test;
            startService(new Intent(getBaseContext(), TaskSchedulerService.class));
        } else
            stopService(new Intent(getBaseContext(), TaskSchedulerService.class));
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        lv = (ListView) findViewById(R.id.deviceList);
        madapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, packedDevices);
        mHandler = new Handler();
        lv.setAdapter(madapter);
        mydb = new DBHelper(this);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            scanner = btAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            filters.add(new ScanFilter.Builder().setDeviceAddress("DA:B4:89:69:7F:72").build());
            filters.add(new ScanFilter.Builder().setDeviceAddress("E9:AD:EC:47:8F:A3").build());
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */

    public void scan(View v) {
        missingDevices.clear();
        packedDevices.clear();
        madapter.notifyDataSetChanged();
        missingDevices.addAll(mydb.getDevicesPerDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
        Toast.makeText(this,Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + ", Scanning for " + SCAN_PERIOD / 1000 + " seconds" ,Toast.LENGTH_LONG).show();
        spinner.setVisibility(View.VISIBLE);
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
                    scanner.flushPendingScanResults(mScanCallback);
                    scanner.stopScan(mScanCallback);
                    spinner.setVisibility(View.GONE);
                    populateLst();
                }
            }, SCAN_PERIOD);
            scanner.startScan(filters, (new android.bluetooth.le.ScanSettings.Builder()).setScanMode(2).build(), mScanCallback);
        } else {
            scanner.stopScan(mScanCallback);
            populateLst();
        }
    }

    private void populateLst() {
        packedDevices.clear();
        ArrayList<String> devices = mydb.getDevicesPerDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        devices.removeAll(missingDevices);
        //adding items found to the active set
        for(String mac : devices) {
            String name = mydb.getDeviceByMac(mac).name;
            packedDevices.add(name);
        }
        madapter.notifyDataSetChanged();
    }
    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                   // gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

    public class ScanResultHandler  extends ScanCallback {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(0 - result.getRssi() < 100 ) {
                Toast.makeText(getApplicationContext(), result.getDevice().getAddress(), Toast.LENGTH_SHORT).show();
                missingDevices.remove(result.getDevice().getAddress());
                if (missingDevices.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_LONG).show();
                    scanLeDevice(false);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                if(0 - result.getRssi() < 100 ) {
                    missingDevices.remove(result.getDevice().getAddress());
                    if (missingDevices.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "good", Toast.LENGTH_LONG);
                        scanLeDevice(false);
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }



    }
}
