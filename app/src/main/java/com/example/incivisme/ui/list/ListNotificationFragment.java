package com.example.incivisme.ui.list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivisme.R;
import com.example.incivisme.ShareViewModel;
import com.example.incivisme.databinding.FragmentNotificationListBinding;
import com.example.incivisme.ui.bs.Notification;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListNotificationFragment extends Fragment {


    private FragmentNotificationListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentNotificationListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference data = FirebaseDatabase.getInstance().getReference();

        DatabaseReference users = data.child("users");
        DatabaseReference uid = users.child(firebaseAuth.getUid());
        DatabaseReference notifications = uid.child("notifications");

        FirebaseListOptions<Notification> options = new FirebaseListOptions.Builder<Notification>()
                .setQuery(notifications, Notification.class)
                .setLayout(R.layout.list_row)
                .setLifecycleOwner(this)
                .build();

        FirebaseListAdapter<Notification> adapter = new FirebaseListAdapter<Notification>(options) {
            @Override
            protected void populateView(View v, Notification notification, int position) {
                TextView addressText = v.findViewById(R.id.addressText);
                TextView descriptionText = v.findViewById(R.id.descriptionText);

                addressText.setText(notification.getAddress());
                descriptionText.setText("notification: "+notification.getProblem());
            }
        };

        ListView listView = binding.notificationList;
        listView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
