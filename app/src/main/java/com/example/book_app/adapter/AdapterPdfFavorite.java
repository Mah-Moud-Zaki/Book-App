package com.example.book_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_app.MyApplication;
import com.example.book_app.activities.PdfDetailActivity;
import com.example.book_app.databinding.RowPdfFavoriteBinding;
import com.example.book_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Stack;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;


    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);
        
        loadBookDetails(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);

            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, model.getId());

            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId =model.getId();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String bookTitle=""+dataSnapshot.child("title").getValue();
                        String description=""+dataSnapshot.child("description").getValue();
                        String categoryId=""+dataSnapshot.child("categoryId").getValue();
                        String bookUrl=""+dataSnapshot.child("url").getValue();
                        String timestamp=""+dataSnapshot.child("timestamp").getValue();
                        String uid= ""+dataSnapshot.child("uid").getValue();
                        String viewCount=""+dataSnapshot.child("viewsCount").getValue();
                        String downloadsCount=""+dataSnapshot.child("downloadsCount").getValue();

                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setCategoryId(categoryId);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));


                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                holder.pdfView,
                                holder.progressBar,
                                null
                        );

                        MyApplication.loadCategory(
                                ""+categoryId,
                                holder.categoryTv
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                holder.sizeTv
                        );

                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            removeFavBtn = binding.removeFavBtn;
        }
    }
}
