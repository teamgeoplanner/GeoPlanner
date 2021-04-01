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

public class autoMsgAdapter extends FirebaseRecyclerAdapter<model3, autoMsgAdapter.myviewholder> {

    DatabaseReference autoMsgReff = FirebaseDatabase.getInstance().getReference("AutoMessage");
    String locID;
    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    Double latitude,longitude;

    private Context context;

    public autoMsgAdapter(@NonNull FirebaseRecyclerOptions<model3> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final autoMsgAdapter.myviewholder holder, int position, @NonNull final model3 model3) {
        holder.autoMsgName.setText(model3.getMname());

        if(model3.getStatus().equals("on")) {
            holder.status.setChecked(true);
            holder.status.setText("On");
        }

        locID = model3.getLocationID();

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

                    Query sQuery = autoMsgReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

                    Query sQuery = autoMsgReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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

        holder.autoMsgClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Query mQuery = autoMsgReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos = holder.getAdapterPosition();
                        String id = getSnapshots().getSnapshot(pos).getKey();

                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoMsgDetailFragment(model3.getMname(),id)).addToBackStack(null).commit();
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
    public autoMsgAdapter.myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.automessage_single_row, parent, false);
        context = parent.getContext();
        return new myviewholder(view);
    }

    public class myviewholder extends RecyclerView.ViewHolder {

        TextView autoMsgName, locationName;
        CardView autoMsgClick;
        Switch status;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            autoMsgName = itemView.findViewById(R.id.txtAutoMessageName);
            locationName = itemView.findViewById(R.id.txtLocationName);
            autoMsgClick = itemView.findViewById(R.id.autoMessageArea);
            status = itemView.findViewById(R.id.switchStatus);
        }
    }
}
