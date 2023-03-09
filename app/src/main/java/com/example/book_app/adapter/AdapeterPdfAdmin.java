package com.example.book_app.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_app.MyApplication;
import com.example.book_app.activities.PdfDetailActivity;
import com.example.book_app.activities.PdfEditActivity;
import com.example.book_app.databinding.RowPdfAdminBinding;
import com.example.book_app.filters.FilterPdfAdmin;
import com.example.book_app.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapeterPdfAdmin extends RecyclerView.Adapter<AdapeterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;

    private static final String TAG="PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    public AdapeterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList=pdfArrayList;

        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Please Wait ");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        //get data
        ModelPdf model=pdfArrayList.get(position);
        String pdfId=model.getId();
        String categoryId= model.getCategoryId();
        String title=model.getTitle();
        String description=model.getDescription();
        String pdfUrl=model.getUrl();
        long timestamp=model.getTimestamp();
        //convert timestamp to dd/MM/yyyy
        String formattedDate= MyApplication.formatTimeStamp(timestamp);
        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
                );

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptiosnDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });



    }

    private void moreOptiosnDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId=model.getId();
        String bookUrl=model.getUrl();
        String bookTitle=model.getTitle();

        String[] option={"Edit", "Delete"};

        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Chose Option")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //edit book, open new activity to edit the book info
                            Intent intent= new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);

                        }
                        else if(which==1){
                            //delete book
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl ,
                                    ""+bookTitle
                            );

                        }

                    }
                })
                .show();
    }









    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter ==null){
            filter=new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;


        }
    }
}
