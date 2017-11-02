package com.example.maggs.fishapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Maggs on 31.10.2017.
 */

public class MyInfoWindow implements GoogleMap.InfoWindowAdapter {
    Context context;
    LayoutInflater inflater;
    public MyInfoWindow(Context context) {
        this.context = context;
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // R.layout.echo_info_window is a layout in my
        // res/layout folder. You can provide your own
        View v = inflater.inflate(R.layout.fragment_info, null);

        TextView title = (TextView) v.findViewById(R.id.info);
        title.setText(marker.getTitle());
        return v;
    }
}
