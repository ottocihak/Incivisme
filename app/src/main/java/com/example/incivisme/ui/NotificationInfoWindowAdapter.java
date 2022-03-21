package com.example.incivisme.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.incivisme.R;
import com.example.incivisme.ui.bs.Notification;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class NotificationInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
{
    private final Activity activity;

    public NotificationInfoWindowAdapter(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        View view = activity.getLayoutInflater()
                .inflate(R.layout.info_view, null);

        Notification notification = (Notification) marker.getTag();

        ImageView markerPic = view.findViewById(R.id.marker_pic);
        TextView markerTitle = view.findViewById(R.id.marker_title);
        TextView markerDes = view.findViewById(R.id.marker_description);

        markerTitle.setText(notification.getAddress());
        markerDes.setText(notification.getProblem());

        Glide.with(activity).load(notification.getPic()).into(markerPic);

        return view;
    }
}
