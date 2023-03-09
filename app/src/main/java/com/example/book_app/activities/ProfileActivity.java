package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.book_app.MyApplication;
import com.example.book_app.R;
import com.example.book_app.adapter.AdapterPdfFavorite;
import com.example.book_app.databinding.ActivityProfileBinding;
import com.example.book_app.model.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.accountTypesTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.favoriteBookCountTv.setText("N/A");
        binding.accountStatusTv.setText("N/A");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();
        loadFavoriteBooks();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,ProfileEditActivity.class));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser.isEmailVerified()){
                    Toast.makeText(ProfileActivity.this, "Already Verified...", Toast.LENGTH_SHORT).show();
                }
                else {
                    emailVerificationDialog();
                }
            }
        });

    }

    private void emailVerificationDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Verify Email")
                .setMessage("Are you sure you want to send email verification instruction to your email"+firebaseUser.getEmail())
                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Sending Email verification instruction to your email"+firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Instruction sent, check your email"+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserInfo(){

        if(firebaseUser.isEmailVerified()){
            binding.accountStatusTv.setText("Verified");
        }
        else{
            binding.accountStatusTv.setText("Not Verified");
        }

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email=""+dataSnapshot.child("email").getValue();
                        String name=""+dataSnapshot.child("Name").getValue();
                        String profileImage=""+dataSnapshot.child("profileImage").getValue();
                        String timestamp=""+dataSnapshot.child("timestamp").getValue();
                        String uid=""+dataSnapshot.child("uid").getValue();
                        String userType=""+dataSnapshot.child("userType").getValue();

                        String formattedDate= MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypesTv.setText(userType);

                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileIv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadFavoriteBooks(){

        pdfArrayList=new ArrayList<>();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pdfArrayList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            String bookId=""+ds.child("bookId").getValue();
                            ModelPdf modelPdf=new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);
                        }
                        binding.favoriteBookCountTv.setText(""+pdfArrayList.size());

                        adapterPdfFavorite=new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}