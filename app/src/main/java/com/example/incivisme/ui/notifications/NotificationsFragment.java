package com.example.incivisme.ui.notifications;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivisme.R;
import com.example.incivisme.ShareViewModel;
import com.example.incivisme.databinding.FragmentNotificationsBinding;
import com.example.incivisme.ui.bs.Notification;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationsFragment extends Fragment {

    private ShareViewModel shareViewModel;
    private FragmentNotificationsBinding binding;
    private ProgressBar loadingBar;
    private TextInputEditText notifyLat;
    private TextInputEditText notifyLon;
    private TextInputEditText notifyAddress;
    private TextInputEditText notifyDescription;
    private Button button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shareViewModel =
                new ViewModelProvider(requireActivity()).get(ShareViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        shareViewModel.setFusedLocationClient(LocationServices.getFusedLocationProviderClient(requireContext()));

        button = binding.notifyButton;
        loadingBar = binding.loading;
        notifyLat = binding.notificationFill1;
        notifyLon = binding.notificationFill2;
        notifyAddress = binding.notificationFill3;
        notifyDescription = binding.notificationFill4;

        shareViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            notifyAddress.setText(getString(R.string.address_text, address, System.currentTimeMillis()));
        });

        shareViewModel.getCurrentPosition().observe(getViewLifecycleOwner(), latLng -> {
            notifyLat.setText(String.valueOf(latLng.latitude));
            notifyLon.setText(String.valueOf(latLng.longitude));
        });

        shareViewModel.getProgressBar().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible){
                loadingBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                loadingBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });

        shareViewModel.switchTrackingLocation();

        button.setOnClickListener(buttonClicked ->{
            Notification notification = new Notification();
            notification.setLatitude(notifyLat.getText().toString());
            notification.setLongitude(notifyLon.getText().toString());
            notification.setAddress(notifyAddress.getText().toString());
            notification.setProblem(notifyDescription.getText().toString());

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference data = FirebaseDatabase.getInstance().getReference();

            DatabaseReference users = data.child("users");
            DatabaseReference uid = users.child(firebaseAuth.getUid());
            DatabaseReference notify = uid.child("notifications");

            DatabaseReference reference = notify.push();
            reference.setValue(notify);

            Toast.makeText(getContext(), "Notified successfully", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DatabaseReference data = FirebaseDatabase.getInstance().getReference();
        DatabaseReference users = data.child("users");

        shareViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user ->{
            DatabaseReference uid = users.child(user.getUid());
            DatabaseReference notify = uid.child("notifications");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}