package com.example.incivisme.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivisme.MainActivity;
import com.example.incivisme.R;

import com.example.incivisme.ShareViewModel;
import com.example.incivisme.databinding.FragmentMapBinding;
import com.example.incivisme.ui.NotificationInfoWindowAdapter;
import com.example.incivisme.ui.bs.Notification;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private ShareViewModel shareViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        shareViewModel =
                new ViewModelProvider(requireActivity()).get(ShareViewModel.class);

        Log.e("map", "map1 ");

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.googleMap);


        DatabaseReference data = FirebaseDatabase.getInstance().getReference();
        DatabaseReference users = data.child("users");
        Log.e("map", "map2 ");
        shareViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            DatabaseReference uid = users.child(user.getUid());
            DatabaseReference notifications = uid.child("notifications");

            Log.e("map", "map3 ");

            mapFragment.getMapAsync(map -> {

                Log.e("map", "map4 ");

                if (ActivityCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(requireActivity(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }else {
                    map.setMyLocationEnabled(true);
                    MutableLiveData<LatLng> currentPosition = shareViewModel.getCurrentPosition();

                    Log.e("TAG", "onCreateView: " + currentPosition.getValue());
                    LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
                    currentPosition.observe(lifecycleOwner, latLng -> {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                        map.animateCamera(cameraUpdate);
                        currentPosition.removeObservers(lifecycleOwner);
                    });

                    notifications.addChildEventListener(new ChildEventListener() {

                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            Notification notification = snapshot.getValue(Notification.class);

                            LatLng position = new LatLng(
                                    Double.parseDouble(notification.getLatitude()),
                                    Double.parseDouble(notification.getLongitude())
                            );

                            NotificationInfoWindowAdapter customInfoWindow = new NotificationInfoWindowAdapter(
                                    getActivity()
                            );

                            Marker marker = map.addMarker(new MarkerOptions()
                                    .title(notification.getProblem())
                                    .position(position)
                                    .snippet(notification.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            marker.setTag(notification);
                            map.setInfoWindowAdapter(customInfoWindow);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        });




        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}