package com.example.incivisme.ui.notifications;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationsFragment extends Fragment {

    private ShareViewModel shareViewModel;
    private FragmentNotificationsBinding binding;
    private ProgressBar loadingBar;
    private TextInputEditText notifyLat;
    private TextInputEditText notifyLon;
    private TextInputEditText notifyAddress;
    private TextInputEditText notifyDescription;
    private String notifyPic;
    private Button button;
    private Button takePicBtn;
    private ImageView photo;

    String photoPath;
    private Uri photoURI;
    static final int REQUEST_TAKE_PHOTO = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shareViewModel =
                new ViewModelProvider(requireActivity()).get(ShareViewModel.class);

        Log.e("notification", "notification ");

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        shareViewModel.setFusedLocationClient(LocationServices.getFusedLocationProviderClient(requireContext()));

        button = binding.notifyButton;
        takePicBtn = binding.takePicBtn;
        loadingBar = binding.loading;
        notifyLat = binding.notificationFill1;
        notifyLon = binding.notificationFill2;
        notifyAddress = binding.notificationFill3;
        notifyDescription = binding.notificationFill4;
        photo = binding.picTook;


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

        takePicBtn.setOnClickListener(button -> {
            dispatchTakePictureIntent();
            FirebaseStorage storage = FirebaseStorage.getInstance();

            StorageReference reference = storage.getReference();

            StorageReference imaRef = reference.child(photoPath);
            UploadTask uploadTask = imaRef.putFile(photoURI);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imaRef.getDownloadUrl().addOnCompleteListener(task -> {
                    Uri downloadUri = task.getResult();
                    Glide.with(this).load(downloadUri).into(photo);

                    notifyPic = downloadUri.toString();
                    Log.e("XXXXX",""+notifyPic);
                });
            });
        });

        button.setOnClickListener(view ->{
            Notification notification = new Notification();
            notification.setLatitude(notifyLat.getText().toString());
            notification.setLongitude(notifyLon.getText().toString());
            notification.setAddress(notifyAddress.getText().toString());
            notification.setProblem(notifyDescription.getText().toString());
            notification.setPic(notifyPic);
            Log.e("------------",""+notification.getPic());

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference data = FirebaseDatabase.getInstance().getReference();

            DatabaseReference users = data.child("users");
            DatabaseReference uid = users.child(firebaseAuth.getUid());
            DatabaseReference notify = uid.child("notifications");

            shareViewModel.getNotificationRef().observe(getViewLifecycleOwner(), dr -> {
                dr.setValue(notify);
            });

            Log.e("XXXXX",notify.toString());

            DatabaseReference notificationReference = notify.push();
            notificationReference.setValue(notification);

            Toast.makeText(getContext(), "Notified successfully", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private File createImageFile() {

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        try {
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            photoPath = image.getAbsolutePath();
            return image;
        }catch (Exception e){
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(
                requireContext().getPackageManager()) != null) {
            Log.e( "hola","hola" );
            File photoFile = null;
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(requireContext(),
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } catch (Exception ex) {
                Log.e("dispatchTakePicture", ex.getMessage());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoURI).into(photo);
            } else {
                Toast.makeText(getContext(),
                        "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
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