package com.example.geoplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    TextView name, emailID;
    DatabaseReference reff;     //Create reference variable for database
    CircleImageView profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        NavigationView nav_view = findViewById(R.id.nav_view);
        View header = nav_view.getHeaderView(0);
        name = header.findViewById(R.id.nav_uName);
        emailID = header.findViewById(R.id.nav_email);
        profileImg = header.findViewById(R.id.profileImage);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        name.setText(fAuth.getCurrentUser().getDisplayName());
        emailID.setText(fAuth.getCurrentUser().getEmail());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("profile_images").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("no profile image");
            }
        });

//        reff = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        reff.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String uname = snapshot.child("name").getValue().toString();
//                String uemail = snapshot.child("email").getValue().toString();
//
//                System.out.println(uname);
//                System.out.println(uemail);
//
//                name.setText(uname);
//                emailID.setText(uemail);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);     //Geoplanner shown in actionbar

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Create toggle in Action Bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);       //Add toggle to drawer
        toggle.syncState();     //Rotate toggle while opening Navigation Panel

        //Open TaskFragment by default
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TasksFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_tasks);
        }
    }


    //Click event when any item of Navigation Drawer is clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Check which item is clicked and open Fragment
        switch (item.getItemId()){
            case R.id.nav_tasks:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new TasksFragment()).commit();
                break;
            case R.id.nav_automsg:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoMsgFragment()).commit();
                break;
            case R.id.nav_autosilent:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new AutoSilentFragment()).commit();
                break;
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new UserProfileFragment()).commit();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Event when user clicks Back button
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}