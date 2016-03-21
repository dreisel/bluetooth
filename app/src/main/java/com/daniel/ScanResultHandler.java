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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 21/03/16.
 */
public class ScanResultHandler  extends ScanCallback {
    Set<BluetoothDevice> devices;

    public ScanResultHandler() {
        devices = new HashSet<>();
    }

    public ScanResultHandler(Set<BluetoothDevice> devices) {
        this.devices = devices;
    }

    public void clear(){
        devices.clear();
    }
    public Set<BluetoothDevice> getDevices(){
        return devices;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        devices.add(result.getDevice());
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        //madapter.notifyDataSetChanged();
        //BluetoothDevice btDevice = result.getDevice();
        //connectToDevice(btDevice);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            devices.add(sr.getDevice());
            Log.i("ScanResult - Results", sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

}
