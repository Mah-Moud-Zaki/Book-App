package com.example.book_app.filters;

import android.widget.Filter;

import com.example.book_app.adapter.AdapeterCategory;
import com.example.book_app.model.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    ArrayList<ModelCategory> filterList;
    AdapeterCategory adapeterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapeterCategory adapeterCategory) {
        this.filterList = filterList;
        this.adapeterCategory = adapeterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results=new FilterResults();

        if (constraint !=null&& constraint.length()>0){
            constraint=constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels=new ArrayList<>();

            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adapeterCategory.categoryArrayList=(ArrayList<ModelCategory>)results.values;
        adapeterCategory.notifyDataSetChanged();

    }
}
