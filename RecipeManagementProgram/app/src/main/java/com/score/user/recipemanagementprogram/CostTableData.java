package com.score.user.recipemanagementprogram;


import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class CostTableData
{
    ArrayList<String> costName = new ArrayList<String>();
    ArrayList<Double> costPrice = new ArrayList<Double>();
    ArrayList<Double> costWeight = new ArrayList<Double>();
    ArrayList<Double> pricePerGram = new ArrayList<Double>();
    ArrayList<String> usesCheck = new ArrayList<String>();

    public CostTableData() {}

    public boolean add(String costName, double costPrice, double costWeight, String usesToCheck)
    {
        for(int i=0;i<this.costName.size();i++)
        {
            String tempName = this.costName.get(i);
            if(tempName.equals(costName))
            {
                return false;
            }
        }
        int sortIndex = nameSort(costName);
        if(sortIndex==-1)
        {
            this.costName.add(costName);
            this.costPrice.add(costPrice);
            this.costWeight.add(costWeight);
            pricePerGram.add((double) Math.round((costPrice/costWeight*100))/100.0);
            this.usesCheck.add(usesToCheck);
        }
        else
        {
            this.costName.add(sortIndex,costName);
            this.costPrice.add(sortIndex,costPrice);
            this.costWeight.add(sortIndex,costWeight);
            pricePerGram.add(sortIndex,(double) Math.round((costPrice/costWeight*100))/100.0);
            this.usesCheck.add(sortIndex,usesToCheck);
        }
        return true;
    }

    public void delete(int index)
    {
        if(index<costName.size() )
        {
            costName.remove(index);
            costPrice.remove(index);
            costWeight.remove(index);
            pricePerGram.remove(index);
            this.usesCheck.remove(index);
        }
    }

    public void adjust(int index,double price, double weight)
    {
        if(index<costName.size())
        {
            costPrice.set(index,price);
            costWeight.set(index,weight);
            pricePerGram.set(index,(Math.round(price/weight*100))/100.0);
            this.usesCheck.set(index,"true");
        }
    }

    public int nameSort(String name)
    {
        for(int i=0;i<this.costName.size();i++)
        {
            if(this.costName.get(i).compareTo(name)>0)
            {
                return i;
            }
            else {continue;}
        }
        return -1;
    }

    public void addDB(DbOpenHelper DOH)
    {
        Cursor c = DOH.selectColumns();
        while (c.moveToNext())
        {
            String name = c.getString(0);
            double costPrice = c.getDouble(1);
            double costWeight = c.getDouble(2);
            String usesToCheck = c.getString(4);
            add(name,costPrice,costWeight,usesToCheck);
        }
    }
    public int CTDNameOf(String name) { return costName.indexOf(name); }
}
