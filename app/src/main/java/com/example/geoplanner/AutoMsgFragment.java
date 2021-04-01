package com.example.geoplanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class AutoMsgFragment extends Fragment {
    ImageButton btnAdd;
    Button btnAddContact;

    TextView locationName;
    EditText txtAutoMessage;
    EditText message;

    LatLng location;
    String newLocID;
    int newID;

    DatabaseReference autoMessageReff;
    FirebaseAuth fAuth;

    RecyclerView recyclerView;
    autoMsgAdapter adapter;

    RecyclerView recyclerViewContacts;
    contactsAdapter contactsAdap;

    ArrayList<String> contactsName = new ArrayList<>();
    public static ArrayList<String> contactsNo = new ArrayList<>();

    Spinner spinner;
    String[] paths = {"entry", "exit"};

    String entryexit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //link with xml file
        View view = inflater.inflate(R.layout.fragment_automsg, container,false);

        recyclerView = view.findViewById(R.id.recAutoMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fAuth = FirebaseAuth.getInstance();


        FirebaseRecyclerOptions<model3> options =
                new FirebaseRecyclerOptions.Builder<model3>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("AutoMessage").child(fAuth.getCurrentUser().getUid()), model3.class)
                        .build();


        adapter = new autoMsgAdapter(options);

        recyclerView.setAdapter(adapter);

        btnAdd = view.findViewById(R.id.btnAddAutoMessage);
        autoMessageReff = FirebaseDatabase.getInstance().getReference("AutoMessage");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int[] i = {0};
                entryexit = "enter";

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

                //Bottom sheet view for adding tasks

                final View bottomSheetView = LayoutInflater.from(getContext())
                        .inflate(R.layout.automessage_bottom_sheet, (LinearLayout)view.findViewById(R.id.autoMsgBottomSheetContainer));


                bottomSheetView.findViewById(R.id.btnAddLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Location", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivityForResult(intent, 2);

                    }
                });


                locationName = bottomSheetView.findViewById(R.id.textLocation);
                txtAutoMessage = bottomSheetView.findViewById(R.id.txtAutoMsg);
                message = bottomSheetView.findViewById(R.id.txtMessage);

                bottomSheetView.findViewById(R.id.btnSaveAutoMsg).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        addLocation();

                        bottomSheetDialog.dismiss();
                    }
                });

                recyclerViewContacts = bottomSheetView.findViewById(R.id.recContacts);
                recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

                contactsAdap = new contactsAdapter(getContext(), contactsName);

                recyclerViewContacts.setAdapter(contactsAdap);

                bottomSheetView.findViewById(R.id.btnAddContact).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        contactsName.add("contact"+ i[0]);
//                        contactsNo.add(String.valueOf(i[0]));
//                        System.out.println(contactsNo);
//                        i[0]++;
//                        contactsAdap.notifyDataSetChanged();

                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, 4);
                    }
                });

                spinner = (Spinner)bottomSheetView.findViewById(R.id.onEnterExit);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        R.layout.spinner_list, paths);

                adapter.setDropDownViewResource(R.layout.spinner_list);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0:
                                entryexit = "enter";
                                break;

                            case 1:
                                entryexit = "exit";
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

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

                                            final model3 automsgObj = new model3(txtAutoMessage.getText().toString(), String.valueOf(newLocID), message.getText().toString(), entryexit, "on");

                                            addData(automsgObj);

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

                        final model3 automsgObj = new model3(txtAutoMessage.getText().toString(), String.valueOf(newLocID), message.getText().toString(), entryexit, "on");

                        addData(automsgObj);

                        location = null;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            newLocID = null;

            final model3 automsgObj = new model3(txtAutoMessage.getText().toString(), String.valueOf(newLocID), message.getText().toString(), entryexit, "on");

            addData(automsgObj);
        }

    }

    public void addData(final model3 automsgObj) {
        autoMessageReff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                    Query lastQuery = autoMessageReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                String taskID = dataSnapshot.getValue().toString();
//                                String id = taskID.substring(1,2);
                            System.out.println("on data change 2");
//                                newID = Integer.parseInt(id) + 1;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Log.d("User key", child.getKey());
                                Log.d("User val", child.child("mname").getValue().toString());

                                String id = child.getKey();
                                newID = Integer.parseInt(id) + 1;

                                System.out.println(newID);

                                FirebaseDatabase.getInstance().getReference("AutoMessage")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(String.valueOf(newID))
                                        .setValue(automsgObj);

                                for(int i = 0 ; i < contactsName.size() ; i++) {
                                    autoMessageReff
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(String.valueOf(newID))
                                            .child("contacts")
                                            .child(String.valueOf(i))
                                            .child("number")
                                            .setValue(contactsNo.get(i));

                                    autoMessageReff
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(String.valueOf(newID))
                                            .child("contacts")
                                            .child(String.valueOf(i))
                                            .child("name")
                                            .setValue(contactsName.get(i));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

                else {
                    FirebaseDatabase.getInstance().getReference("AutoMessage")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("1")
                            .setValue(automsgObj);
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

        else if (requestCode == 4) {
            if(data==null)
                return;

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
