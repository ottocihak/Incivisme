package com.example.incivisme.ui.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivisme.R;
import com.example.incivisme.databinding.FragmentNotificationsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    TextView locationTextView;
    ProgressBar loadingBar;
    boolean isTracking;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        locationCallback = new LocationCallback () {
            @Override
            public void onLocationResult (LocationResult lr) {
                if (isTracking){
                    new FetchAddressTask(requireContext()).execute(lr.getLastLocation());
                }
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        loadingBar = binding.loading;
        locationTextView = binding.location;

        binding.buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTracking) {
                    startTrackingLocation();
                } else stopTrackingLocation();
            }
        });



        return root;
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            Log.d("-----------------------", "getLocation: ");
            mFusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    locationCallback,
                    null
            );
            loadingBar.setVisibility(ProgressBar.VISIBLE);
            isTracking = true;
            binding.buttonLocation.setText("Stop location tracking");
        }
    }

    private void stopTrackingLocation() {
        if (isTracking) {
            mFusedLocationClient.removeLocationUpdates (locationCallback);
            loadingBar.setVisibility(ProgressBar.INVISIBLE);
            isTracking = false;
            binding.buttonLocation.setText("Start location tracking");
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private class FetchAddressTask extends AsyncTask<Location, Void, String> {
        private final String TAG = FetchAddressTask.class.getSimpleName();
        private Context myAppContext;

        FetchAddressTask(Context applicationContext) {
            myAppContext = applicationContext;
        }

        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(myAppContext,
                    Locale.getDefault());
            Location location = locations[0];
            List<Address> addresses = null;
            String resultMessage = "";
            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);
            }catch (IOException ioException) {
                resultMessage = "service not available";
                Log.e(TAG, resultMessage, ioException);
            }catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordinates not available";
                Log.e(TAG, resultMessage + "." +
                        "\nLatitude = " + location.getLatitude() +
                        "\nLongitude = " + location.getLongitude(),
                        illegalArgumentException);
            }

            if (addresses == null || addresses.size() == 0) {
                if (resultMessage.isEmpty()) {
                    resultMessage = "Address not found";
                    Log.e(TAG, resultMessage);
                }
            }else {
                Address address = addresses.get(0);
                ArrayList<String> addressParts = new ArrayList<>();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }

                resultMessage = TextUtils.join("\n", addressParts);
            }

            return resultMessage;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            locationTextView.setText(getString((R.string.address_text),
                    s, System.currentTimeMillis()));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(getContext(),
                            "Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}