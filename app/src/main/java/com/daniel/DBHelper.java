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
    public static final String ITEM_COLUMN_ID = "id";
    public static final String ITEM_COLUMN_NAME = "name";
    public static final String ITEM_COLUMN_PIC = "pic";

    private HashMap hp;
    private static DBHelper sInstance;

    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + ITEM_TABLE_NAME +
                        " (id integer, name text,pic blob, " +
                        "sunday integer, monday integer, tuesday integer, " +
                        "wednesday integer, thursday integer, friday integer, saturday integer)"
        );
        if(numberOfRows() == 0){
            for (int i = 1; i <= 5; i++) {
                insertDevice(i,"device0" + i,"");
            }

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
        SQLiteDatabase db = this.getReadableDatabase();
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
        contentValues.put("mac", deviceEntity.MAC);
        contentValues.put("sunday", deviceEntity.sunday);
        contentValues.put("monday",deviceEntity.monday);
        contentValues.put("tuesday", deviceEntity.tuesday);
        contentValues.put("wednesday", deviceEntity.wednesday);
        contentValues.put("thursday", deviceEntity.thursday);
        contentValues.put("friday", deviceEntity.friday);
        contentValues.put("saturday",deviceEntity.saturday);
        db.update(ITEM_TABLE_NAME, contentValues, "id = ? ", new String[]{String.valueOf(deviceEntity.id) } );
        return true;
    }

    public ArrayList<DeviceEntity> getAll(){
        ArrayList<DeviceEntity> array_list = new ArrayList<DeviceEntity>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + ITEM_TABLE_NAME, null);
        res.moveToFirst();
        while(res.isAfterLast() == false){
            DeviceEntity deviceEntity = new DeviceEntity();
            deviceEntity.name = res.getString(res.getColumnIndex("name"));
            deviceEntity.id = res.getInt(res.getColumnIndex("name"));
            deviceEntity.pic = res.getString(res.getColumnIndex("name"));
            deviceEntity.MAC = res.getString(res.getColumnIndex("name"));
            deviceEntity.sunday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.monday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.tuesday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.wednesday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.thursday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.friday = res.getInt(res.getColumnIndex("name"));
            deviceEntity.saturday = res.getInt(res.getColumnIndex("name"));
            array_list.add(deviceEntity);
            res.moveToNext();
        }
        return array_list;
    }
}
