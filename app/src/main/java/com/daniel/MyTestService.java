package com.daniel;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by daniel on 22/03/16.
 */
public class MyTestService extends IntentService {
    Context context;
    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        context = getApplicationContext();
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(context,"hi there",Toast.LENGTH_LONG);
    }
}