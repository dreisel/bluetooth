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
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.daniel.DBHelper;

public class MainActivity extends Activity {
    ListView lv;
    TextView text;
    private BluetoothAdapter btAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 2000;
    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    ArrayList<String> missingDevices = new ArrayList<>();
    ArrayList<String> packedDevices = new ArrayList<>();
    ArrayAdapter madapter;
    ScanResultHandler mScanCallback = new ScanResultHandler();
    String[] macAddress = {
            "D6:09:89:66:D9:DB",
            "C9:09:D5:3C:D8:CF" ,
            "D2:5F:EE:5A:AB:57" ,
            "D9:BF:6E:64:93:D7" ,
            "DA:80:A4:92:2E:91" };
    Map<String,Integer> macIndex;

    int[] rssis = new int[5];
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
        macIndex = new HashMap<String,Integer>();
        for (int i = 0; i < macAddress.length ; i++) {
            macIndex.put(macAddress[i],i);
        }

        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.subTitleText);
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
            for (int i = 0; i < macAddress.length; i++) {
                filters.add(new ScanFilter.Builder().setDeviceAddress(macAddress[i]).build());
            }
        }
    }

    private void setFilters() {
        int sum = 0;
        int avg = 0;
        int min = 0;
        int minIndex = 0;
        int count = 0;
        for (int i = 0; i < macAddress.length; i++) {
            if(rssis[i] < 0){
                count++;
                if(rssis[i] < min){
                    min = rssis[i];
                    minIndex=i;
                }
                sum += rssis[i];
            }
        }
        avg = (sum-min) / (count-1);
        if(Math.abs(avg - min) >= 12){
            text.setText("device " + (minIndex + 1) + " is missing: " + rssis[minIndex]);
            //Toast.makeText(this, "device " + (i + 1) +" is missing: " + rssis[i],Toast.LENGTH_LONG).show();
            spinner.setVisibility(View.GONE);
            throw new RuntimeException("got all rssi");
        }
//        for (int i = 0; i < macAddress.length; i++) {
//            if(rssis[i] < -75){
//                text.setText("device " + (i + 1) + " is missing: " + rssis[i]);
//                //Toast.makeText(this, "device " + (i + 1) +" is missing: " + rssis[i],Toast.LENGTH_LONG).show();
//                spinner.setVisibility(View.GONE);
//                throw new RuntimeException("got all rssi");
//            }
//            /*if(rssis[i] == 0){
//                count++;
//                filters.add(new ScanFilter.Builder().setDeviceAddress(macAddress[i]).build());
//            }*/
//        }
        /*if(count == 0){
            throw new RuntimeException("got all rssi");
        }*/
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    /*
    "C9:09:D5:3C:D8:CF" : 2,
"D2:5F:EE:5A:AB:57" : 3,
"D6:09:89:66:D9:DB" : 1,
"D9:BF:6E:64:93:D7" : 4,
"DA:80:A4:82:2E:91" : 5}
     */
    int deviceCounter;
    public void scan(View v) {
        /*
        String[] arr = {"C9:09:D5:3C:D8:CF" ,
                "D2:5F:EE:5A:AB:57" ,
                "D6:09:89:66:D9:DB" ,
                "D9:BF:6E:64:93:D7" ,
                "DA:80:A4:82:2E:91" };
        deviceCounter = 0;
        missingDevices.clear();
        packedDevices.clear();
        missingDevices.addAll(Arrays.asList(arr));
        Toast.makeText(this,Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + ", Scanning for " + SCAN_PERIOD / 1000 + " seconds" ,Toast.LENGTH_LONG).show();
        spinner.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int i =1;
                i++;
            }
        }, SCAN_PERIOD);
        connectionloop();*/
        rssis = new int[5];
        scanLeDevice();
    }
    void connectionloop(){
        if(deviceCounter >= missingDevices.size()) {
            spinner.setVisibility(View.GONE);
            return;
        }
        BluetoothDevice dev = btAdapter.getRemoteDevice(missingDevices.get(deviceCounter));
        connectToDevice(dev, true);
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

    private void scanLeDevice() {
        int count = 0;
        try{
            setFilters();
        } catch (Exception e){
            //Toast.makeText(this,"scan complete",Toast.LENGTH_LONG).show();
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanner.stopScan(mScanCallback);
                scanner.flushPendingScanResults(mScanCallback);
                scanLeDevice();
            }
        }, SCAN_PERIOD);
        spinner.setVisibility(View.VISIBLE);
        scanner.startScan(filters, (new android.bluetooth.le.ScanSettings.Builder()).setScanMode(2).build(), mScanCallback);
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
    public void connectToDevice(BluetoothDevice device, boolean connect) {
        if (connect) {
            mGatt = device.connectGatt(this, false, gattCallback);
            int j = 3;
            j++;
        } else {
            mGatt.disconnect();
            deviceCounter++;
            mGatt = null;
            connectionloop();
        }

    }
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.readRemoteRssi();
                    Log.i("gattCallback", "STATE_CONNECTED");
                   // gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    //connectionloop();
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
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
          //  Toast.makeText(getApplicationContext(),"rsssi:" + rssi,Toast.LENGTH_SHORT).show();
            connectToDevice(gatt.getDevice(),false);
        }
    };

    public class ScanResultHandler  extends ScanCallback {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int index = macIndex.get(result.getDevice().getAddress());
            rssis[index] = result.getRssi();

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                int index = macIndex.get(result.getDevice().getAddress());
                rssis[index] = result.getRssi();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }



    }
}
