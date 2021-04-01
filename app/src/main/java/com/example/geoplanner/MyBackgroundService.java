package com.example.geoplanner;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.geoplanner.Interface.IOnLoadLocationListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyBackgroundService extends Service implements IOnLoadLocationListener, GeoQueryEventListener {

    private static final String CHANNEL_ID = "my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.example.geoplanner" + ".started_from_notification";
    private final IBinder mBinder = new LocalBinder();

    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MIL = UPDATE_INTERVAL_IN_MIL / 2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private IOnLoadLocationListener listener;
    private DatabaseReference getLocation;
    private GeoQuery geoQuery;
    private List<LatLng> markedArea;
    private DatabaseReference locationReff;
    private DatabaseReference taskReff;

    SmsManager smsManager= SmsManager.getDefault();

    Double latVal, longVal;

    List<GeoQuery> gqlist;

    static List<String> locEntered = new ArrayList<>();
    static List<String> msglocEntered = new ArrayList<>();
    static List<String> msgexitlocEntered = new ArrayList<>();

    Boolean bool = false;

    Boolean bool3 = false;
    Boolean bool4 = false;
    Boolean bool5 = false;
    Boolean bool6 = false;
    Boolean bool7 = false;
    Boolean bool8 = false;
    Boolean bool9 = false;
    Boolean bool10 = false;

    Boolean silentMode = false;

    String silentLocId = null;

    int i = 1;


    public MyBackgroundService() {

    }

    public static void removeUncheckedId(String id) {
        if(locEntered.contains(id)) {
            locEntered.remove(id);
            System.out.println(locEntered);
        }
    }


    @Override
    public void onCreate() {

        locationReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        taskReff = FirebaseDatabase.getInstance().getReference("Tasks").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myLocationRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        geoFire = new GeoFire(myLocationRef);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();



        getLocation = FirebaseDatabase.getInstance()
                .getReference("Location")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        listener = this;


        getLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // update marked area list
                List<MyLatLng> latLngList = new ArrayList<>();

                for(DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    MyLatLng latLng = locationSnapshot.getValue(MyLatLng.class);
                    latLngList.add(latLng);
                }


                listener.onLoadLocationSuccess(latLngList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        HandlerThread handlerThread = new HandlerThread("GeoPlanner");
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Intent intent1 = new Intent(this, MainPageActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("GeoPlanner")
//                .setContentText("GeoPlanner is running in background")
//                .setContentIntent(pendingIntent)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .build();

        startForeground(NOTI_ID, getNotification());


        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);

        if(startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        mChangingConfiguration = true;

        super.onConfigurationChanged(newConfig);
    }

    public void removeLocationUpdates() {

        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestingLocationUpdates(this, false);
            stopSelf();
        }
        catch (SecurityException securityException) {
            Common.setRequestingLocationUpdates(this, true);
            Log.e("GeoPlanner", "" + securityException);
        }
    }

    private void getLastLocation() {

        try {

            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                geoFire.setLocation("You", new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()));
                            }
                            else {
                                Log.e("GeoPlanner", "Can't get location");
                            }
                        }
                    });
        } catch (SecurityException securityException) {
            Log.e("GeoPlanner", "" + securityException);
        }
    }

    private void createLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MIL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        mLocation = lastLocation;
        geoFire.setLocation("You", new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()));
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

        // update notification content if running as foreground service
