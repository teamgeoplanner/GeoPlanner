package com.example.geoplanner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AutoSilentFragment extends Fragment {
    ImageButton btnAdd;
    Button button;

    TextView locationName;
    EditText txtSilent;
    EditText message;

    LatLng location;
    String newLocID;
    int newID;

    DatabaseReference silentReff;
    FirebaseAuth fAuth;

    RecyclerView recyclerView;
    silentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //link with xml file
        View view = inflater.inflate(R.layout.fragment_autosilent, container,false);

        recyclerView = view.findViewById(R.id.recSilent);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fAuth = FirebaseAuth.getInstance();


        FirebaseRecyclerOptions<model2> options =
                new FirebaseRecyclerOptions.Builder<model2>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Silent").child(fAuth.getCurrentUser().getUid()), model2.class)
                        .build();


        adapter = new silentAdapter(options);

        recyclerView.setAdapter(adapter);


        btnAdd = view.findViewById(R.id.btnAddSilent);
        silentReff = FirebaseDatabase.getInstance().getReference("Silent");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

                //Bottom sheet view for adding tasks

                View bottomSheetView = LayoutInflater.from(getContext())
                        .inflate(R.layout.autosilent_bottom_sheet, (LinearLayout)view.findViewById(R.id.silentBottomSheetContainer));


                bottomSheetView.findViewById(R.id.btnAddLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Location", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivityForResult(intent, 2);

                    }
                });


                locationName = bottomSheetView.findViewById(R.id.textLocation);
                txtSilent = bottomSheetView.findViewById(R.id.txtSilent);
                message = bottomSheetView.findViewById(R.id.txtMessage);

                bottomSheetView.findViewById(R.id.btnSaveTask).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        addLocation();

                        bottomSheetDialog.dismiss();
                    }
                });



                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    //Swipe left or right to delete function
    final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();

            if (viewHolder.getBindingAdapter() == adapter) {
                adapter.copyItem(position);

//                adapter1.deleteItem(position);

                Snackbar snackbar = Snackbar.make(recyclerView,"Auto-Silent Location Deleted", Snackbar.LENGTH_LONG);

                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.undoItem(position);
                    }
                }).show();

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            adapter.clearDeleted();
                        }

                    }
                });
            }
//            System.out.println(viewHolder.getBindingAdapter());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                    .addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    private void addLocation() {
        if(location!=null) {
            final DatabaseReference locationReff = FirebaseDatabase.getInstance()
                    .getReference("Location")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            FirebaseDatabase.getInstance().getReference("Location").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        locationReff.orderByKey().limitToLast(1)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            String id = child.getKey();
                                            newLocID = String.valueOf(Integer.parseInt(id) + 1);

                                            locationReff.child(newLocID).setValue(location);

                                            final model2 silentObj = new model2(txtSilent.getText().toString(), String.valueOf(newLocID), message.getText().toString(), "on");

                                            addData(silentObj);

                                            location = null;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                    else {
                        newLocID = "0";
                        locationReff.child(newLocID).setValue(location);

                        final model2 silentObj = new model2(txtSilent.getText().toString(), String.valueOf(newLocID), message.getText().toString(), "on");

                        addData(silentObj);

                        location = null;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            newLocID = null;

            final model2 silentObj = new model2(txtSilent.getText().toString(), String.valueOf(newLocID), message.getText().toString(), "on");

            addData(silentObj);
        }



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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2) {
            if(resultCode == 2) {
                Double latitude = data.getDoubleExtra("latitude", 0);
                Double longitude = data.getDoubleExtra("longitude", 0);

                location = new LatLng(latitude, longitude);

                //            locationName.setText((int) (location.latitude + location.longitude));
                System.out.println("location" + location);


                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(location.latitude ,location.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = addresses.get(0).getAddressLine(0);
                System.out.println("address" + addresses.get(0).getSubLocality());

                String displayAddress = addresses.get(0).getSubLocality() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + " " + addresses.get(0).getPostalCode();
                locationName.setText(displayAddress.substring(0, 20) + "...");
                locationName.setVisibility(View.VISIBLE);
            }

            else if(resultCode == 3) {
                location = null;
            }
        }

    }


    public void addData(final model2 silentObj) {
        silentReff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                    Query lastQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                String taskID = dataSnapshot.getValue().toString();
//                                String id = taskID.substring(1,2);
                            System.out.println("on data change 2");
//                                newID = Integer.parseInt(id) + 1;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Log.d("User key", child.getKey());
                                Log.d("User val", child.child("sname").getValue().toString());

                                String id = child.getKey();
                                newID = Integer.parseInt(id) + 1;

                                System.out.println(newID);

                                FirebaseDatabase.getInstance().getReference("Silent")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(String.valueOf(newID))
                                        .setValue(silentObj);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

                else {
                    FirebaseDatabase.getInstance().getReference("Silent")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("1")
                            .setValue(silentObj);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
