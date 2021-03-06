package com.example.geoplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static com.example.geoplanner.TasksFragment.adapter1;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link TaskDetailFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class TaskDetailFragment extends Fragment {

    EditText taskName;
    Button btnLocation;
    Button btnSave;
    TextView displayLocation;
    ImageView btnBack;
    ImageButton btnDelete;

    Dialog dialog;

    String newLocID;

    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    LatLng location;

    Double latitude, longitude;

    DatabaseReference taskReff = FirebaseDatabase.getInstance().getReference("Tasks").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String TName;
    String taskID;
    String checked;

    public TaskDetailFragment(String TName, String taskID, String s) {
        this.TName = TName;
        this.taskID = taskID;
        checked = s;
    }

    public TaskDetailFragment() {

    }

    //    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment TaskDetailFragment.
//     */
//    // TODO: Rename and change types and number of parameters
    public static TaskDetailFragment newInstance(String param1, String param2) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);

        taskName = view.findViewById(R.id.txtTask);
        btnLocation = view.findViewById(R.id.btnAddLocation);
        displayLocation = view.findViewById(R.id.textDispLoc);
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDeleted);

        taskName.setText(TName);

        FirebaseDatabase.getInstance().getReference("Tasks")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(checked)
                .child(taskID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String locID;
                        locID = String.valueOf(snapshot.child("locationID").getValue());

                        if(locID != "null") {
                            locReff.child(locID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            latitude = (Double) snapshot.child("latitude").getValue();
                                            longitude = (Double) snapshot.child("longitude").getValue();

                                            dispLoc();
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

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTaskInfo();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.delete_task_dialog);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_background));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);    //Don't close dialog box on clicking background

                Button btnDel, btnCancel;


                btnDel = dialog.findViewById(R.id.deleteBtn);
                btnCancel = dialog.findViewById(R.id.cancelBtn);



                //Click event of Reset Password Bitton
                btnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTask();

                        dialog.dismiss();
                    }
                });

                //Click event of Cancel Button
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TasksFragment()).addToBackStack(null).commit();
            }
        });

        return  view;
    }

    private void deleteTask() {

        Query uncheck = taskReff.child(checked);
        uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(taskID).getRef().removeValue();

                String locID = (String) snapshot.child(taskID).child("locationID").getValue();

                FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(locID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();

                                AppCompatActivity activity = (AppCompatActivity) getContext();
                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TasksFragment()).addToBackStack(null).commit();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateTaskInfo() {
        String tskName = taskName.getText().toString();

        FirebaseDatabase.getInstance().getReference("Tasks")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(checked)
                .child(taskID)
                .child("tname")
                .setValue(tskName);

        if(location != null) {
            FirebaseDatabase.getInstance().getReference("Tasks")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(checked)
                    .child(taskID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String locID;
                            locID = String.valueOf(snapshot.child("locationID").getValue());

                            if(!locID.equals("null")) {
                                locReff.child(locID).setValue(location);
                            } else {
                                addNewLoc(snapshot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void addNewLoc(final DataSnapshot snapshotTask) {
        FirebaseDatabase.getInstance().getReference("Location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                    locReff.orderByKey().limitToLast(1)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        String id = child.getKey();
                                        newLocID = String.valueOf(Integer.parseInt(id) + 1);


                                        locReff.child(newLocID).setValue(location);

                                        snapshotTask.child("locationID").getRef().setValue(newLocID);

                                        System.out.println("Locid" + id);

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
                    locReff.child(newLocID).setValue(location);

                    snapshotTask.child("locationID").getRef().setValue(newLocID);

                    location = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dispLoc() {
        if(latitude != null && longitude != null) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            List<Address> addresses  = null;
            try {
                addresses = geocoder.getFromLocation(latitude ,longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = addresses.get(0).getAddressLine(0);

            displayLocation.setText(address);
            displayLocation.setVisibility(View.VISIBLE);
        } else {
            btnLocation.setText("Add Location");
        }
    }



//    public void onBackPressed() {
//        AppCompatActivity activity = (AppCompatActivity) getContext();
//        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TasksFragment()).commit();
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

                displayLocation.setText(address);
                displayLocation.setVisibility(View.VISIBLE);
                btnLocation.setText("Update Location");
            }

            else {
                location = null;
            }
        }

    }


}