//        if(serviceIsRunningInForeground(this)) {
//            mNotificationManager.notify(NOTI_ID, getNotification());
//        }
    }

    private Notification getNotification() {

        Intent intent = new Intent(this, MyBackgroundService.class);
        String text = "GeoPlanner is running in background";

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

//        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainPageActivity.class), 0);

        Intent intent1 = new Intent(this, MainPageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                .setContentTitle(Common.getLocationTitle(this))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // set the channel id for android O
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private boolean serviceIsRunningInForeground(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(getClass().getName().equals(service.service.getClassName())) {
                if(service.foreground) {
                    return true;
                }
            }
        }

        return false;
    }

    public void requestLocationUpdates() {
        Common.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), MyBackgroundService.class));

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Log.e("GeoPlanner", "" + e);
        }
    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {

        markedArea = new ArrayList<>();


        for(MyLatLng myLatLng : latLngs) {

            LatLng convert = new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude());

            markedArea.add(convert);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        // clear map and add again
//        if(mMap != null) {
//            mMap.clear();

        // add user marker



        if (mLocation != null){
            addCircleArea();
            System.out.println("Add circle area");
        }

//        }

        System.out.println(markedArea);
        System.out.println("on load location success running");

    }

    private void addCircleArea() {
        if(geoQuery != null) {
            System.out.println("remove listener");
            geoQuery.removeGeoQueryEventListener(this);
            geoQuery.removeAllListeners();
        }

        gqlist = new ArrayList<>();
        Boolean b = true;

        for (LatLng latLng : markedArea) {

            geoFire.setLocation("You", new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()));

            // create GeoQuery when user in marked location
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.1f);

            gqlist.add(geoQuery);

            geoQuery.addGeoQueryEventListener(MyBackgroundService.this);
        }
    }

    @Override
    public void onLoadLocationFailed(String message) {

    }


    @Override
    public void onKeyEntered(String key, final GeoLocation location) {

        System.out.println("location entered: " + geoQuery.getCenter());
//        sendNotification("Reminder", String.format("%s entered marked area", key));
//        System.out.println("gqlist"+gqlist);
//
////        for (final GeoQuery gq : gqlist) {
//
////            final GeoQuery gq = gqlist.get(i);
//            System.out.println("gq" + geoQuery.getCenter().latitude);
//
//            locationReff.orderByChild("latitude").equalTo(geoQuery.getCenter().latitude).addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                    latVal = (Double) snapshot.child("latitude").getValue();
//                    longVal = (Double) snapshot.child("longitude").getValue();
//
//                    if(snapshot.child("longitude").getValue().equals(geoQuery.getCenter().longitude)) {
//                        System.out.println("key entered: " + snapshot.getKey());
//
//
//                        taskReff.child("unchecked").orderByChild("locationID").equalTo(snapshot.getKey()).addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                if(latVal == geoQuery.getCenter().latitude && longVal == geoQuery.getCenter().longitude) {
//                                    System.out.println("latVal" + latVal);
//                                    System.out.println("longVal" + longVal);
//                                    sendNotification("Reminder", String.format((String) snapshot.child("tname").getValue()));
//                                    System.out.println(snapshot.getKey());
////                                    addToChecked(snapshot.getKey());
//                                    latVal = Double.valueOf(0);
//                                    longVal = Double.valueOf(0);
//
////                                NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
////                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                                    if(n.isNotificationPolicyAccessGranted()) {
////                                        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
////                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
////                                    }else{
////                                        // Ask the user to grant access
////                                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
////                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                                        startActivity(intent);
////                                    }
////                                }
//                                }
//
//                            }
//
//                            @Override
//                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                            @Override
//                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                            @Override
//                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {}
//                        });
//                    }
//
//                }
//
//                @Override
//                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                @Override
//                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                @Override
//                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//            });



//            locationReff.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for(DataSnapshot snap : snapshot.getChildren()) {
//                        float[] dist = new float[1];
//                        Location.distanceBetween(location.latitude,location.longitude, (Double) snap.child("latitude").getValue(), (Double) snap.child("longitude").getValue(), dist);
//
//                        if(dist[0] <= 100) {
//                            taskReff.child("unchecked").orderByChild("locationID").equalTo(snapshot.getKey()).addChildEventListener(new ChildEventListener() {
//                                @Override
//                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                    if(latVal == geoQuery.getCenter().latitude && longVal == geoQuery.getCenter().longitude) {
//                                        System.out.println("latVal" + latVal);
//                                        System.out.println("longVal" + longVal);
//                                        sendNotification("Reminder", String.format((String) snapshot.child("tname").getValue()));
//                                        System.out.println(snapshot.getKey());
////                                    addToChecked(snapshot.getKey());
//                                        latVal = Double.valueOf(0);
//                                        longVal = Double.valueOf(0);
//                                    }
//
//                                }
//
//                                @Override
//                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                                @Override
//                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                                @Override
//                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {}
//                            });
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });

