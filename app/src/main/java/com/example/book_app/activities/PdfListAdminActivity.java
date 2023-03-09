package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.book_app.adapter.AdapeterPdfAdmin;
import com.example.book_app.databinding.ActivityPdfAddBinding;
import com.example.book_app.databinding.ActivityPdfListAdminBinding;
import com.example.book_app.model.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapeterPdfAdmin adapeterPdfAdmin;
    private String categoryId,categoryTitle;

    private  static final String TAG="PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent=getIntent();
        categoryId=intent.getStringExtra("categoryId");
        categoryTitle=intent.getStringExtra("categoryTitle");

        binding.supTitleTv.setText(categoryTitle);

        loadPdfList();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapeterPdfAdmin.getFilter().filter(s);

                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {

        pdfArrayList=new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            pdfArrayList.add(model);
                            Log.d(TAG, "onDataChange: "+model.getId()+""+model.getTitle());
                        }

                        adapeterPdfAdmin=new AdapeterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter(adapeterPdfAdmin);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}