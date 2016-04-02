package com.daniel;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.view.View;
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
    Set<String> expectedResults;
    Context context;

    public void setDevices(Map<BluetoothDevice, Integer> devices) {
        this.devices = devices;
    }

    public Set<String> getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(Set<String> expectedResults) {
        this.expectedResults = expectedResults;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

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
        expectedResults.remove(result.getDevice().getAddress());
        if(expectedResults.isEmpty()){
            Toast.makeText(context,"good",Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            devices.put(result.getDevice(), result.getRssi());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }



}
