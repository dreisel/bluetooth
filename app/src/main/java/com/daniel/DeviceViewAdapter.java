package com.daniel;

/**
 * Created by daniel on 25/03/16.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniel.myapplication.R;

import java.util.List;

public class DeviceViewAdapter extends ArrayAdapter<DeviceEntity> {
    private Context context;
    private List<DeviceEntity> deviceEntities;

    public DeviceViewAdapter(Context context, List<DeviceEntity> deviceEntities) {
        //super(context, -1, values);
        super(context,-1,deviceEntities);
        this.context = context;
        this.deviceEntities = deviceEntities;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.nut_list_item, parent, false);
//        rowView.setOnClickListener();
        TextView textViewId = (TextView) rowView.findViewById(R.id.devicePic);
        TextView textViewName = (TextView) rowView.findViewById(R.id.deviceName);
       // ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textViewId.setText("0" + position);
        textViewName.setText(deviceEntities.get(position).name);
        // change the icon for Windows and iPhone
        return rowView;
    }
}
