package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private Uri pdfUri=null;
    private static final String TAG="ADD_PDF_TAG";
    private static final int PDF_PICK_CODE=1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        loadPdfCategory();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatedata();
            }
        });


    }
    private String title="", description="";
    private void validatedata() {

        Log.d(TAG, "validatedata: Validating Data...");

        title=binding.titleEt.getText().toString().trim();
        description=binding.descriptionEt.getText().toString().trim();


        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri==null){
            Toast.makeText(this, "pick PDF...", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: Uploading to Storage...");

        progressDialog.setMessage("Uploading pdf...");
        progressDialog.show();

        long timestamp=System.currentTimeMillis();

        String filePathAndName="Books/"+timestamp;
        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded success...");
                        Log.d(TAG, "onSuccess: getting Pdf url");

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadPdfUrl=""+uriTask.getResult();

                        uploadPdfInfoToDb(uploadPdfUrl,timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed du to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        
    }

    private void uploadPdfInfoToDb(String uploadPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfToStorage: Uploading pdf info to firebase dp...");
        progressDialog.setMessage("Uploading pdf info...");

        String uid =firebaseAuth.getUid();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "successfully uploaded", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "failed to upload to dp due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }


    private void loadPdfCategory() {
        Log.d(TAG, "loadPdfCategory: loading pdf categories...");
        categoryTitleArrayList =new ArrayList<>();
        categoryIdArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){

                    String categoryId=""+ds.child("id").getValue();
                    String categoryTitle=""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId,selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        String [] categoriesArray=new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i]= categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectedCategoryTitle=categoryTitleArrayList.get(which);
                        selectedCategoryId=categoryIdArrayList.get(which);
                        binding.categoryTv.setText(selectedCategoryTitle);
                        Log.d(TAG, "onClick: Selected Category"+selectedCategoryId+" "+selectedCategoryTitle);

                    }
                })
                .show();


    }

    private void pdfPickIntent() {

        Log.d(TAG, "pdfPickIntent: string pdf pick intent ");

        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF picked");
                pdfUri=data.getData();
                Log.d(TAG, "onActivityResult: URI "+pdfUri);
            }

        }
        else{
            Log.d(TAG, "onActivityResult: cancelled piking pdf");
            Toast.makeText(this, "cancelled piking pdf", Toast.LENGTH_SHORT).show();

        }
    }
}