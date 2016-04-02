package com.daniel;

/**
 * Created by daniel on 23/03/16.
 */

import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "forBagIt.db";
    public static final String ITEM_TABLE_NAME = "trackers";
    public static String[] macAddress = {"E9:AD:EC:47:8F:A3","DA:B4:89:69:7F:72"};
    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + ITEM_TABLE_NAME +
                        " (id integer, name text,pic text, MAC text," +
                        "sunday integer, monday integer, tuesday integer, " +
                        "wednesday integer, thursday integer, friday integer, saturday integer)"
        );

        for (int i = 0; i < 5; i++) {
            db.execSQL(
                    "INSERT INTO " + ITEM_TABLE_NAME + " (id,name,pic,MAC,sunday,monday,tuesday,wednesday,thursday,friday,saturday)\n" +
                            " VALUES (" + i + ",'device0"+i+"','pic','mac"+i+"',0,0,0,0,0,0,0)"
            );
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertDevice(int id, String name, String pic)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("pic", pic);
        db.insert(ITEM_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ITEM_TABLE_NAME+" where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, ITEM_TABLE_NAME);
        return numRows;
    }

    public boolean updateDevice (DeviceEntity deviceEntity)
    {
        Cursor cursor = getData(deviceEntity.id);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", deviceEntity.name);
        contentValues.put("id", deviceEntity.id);
        contentValues.put("pic", deviceEntity.pic);
        contentValues.put("MAC", deviceEntity.MAC);
        contentValues.put("sunday", deviceEntity.sunday);
        contentValues.put("monday",deviceEntity.monday);
        contentValues.put("tuesday", deviceEntity.tuesday);
        contentValues.put("wednesday", deviceEntity.wednesday);
        contentValues.put("thursday", deviceEntity.thursday);
        contentValues.put("friday", deviceEntity.friday);
        contentValues.put("saturday",deviceEntity.saturday);
        db.update(ITEM_TABLE_NAME, contentValues, "id = " + deviceEntity.id,null);
        return true;
    }
    public ArrayList<DeviceEntity> getDevicesPerDay(int day){
        return getAll("select * from "+ ITEM_TABLE_NAME + " where " + dayNumToString(day) + " > 0");
    }

    private String dayNumToString(int day){
        String dayStr;
        switch (day){
            case 1:
                dayStr = "sunday";
                break;
            case 2:
                dayStr = "monday";
                break;
            case 3:
                dayStr = "tuesday";
                break;
            case 4:
                dayStr = "wednesday";
                break;
            case 5:
                dayStr = "thursday";
                break;
            case 6:
                dayStr = "friday";
                break;
            default:
                dayStr = "saturday";
                break;
        }
        return dayStr;
    }
    public ArrayList<DeviceEntity> getAll(String query){
        ArrayList<DeviceEntity> array_list = new ArrayList<DeviceEntity>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor res =  db.rawQuery(query, null);
        res.moveToFirst();
        while(res.isAfterLast() == false){
            DeviceEntity deviceEntity = new DeviceEntity();
            deviceEntity.name = res.getString(res.getColumnIndex("name"));
            deviceEntity.id = res.getInt(res.getColumnIndex("id"));
            deviceEntity.pic = res.getString(res.getColumnIndex("pic"));
            deviceEntity.MAC = res.getString(res.getColumnIndex("MAC"));
            deviceEntity.sunday = res.getInt(res.getColumnIndex("sunday"));
            deviceEntity.monday = res.getInt(res.getColumnIndex("monday"));
            deviceEntity.tuesday = res.getInt(res.getColumnIndex("tuesday"));
            deviceEntity.wednesday = res.getInt(res.getColumnIndex("wednesday"));
            deviceEntity.thursday = res.getInt(res.getColumnIndex("thursday"));
            deviceEntity.friday = res.getInt(res.getColumnIndex("friday"));
            deviceEntity.saturday = res.getInt(res.getColumnIndex("saturday"));
            array_list.add(deviceEntity);
            res.moveToNext();
        }
        return array_list;
    }
}
