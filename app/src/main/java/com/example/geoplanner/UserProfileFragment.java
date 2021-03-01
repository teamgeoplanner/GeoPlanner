package com.example.geoplanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    TextView displayName;
    EditText name, email;
    Button btnSave, btnChangePass;
    FloatingActionButton btnEditPhoto;
    CircleImageView profileImage;
    FirebaseUser fUser;
    StorageReference storageReference;

    int TAKE_IMAGE_CODE = 10001;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        displayName = view.findViewById(R.id.username);
        name = view.findViewById(R.id.txtName);
        email = view.findViewById(R.id.txtEmail);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePass = view.findViewById(R.id.btnChangePass);
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto);
        profileImage = view.findViewById(R.id.imageview_profile);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("profile_images").child(fUser.getUid() + ".jpg");

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("no profile image");
            }
        });

        displayName.setText(fUser.getDisplayName());
        name.setText(fUser.getDisplayName());
        email.setText(fUser.getEmail());


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                // update name if name is changed
                if(!(name.getText().toString().equals(fUser.getDisplayName()))) {

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.getText().toString()).build();

                    fUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_LONG).show();
                            String name = fUser.getDisplayName();
                            displayName.setText(name);

                    View navView = getActivity().findViewById(R.id.nav_view);

                    TextView textView = navView.findViewById(R.id.nav_uName);
                    textView.setText(name);

                        }
                    });
                }
            }
        });

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new ChangePassFragment()).addToBackStack(null).commit();
            }
        });

        btnEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(intent, TAKE_IMAGE_CODE);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TAKE_IMAGE_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
//                    profileImage.setImageURI(imageUri);

                    uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage

        final StorageReference fileRef = storageReference.child("profile_images").child(fUser.getUid() + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(getContext(), "Profile Photo Updated Successfully!", Toast.LENGTH_LONG).show();

                        Picasso.get().load(uri).into(profileImage);

                        View navView = getActivity().findViewById(R.id.nav_view);

                        CircleImageView circleImageView = navView.findViewById(R.id.profileImage);

                        Picasso.get().load(uri).into(circleImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Profile Photo Update Failed!", Toast.LENGTH_LONG).show();
            }
        });



    }
}