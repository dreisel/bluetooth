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
import android.widget.ProgressBar;
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
    ListView lv;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 3000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    ArrayList list = new ArrayList();
    ArrayAdapter madapter;
    Set<String> mySet= new HashSet<>();
    ScanResultHandler mScanCallback = new ScanResultHandler();
    private ProgressBar spinner;

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
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
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
            filters.add(new ScanFilter.Builder().setDeviceAddress("DA:B4:89:69:7F:72").build());
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
        Toast.makeText(this,"Scanning for " + SCAN_PERIOD / 1000 + " seconds" ,Toast.LENGTH_LONG).show();
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
                    mLEScanner.stopScan(mScanCallback);
                    mLEScanner.flushPendingScanResults(mScanCallback);
                    spinner.setVisibility(View.GONE);
                    populateLst();
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(filters, (new android.bluetooth.le.ScanSettings.Builder()).setScanMode(2).build(), mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }

    private void populateLst() {
        list.clear();
        Set<String> currentMACSet = new HashSet<>(Arrays.asList(DBHelper.macAddress));
        Set<String> activeMACSet = new HashSet<>();
        currentMACSet.addAll(Arrays.asList(DBHelper.macAddress));
        Map<BluetoothDevice,Integer> deviceMap = mScanCallback.getDevices();
        List<BluetoothDevice> lst = new ArrayList(mScanCallback.getDevices().keySet());
        //adding items found to the active set
        for(BluetoothDevice btd : deviceMap.keySet()) {
            int RSSI = deviceMap.get(btd);
            if(currentMACSet.contains(btd.getAddress()) && RSSI > -60) {
               // connectToDevice(btd);
                String name = mydb.getByMac(btd.getAddress()).name;
                list.add(name + " : " + RSSI);
                currentMACSet.remove(btd.getAddress());
                btd.connectGatt(this,true,null);
            }
        }
        currentMACSet.remove("test");
        //if set is not empty )
        if(currentMACSet.size() == 1){
            String name = mydb.getByMac((String)currentMACSet.toArray()[0]).name;
            Toast.makeText(getApplicationContext(),"missing: "+name,Toast.LENGTH_LONG).show();
        } else if(currentMACSet.size() > 1){
            Toast.makeText(getApplicationContext(),"missing some items",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(),"good boy",Toast.LENGTH_LONG).show();
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
}
