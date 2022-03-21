package com.example.incivisme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.incivisme.ui.list.ListNotificationFragment;
import com.example.incivisme.ui.map.MapFragment;
import com.example.incivisme.ui.notifications.NotificationsFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivisme.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int RC_SIGN_IN = 0;
    FusedLocationProviderClient fusedLocationClient;
    private ShareViewModel shareViewModel;

    FragmentManager fm = getSupportFragmentManager();

    final Fragment fragment1 = new NotificationsFragment();
    final Fragment fragment2 = new ListNotificationFragment();
    final Fragment fragment3 = new MapFragment();

    Fragment active = fragment1;

    private NavigationBarView.OnItemSelectedListener onItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fm.beginTransaction().hide(active).show(fragment1).commit();
                active = fragment1;
                return true;
            case R.id.navigation_list:
                fm.beginTransaction().hide(active).show(fragment2).commit();
                active = fragment2;
                return true;
            case R.id.navigation_map:
                fm.beginTransaction().hide(active).show(fragment3).commit();
                active = fragment3;
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shareViewModel = new ViewModelProvider(this).get(ShareViewModel.class);

        shareViewModel.setFusedLocationClient(fusedLocationClient);
        shareViewModel.getCheckPermission().observe(this, permission -> checkPermission());

        NavigationBarView nav = findViewById(R.id.navigation);
        nav.setOnItemSelectedListener(onItemSelectedListener);

        fm.beginTransaction()
                .add(R.id.fragment_selected, fragment1, "1")
                .hide(fragment1)
                .commit();

        fm.beginTransaction()
                .add(R.id.fragment_selected, fragment2, "2")
                .hide(fragment2)
                .commit();

        fm.beginTransaction()
                .add(R.id.fragment_selected, fragment3, "3")
                .hide(fragment3)
                .commit();

        nav.setSelectedItemId(R.id.navigation_home);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.e("doLogin: ", "why");
        if (auth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    )
                            )
                            .build(),
                    RC_SIGN_IN);
        } else shareViewModel.setCurrentUser(auth.getCurrentUser());
        Log.e("onStart: ","hola");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.e("doLogin: ", "why 12");
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                shareViewModel.setCurrentUser(user);
            }
        }
    }

    void checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else {
            shareViewModel.startTrackingLocation(false);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareViewModel.switchTrackingLocation();
            } else {
                Toast.makeText(this,
                        "Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }




}