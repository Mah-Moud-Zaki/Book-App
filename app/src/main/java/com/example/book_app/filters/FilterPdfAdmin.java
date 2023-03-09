package com.example.book_app.filters;

import android.widget.Filter;

import com.example.book_app.adapter.AdapeterCategory;
import com.example.book_app.adapter.AdapeterPdfAdmin;
import com.example.book_app.model.ModelCategory;
import com.example.book_app.model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    ArrayList<ModelPdf> filterList;
    AdapeterPdfAdmin adapeterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapeterPdfAdmin adapeterPdfAdmin) {
        this.filterList = filterList;
        this.adapeterPdfAdmin = adapeterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results=new FilterResults();

        if (constraint !=null&& constraint.length()>0){
            constraint=constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels=new ArrayList<>();

            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;
        }
        else{
            results.count=filterList.size();
            results.values=filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapeterPdfAdmin.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        adapeterPdfAdmin.notifyDataSetChanged();

    }
}
