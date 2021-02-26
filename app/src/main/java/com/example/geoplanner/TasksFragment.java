package com.example.geoplanner;

import android.content.ClipData;
import android.graphics.Canvas;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.SnapshotHolder;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TasksFragment extends Fragment {
    ImageButton btnAdd;
    RecyclerView recyclerView1, recyclerView2;
    static myadapter adapter1;
    myadapter2 adapter2;
    ConcatAdapter concatAdapter;
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


        recyclerView1 = view.findViewById(R.id.recTasks);
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));

        fAuth = FirebaseAuth.getInstance();


        FirebaseRecyclerOptions<model> options1 =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("unchecked"), model.class)
                        .build();


        adapter1 = new myadapter(options1);

        FirebaseRecyclerOptions<model> options2 =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("checked"), model.class)
                        .build();

        adapter2 = new myadapter2(options2);

        concatAdapter = new ConcatAdapter(adapter1,adapter2);

        recyclerView1.setAdapter(concatAdapter);


//        recyclerView2 = view.findViewById(R.id.recTasks);
//        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
//
//
//        FirebaseRecyclerOptions<model> options2 =
//                new FirebaseRecyclerOptions.Builder<model>()
//                        .setQuery(FirebaseDatabase.getInstance().getReference("Tasks").child(fAuth.getCurrentUser().getUid()).child("checked"), model.class)
//                        .build();
//
//        adapter2 = new myadapter2(options2);
//        recyclerView2.setAdapter(adapter2);


        taskReff = FirebaseDatabase.getInstance().getReference("Tasks");

        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked").addValueEventListener(new ValueEventListener() {
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

                //Bottom sheet view for adding tasks

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

                        addData(taskObj);


                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView1);



        return view;
    }


    //Swipe left or right to delete function
    final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();

            if (viewHolder.getBindingAdapter() == adapter1) {
                adapter1.copyItem(position);

                adapter1.deleteItem(position);

                Snackbar.make(recyclerView1,"Task Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter1.undoItem(position);
                            }
                        }).show();
            }

            else if(viewHolder.getBindingAdapter() == adapter2) {
                adapter2.copyItem(position);

                adapter2.deleteItem(position);

                Snackbar.make(recyclerView1,"Task Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter2.undoItem(position);
                            }
                        }).show();
            }



//            System.out.println(viewHolder.getBindingAdapter());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                    .addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    public void addData(final model taskObj) {
        taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("unchecked")) {

                    Query lastQuery = taskReff.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("unchecked").orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                String taskID = dataSnapshot.getValue().toString();
//                                String id = taskID.substring(1,2);
//                                System.out.println(id);
//                                newID = Integer.parseInt(id) + 1;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Log.d("User key", child.getKey());
                                Log.d("User val", child.child("tname").getValue().toString());

                                String id = child.getKey();
                                newID = Integer.parseInt(id) + 1;

                                System.out.println(newID);

                                FirebaseDatabase.getInstance().getReference("Tasks")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("unchecked")
                                        .child(String.valueOf(newID))
                                        .setValue(taskObj);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

                else {
                    FirebaseDatabase.getInstance().getReference("Tasks")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("unchecked")
                            .child("1")
                            .setValue(taskObj);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter1.startListening();
        adapter2.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter1.stopListening();
        adapter2.stopListening();
    }

}
