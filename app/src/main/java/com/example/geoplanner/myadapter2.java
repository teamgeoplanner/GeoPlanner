package com.example.geoplanner;

import android.graphics.Paint;
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

public class myadapter2 extends FirebaseRecyclerAdapter<model, myadapter2.myviewholder> {

    DatabaseReference taskReff = FirebaseDatabase.getInstance().getReference("Tasks");
    DatabaseReference locReff = FirebaseDatabase.getInstance().getReference("Location").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    int newID;
    String locID;

    RecyclerView mRecyclerView;

    myadapter getAdapter = TasksFragment.getAdap();

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public myadapter2(@NonNull FirebaseRecyclerOptions<model> options) {
        super(options);
    }

    protected void onBindViewHolder(@NonNull final myadapter2.myviewholder holder, final int position, @NonNull final model model) {
        holder.taskName.setText(model.getTName());
        holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.cb.setChecked(true);

        holder.taskClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Query check = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked");
                check.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos = holder.getAdapterPosition();
                        System.out.println("pos" + pos);
                        String id = getSnapshots().getSnapshot(pos).getKey();

                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TaskDetailFragment(model.getTName(), id, "checked")).addToBackStack(null).commit();
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

                if(!compoundButton.isChecked()) {
                    int sum = getAdapter.getItemCount() + myadapter2.this.getItemCount();

                    for (int j = getAdapter.getItemCount(); j < sum; j++) {
                        myviewholder holder = (myadapter2.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
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


                            if (snapshot.hasChild("unchecked")) {

                                Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked").orderByKey().limitToLast(1);
                                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            Log.d("User key", child.getKey());
                                            Log.d("User val", child.child("tname").getValue().toString());

                                            String id = child.getKey();
                                            newID = Integer.parseInt(id) + 1;

                                            System.out.println(newID);

                                            Query check = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked");

                                            check.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    System.out.println("on data change 2");
                                                    int pos = holder.getAdapterPosition();

                                                    taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("unchecked")
                                                            .child(String.valueOf(newID))
                                                            .setValue(getSnapshots().getSnapshot(pos).getValue());

                                                    final String locId = getSnapshots().getSnapshot(pos).child("locationID").getValue().toString();

                                                    getSnapshots().getSnapshot(pos).getRef().removeValue();

                                                    locReff.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Object locData = snapshot.child(locId).getValue();
                                                            snapshot.child(locId).getRef().removeValue();

                                                            locReff.child(locId).setValue(locData);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });


                                                    MyBackgroundService.removeUncheckedId(String.valueOf(newID));
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
                                int sum = getAdapter.getItemCount() + myadapter2.this.getItemCount();

                                for (int j = getAdapter.getItemCount(); j < sum; j++) {
                                    myadapter2.myviewholder holder = (myadapter2.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
                                    holder.taskClick.setEnabled(true);

                                    for ( int i = 0; i < holder.taskClick.getChildCount();  i++ ){
                                        View view = holder.taskClick.getChildAt(i);
                                        view.setEnabled(true);
                                    }
                                }

                            }

                            else {

                                Query check = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked");

                                check.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        System.out.println("on data change 2");
                                        int pos = holder.getAdapterPosition();

                                        FirebaseDatabase.getInstance().getReference("Tasks")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("unchecked")
                                                .child("1")
                                                .setValue(getSnapshots().getSnapshot(pos).getValue());

                                        final String locId = getSnapshots().getSnapshot(pos).child("locationID").getValue().toString();
                                        
                                        getSnapshots().getSnapshot(pos).getRef().removeValue();

                                        locReff.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Object locData = snapshot.child(locId).getValue();
                                                snapshot.child(locId).getRef().removeValue();

                                                locReff.child(locId).setValue(locData);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        MyBackgroundService.removeUncheckedId("1");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                int sum = getAdapter.getItemCount() + myadapter2.this.getItemCount();

                                for (int j = getAdapter.getItemCount(); j < sum; j++) {
                                    myadapter2.myviewholder holder = (myadapter2.myviewholder) mRecyclerView.findViewHolderForAdapterPosition(j);
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

        Query check = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked");

        check.addListenerForSingleValueEvent(new ValueEventListener() {
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

    Object deletedTask = null;
    String deletedKey = null;

    Object deletedLocation = null;
    String deletedLocKey = null;

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_singlerow, parent, false);
        return new myviewholder(view);
    }

    public void copyItem(final int position) {
        Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("checked");
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
                .child("checked")
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


