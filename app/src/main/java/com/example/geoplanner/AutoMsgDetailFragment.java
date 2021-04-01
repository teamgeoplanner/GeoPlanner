package com.example.geoplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AutoMsgDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutoMsgDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String mName;
    String Id;
    String newLocID;

    EditText autoMsgName;
    EditText txtMessage;

    Button btnLocation;
    Button btnAddContact;
    Button btnSave;

    LatLng location;

    Dialog dialog;

    TextView displayLocation;

    ImageView btnBack;
    ImageButton btnDelete;

    RecyclerView recyclerViewContacts;
    contactsAdapter2 contactsAdap;

    ArrayList<String> contactsName = new ArrayList<>();
    public static ArrayList<String> contactsNo = new ArrayList<>();

    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    DatabaseReference autoMsgReff = FirebaseDatabase.getInstance().getReference("AutoMessage").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    Double latitude, longitude;

    Boolean bool = false;

    public AutoMsgDetailFragment() {
        // Required empty public constructor
    }

    public AutoMsgDetailFragment(String mname, String id) {

        mName = mname;
        Id = id;

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutoMsgDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutoMsgDetailFragment newInstance(String param1, String param2) {
        AutoMsgDetailFragment fragment = new AutoMsgDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_auto_msg_detail, container, false);

        autoMsgName = view.findViewById(R.id.txtAutoMsg);
        btnLocation = view.findViewById(R.id.btnAddLocation);
        btnAddContact = view.findViewById(R.id.btnAddContact);
        displayLocation = view.findViewById(R.id.textDispLoc);
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDeleted);
        txtMessage = view.findViewById(R.id.txtAutoMsgMessage);

        autoMsgName.setText(mName);

        recyclerViewContacts = view.findViewById(R.id.recContactsList);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsAdap = new contactsAdapter2(getContext(), contactsName);

        recyclerViewContacts.setAdapter(contactsAdap);



        FirebaseDatabase.getInstance().getReference("AutoMessage")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String locID;
                        locID = String.valueOf(snapshot.child("locationID").getValue());

                        txtMessage.setText(snapshot.child("message").getValue().toString());

                        bool = true;

                        snapshot.child("contacts").getRef().addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                contactsNo.clear();
                                contactsName.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    if(bool) {
                                        contactsNo.add(dataSnapshot.child("number").getValue().toString());
                                        contactsName.add(dataSnapshot.child("name").getValue().toString());
                                        contactsAdap.notifyDataSetChanged();
                                    }
                                }
                                bool = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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
                updateAutoMsgInfo();
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoMsgFragment()).addToBackStack(null).commit();
            }
        });

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 4);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.delete_automsg_dialog);
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
                        deleteAutoMsg();

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
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoMsgFragment()).addToBackStack(null).commit();
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

    private void updateAutoMsgInfo() {
        String msgName = autoMsgName.getText().toString();

        FirebaseDatabase.getInstance().getReference("AutoMessage")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .child("mname")
                .setValue(msgName);

        FirebaseDatabase.getInstance().getReference("AutoMessage")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .child("message")
                .setValue(txtMessage.getText().toString());

        FirebaseDatabase.getInstance().getReference("AutoMessage")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Id)
                .child("contacts")
                .getRef().removeValue();

        for(int i = 0 ; i < contactsName.size() ; i++) {
            FirebaseDatabase.getInstance().getReference("AutoMessage")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Id)
                    .child("contacts")
                    .child(String.valueOf(i))
                    .child("number")
                    .setValue(contactsNo.get(i));

            FirebaseDatabase.getInstance().getReference("AutoMessage")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Id)
                    .child("contacts")
                    .child(String.valueOf(i))
                    .child("name")
                    .setValue(contactsName.get(i));
        }

        if(location != null) {
            FirebaseDatabase.getInstance().getReference("AutoMessage")
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

    private void addNewLoc(final DataSnapshot snapshotAutoMsg) {
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

                                        snapshotAutoMsg.child("locationID").getRef().setValue(newLocID);

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

                    snapshotAutoMsg.child("locationID").getRef().setValue(newLocID);

                    location = null;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteAutoMsg(){

        Query mQuery = autoMsgReff;
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.child(Id).getRef().removeValue();

                String locID = (String) snapshot.child(Id).child("locationID").getValue();

                FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(locID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();

                                AppCompatActivity activity = (AppCompatActivity) getContext();
                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoMsgFragment()).addToBackStack(null).commit();
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

        else if (requestCode == 4) {
            Uri result = data.getData();

            // get the contact id from the Uri
            String id = result.getLastPathSegment();

            // query for phone numbers for the selected contact id
            Cursor c = getContext().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{id}, null);

            int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            if(c.getCount() > 1) { // contact has multiple phone numbers
                final CharSequence[] numbers = new CharSequence[c.getCount()];
                int i=0;
                if(c.moveToFirst()) {
                    final String name = c.getString(nameIndex);

                    while(!c.isAfterLast()) { // for each phone number, add it to the numbers array
                        String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                        String number = type + ": " + c.getString(phoneIdx);
                        numbers[i++] = number;
                        c.moveToNext();
                    }
                    // build and show a simple dialog that allows the user to select a number
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Select number:");
                    builder.setItems(numbers, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            String number = (String) numbers[item];
                            int index = number.indexOf(":");
                            number = number.substring(index + 2);

//                                loadContactInfo(number); // do something with the selected number
                            System.out.println("multinum"+number+name);

                            contactsName.add(name);
                            contactsNo.add(number);
                            contactsAdap.notifyDataSetChanged();

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setOwnerActivity(getActivity());
                    alert.show();

                }
            } else if(c.getCount() == 1) {
                // contact has a single phone number, so there's no need to display a second dialog
                if (c != null && c.moveToFirst()) {
                    String number = c.getString(phoneIdx);
                    String name = c.getString(nameIndex);
                    // Do something with the phone number
                    System.out.println("phone" + number+name);

                    contactsName.add(name);
                    contactsNo.add(number);
                    contactsAdap.notifyDataSetChanged();
                }
            }
        }

    }
}