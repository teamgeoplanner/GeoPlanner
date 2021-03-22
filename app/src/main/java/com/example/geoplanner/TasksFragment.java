package com.example.geoplanner;

import android.content.Intent;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import okhttp3.internal.cache.DiskLruCache;

import static android.app.Activity.RESULT_OK;

public class TasksFragment extends Fragment {
    ImageButton btnAdd;
    RecyclerView recyclerView1, recyclerView2;
    static myadapter adapter1;
    myadapter2 adapter2;
    ConcatAdapter concatAdapter;
    FirebaseAuth fAuth;
    EditText txtTask;
    DatabaseReference taskReff;
    int countTasks = 0;
    int newID;
    String newLocID;

    int PLACE_PICKER_REQUEST = 1;

    TextView locationName;

    LatLng location;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //link with xml file
        View view = inflater.inflate(R.layout.fragment_tasks, container,false);


        recyclerView1 = view.findViewById(R.id.recTasks);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));

        fAuth = FirebaseAuth.getInstance();


        FirebaseRecyclerOptions<model> options1 =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("unchecked"), model.class)
                        .build();


        adapter1 = new myadapter(options1);

        FirebaseRecyclerOptions<model> options2 =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("checked"), model.class)
                        .build();

        adapter2 = new myadapter2(options2);

        concatAdapter = new ConcatAdapter(adapter1,adapter2);

        recyclerView1.setAdapter(concatAdapter);


//        recyclerView2 = view.findViewById(R.id.recTasks);
//        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
//
//
//        FirebaseRecyclerOptions<model> options2 =
//                new FirebaseRecyclerOptions.Builder<model>()
//                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("checked"), model.class)
//                        .build();
//
//        adapter2 = new myadapter2(options2);
//        recyclerView2.setAdapter(adapter2);


        taskReff = FirebaseDatabase.getInstance().getReference("Tasks");

        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    countTasks = (int) snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnAdd = view.findViewById(R.id.btnAddTask);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

                //Bottom sheet view for adding tasks

                View bottomSheetView = LayoutInflater.from(getContext())
                        .inflate(R.layout.layout_bottom_sheet, (LinearLayout)view.findViewById(R.id.bottomSheetContainer));

                bottomSheetView.findViewById(R.id.btnAddDate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Date", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetView.findViewById(R.id.btnAddLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Location", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivityForResult(intent, 2);

                    }
                });


                locationName = bottomSheetView.findViewById(R.id.textLocation);

                txtTask = bottomSheetView.findViewById(R.id.txtTask);

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
        itemTouchHelper.attachToRecyclerView(recyclerView1);



        return view;
    }

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

                                            final model taskObj = new model(txtTask.getText().toString(), String.valueOf(newLocID));

                                            addData(taskObj);

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

                        final model taskObj = new model(txtTask.getText().toString(), String.valueOf(newLocID));

                        addData(taskObj);

                        location = null;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            newLocID = null;

            final model taskObj = new model(txtTask.getText().toString(), String.valueOf(newLocID));

            addData(taskObj);
        }



    }

    public static myadapter getAdap() {
        return adapter1;
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

            if (viewHolder.getBindingAdapter() == adapter1) {
                adapter1.copyItem(position);

//                adapter1.deleteItem(position);

                Snackbar snackbar = Snackbar.make(recyclerView1,"Task Deleted", Snackbar.LENGTH_LONG);

                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter1.undoItem(position);
                    }
                }).show();

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            adapter1.clearDeleted();
                        }

                    }
                });
            }

            else if(viewHolder.getBindingAdapter() == adapter2) {
                adapter2.copyItem(position);

//                adapter2.deleteItem(position);

                Snackbar snackbar = Snackbar.make(recyclerView1,"Task Deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter2.undoItem(position);
                    }
                }).show();

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            adapter2.clearDeleted();
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


    public void addData(final model taskObj) {
        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("unchecked")) {

                    Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked").orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                String taskID = dataSnapshot.getValue().toString();
//                                String id = taskID.substring(1,2);
                                System.out.println("on data change 2");
//                                newID = Integer.parseInt(id) + 1;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Log.d("User key", child.getKey());
                                Log.d("User val", child.child("tname").getValue().toString());

                                String id = child.getKey();
                                newID = Integer.parseInt(id) + 1;

                                System.out.println(newID);

                                FirebaseDatabase.getInstance().getReference("Tasks")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("unchecked")
                                        .child(String.valueOf(newID))
                                        .setValue(taskObj);

                                MyBackgroundService.removeUncheckedId(String.valueOf(newID));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

                else {
                    FirebaseDatabase.getInstance().getReference("Tasks")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("unchecked")
                            .child("1")
                            .setValue(taskObj);

                    MyBackgroundService.removeUncheckedId("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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

    //    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if(requestCode == PLACE_PICKER_REQUEST) {
//            if(resultCode == RESULT_OK) {
//
//                Place place = PlacePicker.getPlace(data, getContext());
////                String address = String.valueOf(place.getLatLng().latitude + ", " + place.getLatLng().longitude);
////                locationName.setText(address);
////                locationName.setVisibility(View.VISIBLE);
////                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
//                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
//
//                List<Address> addresses  = null;
//                try {
//                    addresses = geocoder.getFromLocation(place.getLatLng().latitude ,place.getLatLng().longitude, 1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                String address = addresses.get(0).getAddressLine(0);
//                locationName.setText(address);
//            }
//        }
//    }


//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 100) {
//            if (resultCode == RESULT_OK) {
//                Place place = Autocomplete.getPlaceFromIntent(data);
////                Log.i("TAG", "Place: " + place.getName() + ", " + place.getId());
//                System.out.println("address:"+place.getAddress());
//            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
//                // TODO: Handle the error.
//                Status status = Autocomplete.getStatusFromIntent(data);
//                Log.i("TAG", status.getStatusMessage());
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        adapter1.startListening();
        adapter2.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter1.stopListening();
        adapter2.stopListening();
    }

}
