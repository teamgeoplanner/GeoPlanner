package com.example.geoplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
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


        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS
                ))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(!n.isNotificationPolicyAccessGranted()) {
                                // Ask the user to grant access
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        System.out.println("permission:"+permissions);
//                        if(permissions.toString().equals("[Permission name: android.permission.ACCESS_BACKGROUND_LOCATION]") || permissions.toString().equals("[Permission name: android.permission.ACCESS_COARSE_LOCATION, Permission name: android.permission.ACCESS_FINE_LOCATION]")) {
//                            System.out.println("permission:"+permissions);
//
//
//                            AlertDialog.Builder adb = new AlertDialog.Builder(MainPageActivity.this);
//                            adb.setTitle("Title of alert dialog");
//                            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                    intent.setData(Uri.parse("package:" + getPackageName()));
//                                    startActivity(intent);
//                                }
//                            });
//                            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    finish();
//                                }
//                            });
//                            adb.show();
//                        }
                    }
                }).check();



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
            case R.id.nav_background:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new StartBackgroundFragment()).commit();
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

    boolean doubleBackToExitPressedOnce = false;

    //Event when user clicks Back button
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
}