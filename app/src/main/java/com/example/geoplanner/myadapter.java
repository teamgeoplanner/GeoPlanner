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

    public myadapter(@NonNull FirebaseRecyclerOptions<model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final myviewholder holder, final int position, @NonNull final model model) {
        holder.taskName.setText(model.getTName());
        holder.cb.setChecked(false);

        holder.taskClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TaskDetailFragment(model.getTName())).addToBackStack(null).commit();
            }
        });

        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(compoundButton.isChecked()) {

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

    public void copyItem(final int position) {

        Query uncheck = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");
        uncheck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deletedTask = getSnapshots().getSnapshot(position).getValue();
                deletedKey = getSnapshots().getSnapshot(position).getKey();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void undoItem(final int position) {
        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked");
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FirebaseDatabase.getInstance().getReference("Tasks")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("unchecked")
                                .child(deletedKey)
                                .setValue(deletedTask);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors.
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