//        }
        bool = true;


        locationReff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(bool) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(location.latitude);
                    startPoint.setLongitude(location.longitude);

                    Location endPoint=new Location("locationA");
                    endPoint.setLatitude((Double) snapshot.child("latitude").getValue());
                    endPoint.setLongitude((Double) snapshot.child("longitude").getValue());

                    double distance=startPoint.distanceTo(endPoint);


                    if(distance <= 100) {

                        bool3 = true;
                        bool4 = true;
                        bool5 = true;
                        bool6 = true;
                        bool9 = true;
//                        taskReff.child("unchecked").orderByChild("locationID").equalTo(snapshot.getKey()).addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                if(bool3) {
//                                    if (!locEntered.contains(snapshot.getKey())) {
//                                        sendNotification("Reminder", String.format((String) snapshot.child("tname").getValue()));
//                                        addToChecked(snapshot.getKey());
//                                        locEntered.add(snapshot.getKey());
//                                        System.out.println("locEntered" + locEntered);
//                                    }
//                                }
//                            }
//
//
//                            @Override
//                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                            @Override
//                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                            @Override
//                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {}
//                        });

                        taskReff.child("unchecked").orderByChild("locationID").equalTo(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                    if (bool3) {
                                        if (!locEntered.contains(dataSnapshot1.getKey())) {
                                            sendNotification("Reminder", String.format((String) dataSnapshot1.child("tname").getValue()));
                                            addToChecked(dataSnapshot1.getKey());
//                                            dataSnapshot1.getRef().removeValue();
                                            locEntered.add(dataSnapshot1.getKey());
                                            System.out.println("locEntered" + locEntered);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                bool3 = false;
                            }
                        }, 5000);


                        FirebaseDatabase.getInstance().getReference("Silent").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("locationID").equalTo(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                    if(bool4) {
                                        if(dataSnapshot1.child("status").getValue().toString().equals("on")) {
                                            silentMode = true;
                                            NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                if (n.isNotificationPolicyAccessGranted()) {
                                                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                                                    callServiceReceiver.silentService = true;
                                                    callServiceReceiver.message = dataSnapshot1.child("message").getValue().toString();
                                                    callServiceReceiver.id = dataSnapshot1.getKey();
                                                    silentLocId = dataSnapshot1.child("locationID").getValue().toString();
                                                    System.out.println("message: " + callServiceReceiver.message);
                                                } else {
                                                    // Ask the user to grant access
                                                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                bool4 = false;
                            }
                        }, 5000);

                        FirebaseDatabase.getInstance().getReference("AutoMessage").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("locationID").equalTo(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (bool5) {
                                            if (dataSnapshot.child("status").getValue().toString().equals("on")) {
                                                if (dataSnapshot.child("sendAt").getValue().toString().equals("enter")) {

                                                    if (!msglocEntered.contains(dataSnapshot.child("locationID").getValue().toString())) {

                                                        final String message = dataSnapshot.child("message").getValue().toString();

                                                        dataSnapshot.child("contacts").getRef().addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                                                    if (bool6) {
                                                                        String contactNo = dataSnapshot1.child("number").getValue().toString();

                                                                        smsManager.sendTextMessage(contactNo, null, message + "\n\n" + "~ GeoPlanner", null, null);
                                                                        System.out.println("message");
                                                                        Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                                                                        sendNotification("GeoPlanner", "message sent");
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }

                                                    msglocEntered.add(dataSnapshot.child("locationID").getValue().toString());
                                                    System.out.println("locEntered" + msglocEntered);
                                                    sendNotification("GeoPlanner", "msgloc: " + msglocEntered);
                                                } else if (dataSnapshot.child("sendAt").getValue().toString().equals("exit")) {

                                                    if (!msgexitlocEntered.contains(dataSnapshot.child("locationID").getValue().toString())) {
                                                        msgexitlocEntered.add(dataSnapshot.child("locationID").getValue().toString());
                                                        System.out.println("msgexitloc" + msgexitlocEntered);
                                                    }

                                                }
                                            }
                                        }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                bool5 = false;
                            }
                        }, 5000);

                        final Handler handler4 = new Handler();
                        handler4.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                bool9 = false;
                            }
                        }, 5000);

                        final Handler handler3 = new Handler();
                        handler3.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                bool6 = false;
                            }
                        }, 5000);


                    }
                }
                    bool = false;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





