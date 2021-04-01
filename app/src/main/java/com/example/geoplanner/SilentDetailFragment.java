package com.example.geoplanner;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SilentDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SilentDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String Silentname;
    String Id;
    String newLocID;

    EditText silentName;
    EditText txtMessage;

    Button btnLocation;
    Button btnSave;

    LatLng location;

    Dialog dialog;

    TextView displayLocation;

    ImageView btnBack;
    ImageButton btnDelete;

    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    DatabaseReference silentReff = FirebaseDatabase.getInstance().getReference("Silent").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    Double latitude, longitude;

    public SilentDetailFragment() {
        // Required empty public constructor
    }

    public SilentDetailFragment(String sname, String id) {

        Silentname = sname;
        Id = id;

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SilentDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SilentDetailFragment newInstance(String param1, String param2) {
        SilentDetailFragment fragment = new SilentDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_silent_detail, container, false);

        silentName = view.findViewById(R.id.txtSilent);
        btnLocation = view.findViewById(R.id.btnAddLocation);
        displayLocation = view.findViewById(R.id.textDispLoc);
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDeleted);
        txtMessage = view.findViewById(R.id.txtSilentMessage);

        silentName.setText(Silentname);


        FirebaseDatabase.getInstance().getReference("Silent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String locID;
                        locID = String.valueOf(snapshot.child("locationID").getValue());

                        txtMessage.setText(snapshot.child("message").getValue().toString());

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
                updateSilentInfo();
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).addToBackStack(null).commit();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.delete_silent_dialog);
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
                        deleteSilent();

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
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).addToBackStack(null).commit();
            }
        });

       return view;
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

    private void updateSilentInfo() {
        String sName = silentName.getText().toString();

        FirebaseDatabase.getInstance().getReference("Silent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .child("sname")
                .setValue(sName);

        FirebaseDatabase.getInstance().getReference("Silent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .child("message")
                .setValue(txtMessage.getText().toString());

        if(location != null) {
            FirebaseDatabase.getInstance().getReference("Silent")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String locID;
                            locID = String.valueOf(snapshot.child("locationID").getValue());

                            if(!locID.equals("null")) {
                                locReff.child(locID).setValue(location);
//                                AppCompatActivity activity = (AppCompatActivity) getView().getContext();
//                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).addToBackStack(null).commit();
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
//                                        AppCompatActivity activity = (AppCompatActivity) getContext();
//                                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).addToBackStack(null).commit();
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

    private void deleteSilent(){

        Query sQuery = silentReff;
        sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(Id.equals(callServiceReceiver.id)) {
                    callServiceReceiver.silentService = false;
                    callServiceReceiver.message = null;
                    callServiceReceiver.id = null;
                }
                snapshot.child(Id).getRef().removeValue();

                String locID = (String) snapshot.child(Id).child("locationID").getValue();

                FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(locID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();

                                AppCompatActivity activity = (AppCompatActivity) getContext();
                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).addToBackStack(null).commit();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2) {
            if(resultCode == 2) {
                Double latitude = data.getDoubleExtra("latitude", 0);
                Double longitude = data.getDoubleExtra("longitude", 0);

                location = new LatLng(latitude, longitude);

//                locationName.setText((int) (location.latitude + location.longitude));
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