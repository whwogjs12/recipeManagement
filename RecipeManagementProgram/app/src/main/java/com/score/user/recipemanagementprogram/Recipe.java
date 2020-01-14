package com.score.user.recipemanagementprogram;

import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;


public class Recipe implements Comparable<Recipe>,Serializable
{
    private static final long serialVersionUID = 1L;
    private String recipeName;
    ArrayList<String> materialName = new ArrayList<>();
    ArrayList<Double> materialUsage = new ArrayList<>();
    ArrayList<Double> materialPrice = new ArrayList<>();
    private double recipePrice=0;
    boolean useToCheck = false;

    public Recipe(String name)
    {
        recipeName = name;
    }

    public boolean addMaterial(String materialName, double materialUsage, double materialPrice, int index)
    {
        if(index>=this.materialName.size())
        {
            return false;
        }
        else if(index<0)
        {
            this.materialName.add(materialName);
            this.materialUsage.add(materialUsage);
            this.materialPrice.add(materialPrice);
            recipePrice = allPrice();
            useToCheck=true;
            return true;
        }
        else
        {
            this.materialName.add(index,materialName);
            this.materialUsage.add(index,materialUsage);
            this.materialPrice.add(index,materialPrice);
            recipePrice = allPrice();
            useToCheck=true;
            return true;
        }

    }

    public void modifyMaterial(int fixNumber,String nameToFix, double usageToFix, double priceToFix)
    {
        this.materialName.set(fixNumber,nameToFix);
        this.materialUsage.set(fixNumber,usageToFix);
        this.materialPrice.set(fixNumber,priceToFix);
        recipePrice = allPrice();
        useToCheck=true;
    }

    public String deleteMaterial(int index)
    {
        if(index<0 || index>=materialName.size())
        {
            return null;
        }
        else
        {
            String deletedName = this.materialName.get(index);
            this.materialName.remove(index);
            this.materialUsage.remove(index);
            this.materialPrice.remove(index);
            recipePrice = allPrice();
            useToCheck=true;
            return deletedName;
        }
    }


    public double allPrice()
    {
        double PlusAll = 0;
        for(int i=0;i<materialPrice.size();i++)
        {
            PlusAll+=materialPrice.get(i);
        }
        return PlusAll;
    }

    public String getRecipeName() {return recipeName;}

    public double getRecipeUsage()
    {
        double usage = 0;
        for(int i=0;i<materialUsage.size();i++)
        {
            usage+=materialUsage.get(i);
        }
        return usage;
    }

    public void rePriceSet() {recipePrice = allPrice();}
    public double getRecipePrice() {return recipePrice;}

    public int getCostQuantity() {return materialName.size();}

    @Override
    public int compareTo(@NonNull Recipe recipe) {
        if(this.recipeName.compareTo(recipe.getRecipeName())>0)
        {
            return 1;
        }
        else if(this.recipeName.compareTo(recipe.getRecipeName())==0)
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
