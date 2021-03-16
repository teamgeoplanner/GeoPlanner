package com.example.geoplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class MainActivity2 extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Button button, button2;
    MyBackgroundService mService;
    Boolean mBound = false;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(!n.isNotificationPolicyAccessGranted()) {
                                // Ask the user to grant access
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }

                        button = findViewById(R.id.button2);
                        button2 = findViewById(R.id.button3);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mService.requestLocationUpdates();
                            }
                        });

                        button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mService.removeLocationUpdates();
                            }
                        });


                        bindService(new Intent(MainActivity2.this, MyBackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        for(int i=0 ; i<permissions.size() ; i++) {
                            if(permissions.toString().equals("[Permission name: android.permission.ACCESS_BACKGROUND_LOCATION]")) {
                                System.out.println("permission:"+permissions);
//                                Toast.makeText(mService, "background permission denied", Toast.LENGTH_SHORT).show();
                            }
//                        finish();

//                        }
                    }
                }).check();


    }



    @Override
    public void onStart() {

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);

        super.onStart();
    }

    @Override
    public void onStop() {
        if(mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event) {
        if(event != null) {
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();

            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}