//        locationReff.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            }
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//            }
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });

    }

    public void addToChecked(final String taskKey) {
        System.out.println("task key:" + taskKey);

        final Boolean[] bool2 = {true};

        Query query = taskReff;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (snapshot.hasChild("checked")) {

                    Query lastQuery = taskReff.child("checked").orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                System.out.println("on data change called");

                                Log.d("User key", child.getKey());
                                Log.d("User val", child.child("tname").getValue().toString());

//                                Random random = new Random();
//                                int ran = random.nextInt(10-1)+1;
//                                System.out.println("random num"+ ran);

                                String id = child.getKey();
                                final int newID = Integer.parseInt(id) + i;
                                System.out.println("before i:"+i);
                                i++;
                                System.out.println("after i:"+i);

                                System.out.println(newID);


                                Query uncheck = taskReff.child("unchecked").child(taskKey);

                                uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        System.out.println("bool2"+ bool2[0]);
                                        if (bool2[0]) {

                                            taskReff
                                                    .child("checked")
                                                    .child(String.valueOf(newID))
                                                    .setValue(snapshot.getValue());

                                            snapshot.getRef().removeValue();

                                            bool2[0] = false;
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

                else {

                    Query uncheck = taskReff.child("unchecked").child(taskKey);


                    uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (bool2[0]) {
                                System.out.println("on data change called");
                                System.out.println(snapshot.getValue());

                                FirebaseDatabase.getInstance().getReference("Tasks")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("checked")
                                        .child("1")
                                        .setValue(snapshot.getValue());

                                snapshot.getRef().removeValue();

                                bool2[0] = false;
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        i = 1;
    }



    @Override
    public void onKeyExited(String key) {
//        sendNotification("GeoPlanner", String.format("%s exited marked area", key));

//        if(silentMode) {
//            NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (n.isNotificationPolicyAccessGranted()) {
//                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//                } else {
//                    // Ask the user to grant access
//                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
//            }
//            callServiceReceiver.silentService = false;
//            callServiceReceiver.message = null;
//            callServiceReceiver.id = null;
//            silentMode = false;
//        }


        bool7 = true;

        locationReff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(bool7) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Location startPoint=new Location("locationA");
                        startPoint.setLatitude(mLocation.getLatitude());
                        startPoint.setLongitude(mLocation.getLongitude());

                        Location endPoint=new Location("locationA");
                        endPoint.setLatitude((Double) snapshot.child("latitude").getValue());
                        endPoint.setLongitude((Double) snapshot.child("longitude").getValue());

                        double distance=startPoint.distanceTo(endPoint);


                        if(distance >= 100) {
                            if (msglocEntered.contains(snapshot.getKey())) {
                                msglocEntered.remove(snapshot.getKey());
                                System.out.println("msgloc: " + msglocEntered);
                                sendNotification("GeoPlanner", "msgloc: " + msglocEntered);
                            }

                            if (msgexitlocEntered.contains(snapshot.getKey())) {
                                msgexitlocEntered.remove(snapshot.getKey());
                                System.out.println("msgexitloc: " + msgexitlocEntered);

                                bool8 = true;

                                FirebaseDatabase.getInstance().getReference("AutoMessage").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("locationID").equalTo(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            if (bool8) {
                                                final String message = dataSnapshot.child("message").getValue().toString();

                                                dataSnapshot.child("contacts").getRef().addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                                            if (bool8) {
                                                                String contactNo = dataSnapshot1.child("number").getValue().toString();

                                                                smsManager.sendTextMessage(contactNo, null, message + "\n\n" + "~ GeoPlanner", null, null);
                                                                System.out.println("message");
                                                                Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }

                            if (silentLocId != null) {
                                if (silentLocId.equals(snapshot.getKey())) {
                                    if (silentMode) {
                                        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            if (n.isNotificationPolicyAccessGranted()) {
                                                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                            } else {
                                                // Ask the user to grant access
                                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                        callServiceReceiver.silentService = false;
                                        callServiceReceiver.message = null;
                                        callServiceReceiver.id = null;
                                        silentMode = false;
                                    }
                                }
                            }
                        }
                    }
                    bool7 = false;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }

    private void sendNotification(String title, String content) {

        String NOTIFICATION_CHANNEL_ID = "gp_rem";

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Reminder", NotificationManager.IMPORTANCE_DEFAULT);

            // config
            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    public class LocalBinder extends Binder {
        MyBackgroundService getService() {
            return MyBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {

        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if(!mChangingConfiguration && Common.requestingLocationUpdates(this)) {
            startForeground(NOTI_ID, getNotification());
        }

        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }
}

