package com.example.geoplanner;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class silentAdapter extends FirebaseRecyclerAdapter<model2, silentAdapter.myviewholder> {

    DatabaseReference silentReff = FirebaseDatabase.getInstance().getReference("Silent");
    String locID;
    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    Double latitude,longitude;
    private Context context;

    public silentAdapter(@NonNull FirebaseRecyclerOptions<model2> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final myviewholder holder, int position, @NonNull final model2 model2) {
        holder.silentName.setText(model2.getSname());

        if(model2.getStatus().equals("on")) {
            holder.status.setChecked(true);
            holder.status.setText("On");
        }

        locID =model2.getLocationID();

        locReff.child(locID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                latitude = (Double) snapshot.child("latitude").getValue();
                longitude = (Double) snapshot.child("longitude").getValue();

                if(latitude != null && longitude != null) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                    List<Address> addresses  = null;
                    try {
                        addresses = geocoder.getFromLocation(latitude ,longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String address = addresses.get(0).getAddressLine(0);

                    holder.locationName.setText(address);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(holder.status.isChecked()) {
                    holder.status.setText("On");

                    Query sQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pos = holder.getAdapterPosition();
                            getSnapshots().getSnapshot(pos).getRef().child("status").setValue("on");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    holder.status.setText("Off");

                    Query sQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pos = holder.getAdapterPosition();
                            getSnapshots().getSnapshot(pos).getRef().child("status").setValue("off");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        holder.silentClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Query sQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos = holder.getAdapterPosition();
                        String id = getSnapshots().getSnapshot(pos).getKey();

                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new SilentDetailFragment(model2.getSname(),id)).addToBackStack(null).commit();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public silentAdapter.myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.silent_single_row, parent, false);
        context = parent.getContext();
        return new myviewholder(view);
    }

    public class myviewholder extends RecyclerView.ViewHolder {

        TextView silentName, locationName;
        CardView silentClick;
        Switch status;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            silentName = itemView.findViewById(R.id.txtSilentName);
            locationName = itemView.findViewById(R.id.txtLocationName);
            silentClick = itemView.findViewById(R.id.silentArea);
            status = itemView.findViewById(R.id.switchStatus);
        }
    }

    Object deletedTask = null;
    String deletedKey = null;

    Object deletedLocation = null;
    String deletedLocKey = null;

    public void copyItem(final int position) {

        Query sQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deletedTask = getSnapshots().getSnapshot(position).getValue();
                deletedKey = getSnapshots().getSnapshot(position).getKey();

                locID = (String) getSnapshots().getSnapshot(position).child("locationID").getValue();

                FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(locID)) {
                                    deletedLocation = snapshot.getValue();
                                    deletedLocKey = locID;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                deleteItem(position);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteItem(final int position){

        Query sQuery = silentReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        sQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String id = getSnapshots().getSnapshot(position).getKey();
                if(id.equals(callServiceReceiver.id)) {
                    callServiceReceiver.silentService = false;
                    callServiceReceiver.message = null;
                    callServiceReceiver.id = null;
                }
                getSnapshots().getSnapshot(position).getRef().removeValue();

                locID = (String) getSnapshots().getSnapshot(position).child("locationID").getValue();

                FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(locID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().removeValue();
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

    public void undoItem(final int position) {
        FirebaseDatabase.getInstance().getReference("Silent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(deletedKey)
                .setValue(deletedTask);

        deletedTask = null;
        deletedKey = null;

        if(deletedLocation!=null && deletedLocKey!=null) {
            FirebaseDatabase.getInstance().getReference("Location")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(deletedLocation);

            deletedLocation = null;
            deletedLocKey = null;
        }
    }

    public void clearDeleted() {
        deletedTask = null;
        deletedKey = null;

        deletedLocation = null;
        deletedLocKey = null;
    }

}
