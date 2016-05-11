package com.daniel.myapplication;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by daniel on 13/04/16.
 */

public class TaskSchedulerService extends IntentService {



    public TaskSchedulerService() {
        super("taskScheduler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        for (int i = 0; i < 10; i++) {
            try {
                notify("item " + i);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    NotificationManager notificationManager;
    Notification notification;
    Intent notificationIntent;
    PendingIntent contentIntent;

    @Override
    public void onCreate(){
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int icon = R.drawable.abc;
        CharSequence notiText = "Your notification from the service";
        long meow = System.currentTimeMillis();
         notification = new Notification(icon, notiText, meow);
         notificationIntent = new Intent(this, MainActivity.class);
         contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    }
    public void notify(CharSequence txt){


        Context context = getApplicationContext();
        CharSequence contentTitle = "missing something";
        CharSequence contentText = txt;
        Bitmap aBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bag);
        Notification noti = new Notification.Builder(context)
                .setContentTitle("New mail from " + "me")
                .setContentText("hi thi s isis " + txt)
                .setSmallIcon(R.drawable.abc)
                .setLargeIcon(aBitmap)
                .build();
        noti.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        int SERVER_DATA_RECEIVED = 1;
        notificationManager.notify(SERVER_DATA_RECEIVED, noti);
    }
}
