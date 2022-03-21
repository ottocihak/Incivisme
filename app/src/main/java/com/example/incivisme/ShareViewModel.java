package com.example.incivisme;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShareViewModel extends AndroidViewModel {

    private final Application app;
    private final MutableLiveData<String> currentAddress = new MutableLiveData<>();
    private final MutableLiveData<String> checkPermission = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressBar = new MutableLiveData<>();
    private final MutableLiveData<LatLng> currentPosition = new MutableLiveData<>();
    private MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private MutableLiveData<DatabaseReference> notificationRef = new MutableLiveData<>();

    private boolean trackingLocation;
    FusedLocationProviderClient fusedLocationClient;

    public ShareViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
        Log.e("sharedView", "ShareViewModel ");
        this.app = application;
    }

    public void setFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.fusedLocationClient = mFusedLocationClient;
    }

    public LiveData<String> getCurrentAddress() {
        return currentAddress;
    }

    public MutableLiveData<Boolean> getProgressBar() {
        return progressBar;
    }

    public LiveData<String> getCheckPermission() {
        return checkPermission;
    }

    public MutableLiveData<LatLng> getCurrentPosition () {return currentPosition;}

    public LiveData<FirebaseUser> getCurrentUser () {
        if (currentUser == null){
            currentUser = new MutableLiveData<>();
        }
        Log.e("sharedView", "ShareViewModel "+currentUser.getValue());
        return currentUser;
    }

    public LiveData<DatabaseReference> getNotificationRef () {return notificationRef;}

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser.postValue(currentUser);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (trackingLocation) {
                new FetchAddressTask(
                        getApplication().getApplicationContext()
                ).execute(
                        locationResult.getLastLocation()
                );
            }
        }
    };

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void switchTrackingLocation() {
        if (!trackingLocation) {
            startTrackingLocation(true);
        } else {
            stopTrackingLocation();
        }

    }

    @SuppressLint("MissingPermission")
    void startTrackingLocation(boolean needsChecking) {
        if (needsChecking) {
            checkPermission.postValue("check");
        } else {
            fusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    mLocationCallback,
                    null
            );

            currentAddress.postValue("Loading...");

            progressBar.postValue(true);
            trackingLocation = true;
        }
    }


    private void stopTrackingLocation() {
        if (trackingLocation) {
            fusedLocationClient.removeLocationUpdates (mLocationCallback);
            trackingLocation = false;
            progressBar.postValue(false);
        }
    }

    private class FetchAddressTask extends AsyncTask<Location, Void, String> {
        private final String TAG = FetchAddressTask.class.getSimpleName();
        private Context myContext;

        FetchAddressTask(Context applicationContext) {
            myContext = applicationContext;
        }

        @Override
        protected String doInBackground(Location... locations) {
            Log.e("doIt", "do it ");
            Geocoder geocoder = new Geocoder(myContext,
                    Locale.getDefault());
            Location location = locations[0];

            List<Address> addresses = null;
            String resultMessage = "";

            try {
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                currentPosition.postValue(latLng);
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);

                if (addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "There was no address found";
                        Log.e(TAG, resultMessage);
                    }
                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressParts.add(address.getAddressLine(i));
                    }

                    resultMessage = TextUtils.join("\n", addressParts);
                }
            } catch (IOException ioException) {
                resultMessage = "Service not available";
                Log.e(TAG, resultMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordinates no valid";
                Log.e(TAG, resultMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(),
                        illegalArgumentException);
            }
            return resultMessage;
        }

        @Override
        protected void onPostExecute(String address) {
            super.onPostExecute(address);
            currentAddress.postValue(address);
            progressBar.postValue(false);
        }
    }

}
