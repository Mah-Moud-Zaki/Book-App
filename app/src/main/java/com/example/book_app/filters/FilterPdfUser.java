package com.example.book_app.filters;

import android.widget.Filter;

import androidx.constraintlayout.widget.ConstraintSet;

import com.example.book_app.adapter.AdapterPdfUser;
import com.example.book_app.model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults=new FilterResults();

        if(constraint!=null||constraint.length()>0){
            constraint= constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels=new ArrayList<>();

            for (int i=0; i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            filterResults.count=filteredModels.size();
            filterResults.values=filteredModels;
        }
        else{
            filterResults.count=filterList.size();
            filterResults.values=filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterPdfUser.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        adapterPdfUser.notifyDataSetChanged();
    }
}
