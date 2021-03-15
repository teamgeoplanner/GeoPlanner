package com.example.geoplanner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

public class AutoSilentFragment extends Fragment {
    Button button;
//    MyBackgroundService mService;
//    Boolean mBound = false;


//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder) iBinder;
//            mService = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mService = null;
//            mBound = false;
//        }
//    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //link with xml file
        final View view = inflater.inflate(R.layout.fragment_autosilent, container,false);

//        Dexter.withActivity(getActivity())
//                .withPermissions(Arrays.asList(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                ))
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        button = view.findViewById(R.id.button);
//
//                        button.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                mService.requestLocationUpdates();
//                            }
//                        });
//
//
//                        getActivity().bindService(new Intent(getActivity(), MyBackgroundService.class),
//                                mServiceConnection,
//                                Context.BIND_AUTO_CREATE);
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//
//                    }
//                }).check();



//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent = new Intent(getContext(), MapsActivity.class);
////                startActivity(intent);
//
////                Intent intent = new Intent(getContext(), MyService.class);
////
////                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                    getContext().startForegroundService(intent);
////                }
////                else {
////                    getContext().startService(intent);
////                }
//            }
//        });

        button = view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity2.class);
                startActivity(intent);
            }
        });

        return view;
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//        if(s.equals(Common.KEY_REQUESTING_LOCATION_UPDATES)) {
//
//        }
//    }

//    @Override
//    public void onStart() {
//
//        PreferenceManager.getDefaultSharedPreferences(getContext())
//                .registerOnSharedPreferenceChangeListener(this);
//        EventBus.getDefault().register(this);
//
//        super.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        if(mBound) {
//            getActivity().unbindService(mServiceConnection);
//            mBound = false;
//        }
//
//        PreferenceManager.getDefaultSharedPreferences(getContext())
//                .unregisterOnSharedPreferenceChangeListener(this);
//
//        EventBus.getDefault().unregister(this);
//
//        super.onStop();
//    }
//
//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onListenLocation(SendLocationToActivity event) {
//        if(event != null) {
//            String data = new StringBuilder()
//                    .append(event.getLocation().getLatitude())
//                    .append("/")
//                    .append(event.getLocation().getLongitude())
//                    .toString();
//
//            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
//        }
//    }
}
