package com.example.geoplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class TasksFragment extends Fragment {
    ImageButton btnAdd;
    RecyclerView recyclerView;
    myadapter adapter;
    FirebaseAuth fAuth;
    EditText txtTask;
    DatabaseReference taskReff;
    int countTasks = 0;
    int newID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //link with xml file
        View view = inflater.inflate(R.layout.fragment_tasks, container,false);

        recyclerView = view.findViewById(R.id.recTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fAuth = FirebaseAuth.getInstance();

        FirebaseRecyclerOptions<model> options =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()), model.class)
                        .build();

        adapter = new myadapter(options);
        recyclerView.setAdapter(adapter);


        taskReff = FirebaseDatabase.getInstance().getReference("Tasks");

        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    countTasks = (int) snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnAdd = view.findViewById(R.id.btnAddTask);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

                View bottomSheetView = LayoutInflater.from(getContext())
                        .inflate(R.layout.layout_bottom_sheet, (LinearLayout)view.findViewById(R.id.bottomSheetContainer));

                bottomSheetView.findViewById(R.id.btnAddDate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Date", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetView.findViewById(R.id.btnAddLocation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "Add Location", Toast.LENGTH_SHORT).show();
                    }
                });

                txtTask = bottomSheetView.findViewById(R.id.txtTask);

                bottomSheetView.findViewById(R.id.btnSaveTask).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final model taskObj = new model(txtTask.getText().toString());


//                        int taskNo = countTasks + 1;
//
//                        FirebaseDatabase.getInstance().getReference("Tasks")
//                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                .child(String.valueOf(taskNo))
//                                .setValue(taskObj);
//
//                        taskNo = 0;

                        Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByKey().limitToLast(1);
                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                String taskID = dataSnapshot.getValue().toString();
//                                String id = taskID.substring(1,2);
//                                System.out.println(id);
//                                newID = Integer.parseInt(id) + 1;

                                for (DataSnapshot child: dataSnapshot.getChildren()) {
                                    Log.d("User key", child.getKey());
                                    Log.d("User val", child.child("tname").getValue().toString());

                                    String id = child.getKey();
                                    newID = Integer.parseInt(id) + 1;

                                    System.out.println(newID);

                                    FirebaseDatabase.getInstance().getReference("Tasks")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(String.valueOf(newID))
                                            .setValue(taskObj);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle possible errors.
                            }
                        });



                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        return view;
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
