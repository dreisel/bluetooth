package com.daniel;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.daniel.myapplication.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by daniel on 21/03/16.
 */
public class ScanResultHandler  extends ScanCallback {
    Map<BluetoothDevice,Integer> devices;


    public ScanResultHandler() {
        devices = new HashMap<>();
    }

    public void clear(){
        devices.clear();
    }
    public Map<BluetoothDevice,Integer> getDevices(){
        return devices;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        devices.put(result.getDevice(), result.getRssi());
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        //madapter.notifyDataSetChanged();
        //BluetoothDevice btDevice = result.getDevice();
        //connectToDevice(btDevice);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            devices.put(sr.getDevice(), sr.getRssi());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

}
