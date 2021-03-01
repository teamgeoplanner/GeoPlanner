package com.example.geoplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangePassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePassFragment extends Fragment {

    ImageView backButton;
    Button btnChangePass;
    EditText oldPass, newPass, confirmPass;

    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChangePassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangePassFragment newInstance(String param1, String param2) {
        ChangePassFragment fragment = new ChangePassFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_pass, container, false);

        btnChangePass = view.findViewById(R.id.btnUpdatePass);
        backButton = view.findViewById(R.id.btnBack);
        oldPass = view.findViewById(R.id.txtOldPass);
        newPass = view.findViewById(R.id.txtNewPass);
        confirmPass = view.findViewById(R.id.txtConfirmPass);

        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldpassword = oldPass.getText().toString().trim();
                String newpassword = newPass.getText().toString().trim();
                String confpassword = confirmPass.getText().toString().trim();

                if(TextUtils.isEmpty(oldpassword)){
                    oldPass.setError("Old Password Required!");
                    return;
                }

                if(TextUtils.isEmpty(newpassword)){
                    newPass.setError("Old Password Required!");
                    return;
                }

                if(newpassword.length() < 8){
                    newPass.setError("Minimum 8 characters required!");
                    return;
                }

                if(!newpassword.equals(confpassword)) {
                    confirmPass.setError("Password does not match!");
                    return;
                }

                updatePassword(oldpassword, newpassword);
            }
        });

        //Click event of Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragments_container, new UserProfileFragment()).addToBackStack(null).commit();
            }
        });

        return view;
    }

    public void updatePassword(String oldpass, final String newpass) {

        AuthCredential authCredential = EmailAuthProvider.getCredential(fUser.getEmail(), oldpass);

        fUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fUser.updatePassword(newpass)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Password Updated Successfully!", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Password Update Failed!", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Password is Incorrect!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}