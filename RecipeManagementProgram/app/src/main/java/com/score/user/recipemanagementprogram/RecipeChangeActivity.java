package com.score.user.recipemanagementprogram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class RecipeChangeActivity extends AppCompatActivity
{
    private DbOpenHelper mDbOpenHelper;
    CostTableData CTD = new CostTableData();
    ArrayList<String> recipeList;
    ArrayList<Recipe> recipes;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_change);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();
        CTD.addDB(mDbOpenHelper);
        addCostDropBox(CTD.costName);
        recipeList = getIntent().getStringArrayListExtra("recipeNameList");
        recipes = (ArrayList<Recipe>) getIntent().getSerializableExtra("recipes");
        addRecipeDropBox(recipeList);
        Spinner selectRecipeSpinner = (Spinner)findViewById(R.id.selectRecipe);
        selectRecipeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempName = adapterView.getItemAtPosition(i).toString();
                LinearLayout LL = (LinearLayout)findViewById(R.id.recipeContentsView);
                LL.removeAllViews();
                if(tempName.equals("추가하기"))
                {
                    EditText addRecipeName = (EditText)findViewById(R.id.recipeName);
                    addRecipeName.setVisibility(View.VISIBLE);
                    resetTotalData();
                }
                else
                {
                    EditText addRecipeName = (EditText)findViewById(R.id.recipeName);
                    addRecipeName.setVisibility(View.INVISIBLE);
                    int recipeNumber = recipeIndexOf(recipes,tempName);
                    allViewAttach(LL,recipes.get(recipeNumber));
                    updateTotalData(recipeNumber);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button recipeAdd = (Button)findViewById(R.id.recipeAdd);//레시피 저장 버튼

        recipeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner selectRecipe = (Spinner)findViewById(R.id.selectRecipe);
                String selectRecipeName = selectRecipe.getSelectedItem().toString();
                if(selectRecipeName.equals("추가하기"))
                {
                    addRecipe();
                }
                fileOut(recipes);
                Intent intent = new Intent(getApplicationContext(),RecipeTableActivity.class);
                Toast.makeText(getApplicationContext(),"레시피 저장/수정이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button addCostToButton = (Button)findViewById(R.id.addCostToRecipe);//레시피에 재료 추가하는 버튼
        addCostToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner selectCost = (Spinner)findViewById(R.id.costSelect);
                EditText usage = (EditText)findViewById(R.id.usageToAdd);
                Spinner selectRecipe = (Spinner)findViewById(R.id.selectRecipe);
                try
                {
                    String costNameToAdd = selectCost.getSelectedItem().toString();
                    double usageToAdd = Double.parseDouble(usage.getText().toString());
                    int index = CTD.CTDNameOf(costNameToAdd);
                    String recipeName = selectRecipe.getSelectedItem().toString();
                    if(!recipeName.equals("추가하기"))
                    {
                        addMaterial(recipeName,costNameToAdd,usageToAdd,index);
                    }
                    else
                    {
                        EditText recipeNameToAddField = (EditText)findViewById(R.id.recipeName);
                        String nameToAdd = recipeNameToAddField.getText().toString();
                        if(nameToAdd.getBytes().length==0)
                        {
                            Toast.makeText(getApplicationContext(),"추가하기 항목에서 재료를 추가하실 수 없습니다.",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            addRecipe();
                            addMaterial(nameToAdd,costNameToAdd,usageToAdd,index);
                        }
                    }
                    usage.setText(null);
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(),"사용량은 숫자만 입력 가능합니다.",Toast.LENGTH_LONG).show();
                    usage.setText(null);
                }
                catch (NullPointerException e)
                {
                    Toast.makeText(getApplicationContext(),"지정 가능한 재료가 없습니다.",Toast.LENGTH_LONG).show();
                    usage.setText(null);
                }
            }
        });

        Button deleteCostToRecipe = (Button)findViewById(R.id.deleteCostToRecipe);

        deleteCostToRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner selectRecipe = (Spinner)findViewById(R.id.selectRecipe);
                deleteMaterial(selectRecipe.getSelectedItem().toString());
            }
        });

        Button updateCostToRecipe = (Button)findViewById(R.id.updateCostToRecipe);

        updateCostToRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner selectCost = (Spinner)findViewById(R.id.costSelect);
                int fixNumber = locationVerification();
                EditText usage = (EditText)findViewById(R.id.usageToAdd);
                Spinner selectRecipe = (Spinner)findViewById(R.id.selectRecipe);
                if(usage.getText().toString().length()!=0)
                {
                    try
                    {
                        double usageToFix = Double.parseDouble(usage.getText().toString());
                        modifyMaterial(fixNumber - 1, selectRecipe.getSelectedItem().toString(), selectCost.getSelectedItem().toString(), usageToFix);
                        Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
                        usage.setText(null);
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "사용량은 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    public void addCostDropBox(ArrayList<String> names)
    {
        ArrayList<String> addDrop = new ArrayList<>();
        addDrop.addAll(names);
        Spinner selectCost = (Spinner)findViewById(R.id.costSelect);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_dropdown_item,addDrop);
        selectCost.setAdapter(arrayAdapter);
    }

    public void addRecipeDropBox(ArrayList<String> recipeList)
    {
        ArrayList<String> addDrop = new ArrayList<>();
        Spinner selectRecipe = (Spinner)findViewById(R.id.selectRecipe);
        addDrop.addAll(recipeList);
        addDrop.add("추가하기");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,addDrop);
        selectRecipe.setAdapter(arrayAdapter);
    }

    public Sub addRecipeContentsView(int location, String materialName, double materialUsage, double usagePrice)
    {
        Sub addLayout = new Sub(getApplicationContext());
        addLayout.addView(addText(Integer.toString(location),1));
        addLayout.addView(addText(materialName,3));
        addLayout.addView(addText(Double.toString(materialUsage),2));
        addLayout.addView(addText(Double.toString(usagePrice),2));
        return addLayout;
    }

    public TextView addText(String str,float weight)
    {
        TextView text = new TextView(this);
        Drawable dra = getResources().getDrawable(R.drawable.border);
        text.setText(str);
        text.setTextSize(20);
        text.setBackground(dra);
        text.setHorizontallyScrolling(true);
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setMaxLines(1);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,weight));
        return text;
    }

    public void allViewAttach(LinearLayout sv, Recipe recipe)
    {
        for(int x=0;x<recipe.materialName.size();x++)
        {
            sv.addView(addRecipeContentsView(x+1,recipe.materialName.get(x),recipe.materialUsage.get(x),recipe.materialPrice.get(x)));
        }
    }

    public void updateTotalData(int index)
    {
        TextView totalMaterialQuantity = (TextView)findViewById(R.id.totalMaterialText);
        TextView totalMaterialUsage = (TextView)findViewById(R.id.totalUsageText);
        TextView totalMaterialPrice = (TextView)findViewById(R.id.totalPriceText);
        String quantity = "사용 재료 수 : "+recipes.get(index).getCostQuantity();
        String usage = "총 사용량 : "+recipes.get(index).getRecipeUsage();
        String price = "총 금액 : "+recipes.get(index).getRecipePrice();
        totalMaterialQuantity.setText(quantity);
        totalMaterialUsage.setText(usage);
        totalMaterialPrice.setText(price);
    }

    public int recipeIndexOf(ArrayList<Recipe> recipe, String name)
    {
        for(int i=0;i<recipe.size();i++)
        {
            if(name.equals(recipe.get(i).getRecipeName()))
            {
                return i;
            }
        }
        return -1;
    }

    public void resetTotalData()
    {
        TextView totalMaterialQuantity = (TextView)findViewById(R.id.totalMaterialText);
        TextView totalMaterialUsage = (TextView)findViewById(R.id.totalUsageText);
        TextView totalMaterialPrice = (TextView)findViewById(R.id.totalPriceText);
        String quantity = "사용 재료 수 : "+0;
        String usage = "총 사용량 : "+0;
        String price = "총 금액 : "+0;
        totalMaterialQuantity.setText(quantity);
        totalMaterialUsage.setText(usage);
        totalMaterialPrice.setText(price);
    }

    public void addRecipe()
    {
        EditText recipeNameToAddField = (EditText)findViewById(R.id.recipeName);
        String nameToAdd = recipeNameToAddField.getText().toString();
        if(nameToAdd.length()!=0)
        {
            Recipe addedRecipe = new Recipe(nameToAdd);
            recipes.add(addedRecipe);
            recipeList.add(nameToAdd);
            addRecipeDropBox(recipeList);
            addedRecipe.useToCheck=true;
            Toast.makeText(getApplicationContext(),nameToAdd+"가 추가되었습니다.",Toast.LENGTH_LONG).show();
            recipeNameToAddField.setText(null);
        }
    }

    public void addMaterial(String recipeName,String costNameToAdd,double usageToAdd, int index)
    {
        int recipeNumber = recipeIndexOf(recipes,recipeName);
        double pricePerGram = CTD.pricePerGram.get(index);
        Recipe tempRecipe = recipes.get(recipeNumber);
        int fixNumber = locationVerification();
        if(tempRecipe.addMaterial(costNameToAdd,usageToAdd,(double) Math.round(pricePerGram*usageToAdd*100)/100,fixNumber-1))
        {
            Toast.makeText(getApplicationContext(),"재료가 추가되었습니다.",Toast.LENGTH_SHORT).show();
            refresh(recipeNumber);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"위치를 확인해주세요.",Toast.LENGTH_SHORT).show();
        }

    }

    public void modifyMaterial(int index,String recipeName,String nameToFix, double usageToFix)
    {
        int recipeNumber = recipeIndexOf(recipes,recipeName);
        Recipe tempRecipe = recipes.get(recipeNumber);
        if(index>=0 && index<recipes.get(recipeNumber).materialName.size())
        {
            String costName= tempRecipe.materialName.get(index);
            double pricePerGram = CTD.pricePerGram.get(CTD.CTDNameOf(costName));
            tempRecipe.modifyMaterial(index,nameToFix,usageToFix,(double)Math.round((pricePerGram*usageToFix*100))/100);
            refresh(recipeNumber);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"수정할 정확한 위치를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteMaterial(String recipeName)
    {
        int recipeNumber = recipeIndexOf(recipes,recipeName);
        Recipe tempRecipe = recipes.get(recipeNumber);
        int fixNumber = locationVerification();
        String deletedName = tempRecipe.deleteMaterial(fixNumber-1);
        if(deletedName!=null)
        {
            Toast.makeText(getApplicationContext(),fixNumber+"번째의 위치의"+deletedName+" (을)를 제거하였습니다.",Toast.LENGTH_LONG).show();
            refresh(recipeNumber);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"제대로 된 위치가 맞는지 확인해주세요.",Toast.LENGTH_LONG).show();
        }
    }

    public void fileOut(ArrayList<Recipe> recipes)
    {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(root,"recipeFileDocument");

        if(!dir.exists())
        {
            if(dir.mkdirs())
            {
                Toast.makeText(getApplicationContext(),"폴더 생성에 성공하였습니다.",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"폴더 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        for(int i=0;i<recipes.size();i++)//레시피 개수만큼 반복
        {
            Recipe tempRecipe = recipes.get(i);
            if(tempRecipe.useToCheck)//레시피의 변동이 존재한다면
            {
                String fileName = tempRecipe.getRecipeName();
                File saveFile = new File(root+"/recipeFileDocument/"+fileName+".txt");
                try {
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
                    for(int x= 0;x<tempRecipe.materialName.size();x++)//레시피 속 재료수만큼 반복
                    {
                        String materialName = tempRecipe.materialName.get(x);
                        double materialUsage = tempRecipe.materialUsage.get(x);
                        double materialPrice = tempRecipe.materialPrice.get(x);
                        pw.println(materialName+"\t"+materialUsage+"\t"+materialPrice);
                    }
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"파일 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                }
            }
            tempRecipe.useToCheck=false;
        }
    }

    public int locationVerification()
    {
        EditText addLocation = (EditText)findViewById(R.id.fixNumber);
        int fixNumber = -1;
        if(addLocation.getText().toString().length()!=0)
        {
            try
            {
                fixNumber = Integer.parseInt(addLocation.getText().toString());
            }
            catch (NumberFormatException e)
            {
                Toast.makeText(getApplicationContext(),"위치 지정은 숫자만 가능합니다.",Toast.LENGTH_SHORT).show();
            }
        }
        addLocation.setText(null);
        return fixNumber;
    }

    public void refresh(int recipeNumber)
    {
        updateTotalData(recipeNumber);
        LinearLayout sv = (LinearLayout)findViewById(R.id.recipeContentsView);
        sv.removeAllViews();
        allViewAttach(sv,recipes.get(recipeNumber));
    }


}
