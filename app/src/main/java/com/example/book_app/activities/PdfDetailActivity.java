package com.example.book_app.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.MyApplication;
import com.example.book_app.R;
import com.example.book_app.adapter.AdapterComment;
import com.example.book_app.adapter.AdapterPdfFavorite;
import com.example.book_app.databinding.ActivityPdfDetailBinding;
import com.example.book_app.databinding.DialogCommentAddBinding;
import com.example.book_app.model.ModelComment;
import com.example.book_app.model.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    String bookId, bookTitle, bookUrl;

    boolean isInMyFavorite=false;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        bookId=intent.getStringExtra("bookId");

        binding.downloadBookBtn.setVisibility(View.GONE);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null){
            checkIsFavorite();
        }
        
        loadBookDetails();
        loadComment();
        MyApplication.incrementBookViewCount(bookId);



        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    MyApplication.downloadBook(PdfDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser()==null){
                    Toast.makeText(PdfDetailActivity.this, "you're not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isInMyFavorite){
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                    }
                    else {
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser()==null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in...", Toast.LENGTH_SHORT).show();
                }
                else{
                    addCommentDialog();
                }
            }
        });
    }

    private void loadComment() {
        commentArrayList=new ArrayList<>();

        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        commentArrayList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelComment model = ds.getValue(ModelComment.class);
                            commentArrayList.add(model);
                        }
                        adapterComment=new AdapterComment(PdfDetailActivity.this, commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private String comment="";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding=DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment=commentAddBinding.commentEt.getText().toString().trim();

                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Enter your Comment...", Toast.LENGTH_SHORT).show();
                }
                else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        String timestamp=""+System.currentTimeMillis();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private ActivityResultLauncher<String> requestPermissionLauncher=
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted -> {
                if(isGranted){
                    MyApplication.downloadBook(this,""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else {
                    Toast.makeText(this, "Permission was denied... ", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        bookTitle=""+dataSnapshot.child("title").getValue();
                        String description=""+dataSnapshot.child("description").getValue();
                        String categoryId=""+dataSnapshot.child("categoryId").getValue();
                        String viewsCount=""+dataSnapshot.child("viewsCount").getValue();
                        String downloadsCount=""+dataSnapshot.child("downloadsCount").getValue();
                        bookUrl=""+dataSnapshot.child("url").getValue();
                        String timestamp=""+dataSnapshot.child("timestamp").getValue();

                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        String date=MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );



                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void checkIsFavorite(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        isInMyFavorite=dataSnapshot.exists();
                        if(isInMyFavorite){
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_whit,0,0);
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_whit,0,0);
                            binding.favoriteBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}