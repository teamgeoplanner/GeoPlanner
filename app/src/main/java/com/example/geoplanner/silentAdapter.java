package com.example.geoplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class silentAdapter extends FirebaseRecyclerAdapter<model2, silentAdapter.myviewholder> {

    public silentAdapter(@NonNull FirebaseRecyclerOptions<model2> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull model2 model2) {
        holder.silentName.setText(model2.getSname());
        holder.locationName.setText(model2.getLocationID());
    }

    @NonNull
    @Override
    public silentAdapter.myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.silent_single_row, parent, false);
        return new myviewholder(view);
    }

    public class myviewholder extends RecyclerView.ViewHolder {

        TextView silentName, locationName;
        CardView silentClick;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            silentName = itemView.findViewById(R.id.txtSilentName);
            locationName = itemView.findViewById(R.id.txtLocationName);
            silentClick = itemView.findViewById(R.id.silentArea);


        }
    }
}
