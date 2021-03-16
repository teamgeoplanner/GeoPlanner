package com.example.geoplanner;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class myadapter extends FirebaseRecyclerAdapter<model, myadapter.myviewholder> {

    DatabaseReference taskReff = FirebaseDatabase.getInstance().getReference("Tasks");
    int newID;
    String locID;

    RecyclerView mRecyclerView;



    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public myadapter(@NonNull FirebaseRecyclerOptions<model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final myadapter.myviewholder holder, final int position, @NonNull final model model) {
        holder.taskName.setText(model.getTName());
        holder.cb.setChecked(false);

        holder.taskClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");
                uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int pos = holder.getAdapterPosition();
                                String id = getSnapshots().getSnapshot(pos).getKey();

                                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TaskDetailFragment(model.getTName(), id, "unchecked")).addToBackStack(null).commit();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


//                for ( int i = 0; i < holder.taskClick.getChildCount();  i++ ){
//                    View view = holder.taskClick.getChildAt(i);
//                    view.setEnabled(false);
////                    view.setVisibility(View.GONE); // Or whatever you want to do with the view.
//                }
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }


                if(compoundButton.isChecked()) {
                    for (int j = 0; j < myadapter.this.getItemCount(); j++) {
                        myadapter.myviewholder holder = (myadapter.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
                        holder.taskClick.setEnabled(false);

                        for ( int i = 0; i < holder.taskClick.getChildCount();  i++ ){
                            View view = holder.taskClick.getChildAt(i);
                            view.setEnabled(false);
                        }
                    }

                        Query query = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {


                                if (snapshot.hasChild("checked")) {

                                    Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked").orderByKey().limitToLast(1);
                                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                Log.d("User key", child.getKey());
                                                Log.d("User val", child.child("tname").getValue().toString());

                                                String id = child.getKey();
                                                newID = Integer.parseInt(id) + 1;

                                                System.out.println(newID);


                                                Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");

                                                uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        int pos = holder.getAdapterPosition();

                                                        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .child("checked")
                                                                .child(String.valueOf(newID))
                                                                .setValue(getSnapshots().getSnapshot(pos).getValue());

                                                        getSnapshots().getSnapshot(pos).getRef().removeValue();

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

                                    for (int j = 0; j < myadapter.this.getItemCount(); j++) {
                                        myadapter.myviewholder holder = (myadapter.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
                                        holder.taskClick.setEnabled(true);
                                        System.out.println(holder.taskClick);

                                        for ( int i = 0; i < holder.taskClick.getChildCount();  i++ ){
                                            View view = holder.taskClick.getChildAt(i);
                                            view.setEnabled(true);
                                        }
                                    }

                                }

                                else {

                                    Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");

                                    uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int pos = holder.getAdapterPosition();
                                            FirebaseDatabase.getInstance().getReference("Tasks")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("checked")
                                                    .child("1")
                                                    .setValue(getSnapshots().getSnapshot(pos).getValue());

                                            getSnapshots().getSnapshot(pos).getRef().removeValue();

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    for (int j = 0; j < myadapter.this.getItemCount(); j++) {
                                        myadapter.myviewholder holder = (myadapter.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
                                        holder.taskClick.setEnabled(true);

                                        for ( int i = 0; i < holder.taskClick.getChildCount();  i++ ){
                                            View view = holder.taskClick.getChildAt(i);
                                            view.setEnabled(true);
                                        }
                                    }


                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


            }
        });
    }

    public void deleteItem(final int position){

        Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");
        uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_singlerow, parent, false);
        return new myviewholder(view);
    }

    Object deletedTask = null;
    String deletedKey = null;

    Object deletedLocation = null;
    String deletedLocKey = null;

    public void copyItem(final int position) {

        Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");
        uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void undoItem(final int position) {
        FirebaseDatabase.getInstance().getReference("Tasks")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("unchecked")
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

    public class myviewholder extends RecyclerView.ViewHolder {

        TextView taskName;
        RelativeLayout taskClick;
        CheckBox cb;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            taskName = itemView.findViewById(R.id.txtTaskName);
            taskClick = itemView.findViewById(R.id.taskArea);
            cb = itemView.findViewById(R.id.chkBox);

        }
    }
}
