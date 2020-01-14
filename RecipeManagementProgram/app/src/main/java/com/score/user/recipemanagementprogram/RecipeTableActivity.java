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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class RecipeTableActivity extends AppCompatActivity
{
    private DbOpenHelper mDbOpenHelper;
    ArrayList<Recipe> recipes = new ArrayList<>();
    ArrayList<String> recipeList = new ArrayList<>();
    CostTableData CTD = new CostTableData();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();
        CTD.addDB(mDbOpenHelper);
        authrization();
        recipeLoad(recipes);
        recipeList = allName();
        addRecipeDropBox(recipeList);
        Button backToMain = (Button)findViewById(R.id.toMainButton);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        final Button addOrEditRecipe = (Button)findViewById(R.id.insertButton);

        addOrEditRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecipeChangeActivity.class);
                intent.putExtra("recipeNameList",allName());
                intent.putExtra("recipes",recipes);
                startActivity(intent);
            }
        });

        final Spinner recipeListS = (Spinner)findViewById(R.id.recipeSpinner);

        recipeListS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LinearLayout LL = (LinearLayout)findViewById(R.id.recipeAllView);
                LL.removeAllViews();
                Recipe tempRecipe = recipes.get(i);
                allViewAttach(LL,tempRecipe);
                updateTotalData(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Button removeButton = (Button)findViewById(R.id.deleteButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner recipeSpinner = (Spinner)findViewById(R.id.recipeSpinner);
                try {
                    String removeRecipeName = recipeSpinner.getSelectedItem().toString();
                    int indexToRemove = recipeList.indexOf(removeRecipeName);
                    if (fileDelete(recipes.get(indexToRemove))) {
                        recipes.remove(indexToRemove);
                        recipeList.remove(indexToRemove);
                        addRecipeDropBox(recipeList);
                        Toast.makeText(getApplicationContext(), removeRecipeName + "(이)가 제거되었습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "파일 제거에 실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                }
                catch (NullPointerException e)
                {
                    Toast.makeText(getApplicationContext(), "레시피가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                }
                LinearLayout LL = (LinearLayout)findViewById(R.id.recipeAllView);
                LL.removeAllViews();
                resetTotalData();
            }
        });
    }

    public ArrayList<String> allName()
    {
        ArrayList<String> tempList = new ArrayList<>();
        for(int i=0;i<recipes.size();i++)
        {
            tempList.add(recipes.get(i).getRecipeName());
        }
        return tempList;
    }

    public void addRecipeDropBox(ArrayList<String> recipeList)
    {
        ArrayList<String> addDrop = new ArrayList<>();
        Spinner selectRecipe = (Spinner)findViewById(R.id.recipeSpinner);
        addDrop.addAll(recipeList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,addDrop);
        selectRecipe.setAdapter(arrayAdapter);
    }

    public Sub addRecipeContentsView(String materialName, double materialUsage, double usagePrice)
    {
        Sub addLayout = new Sub(getApplicationContext());
        addLayout.addView(addText(materialName,3));
        addLayout.addView(addText(Double.toString(materialUsage),2));
        addLayout.addView(addText(Double.toString(usagePrice),2));
        return addLayout;
    }

    public TextView addText(String str, float weight)
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
        String modifiedMaterialName;
        String usesToCheck ="false";
        int size = recipe.materialName.size();
        for(int i=0;i<size;i++)
        {
            modifiedMaterialName = recipe.materialName.get(i);
            int materialIndex = CTD.costName.indexOf(modifiedMaterialName);
            if (materialIndex == -1)
            {
                int deleteIndex = recipe.materialName.indexOf(modifiedMaterialName);
                recipe.materialName.remove(deleteIndex);
                recipe.materialUsage.remove(deleteIndex);
                recipe.materialPrice.remove(deleteIndex);
                usesToCheck="true";
                recipe.rePriceSet();
                i--;
                size--;
            }
            else
            {
                String usingToCheck = CTD.usesCheck.get(materialIndex);
                if(usingToCheck.equals("true"))
                {
                    recipe.materialPrice.set(i, (double) Math.round(CTD.pricePerGram.get(materialIndex) * recipe.materialUsage.get(i) * 100) / 100);
                    usesToCheck="true";
                }
                else
                {
                    continue;
                }
            }
        }
        for(int x=0;x<recipe.materialName.size();x++) {
            if (usesToCheck.equals("true"))
            {
                File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recipeFileDocument/" + recipe.getRecipeName() + ".txt");
                try
                {
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
                    for (int y = 0; y < recipe.materialName.size(); y++)//레시피 속 재료수만큼 반복
                    {
                        String materialName = recipe.materialName.get(y);
                        double materialUsage = recipe.materialUsage.get(y);
                        double materialPrice = recipe.materialPrice.get(y);
                        pw.println(materialName + "\t" + materialUsage + "\t" + materialPrice);
                    }
                    pw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
            sv.addView(addRecipeContentsView(recipe.materialName.get(x),recipe.materialUsage.get(x),recipe.materialPrice.get(x)));
        }
    }

    public void resetTotalData()
    {
        TextView totalMaterialQuantity = (TextView)findViewById(R.id.totalMaterialInOne);
        TextView totalMaterialUsage = (TextView)findViewById(R.id.totalUsageInOne);
        TextView totalMaterialPrice = (TextView)findViewById(R.id.totalPriceInOne);
        totalMaterialQuantity.setText("사용 재료 수 : "+0);
        totalMaterialUsage.setText("총 사용량 : "+0);
        totalMaterialPrice.setText("총 금액 : "+0);
    }

    public void updateTotalData(int index)
    {
        TextView totalMaterialQuantity = (TextView)findViewById(R.id.totalMaterialInOne);
        TextView totalMaterialUsage = (TextView)findViewById(R.id.totalUsageInOne);
        TextView totalMaterialPrice = (TextView)findViewById(R.id.totalPriceInOne);
        totalMaterialQuantity.setText("사용 재료 수 : "+recipes.get(index).getCostQuantity());
        totalMaterialUsage.setText("총 사용량 : "+recipes.get(index).getRecipeUsage());
        totalMaterialPrice.setText("총 금액 : "+recipes.get(index).getRecipePrice());
    }

    public void authrization()
    {
        int readPermisstionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermisstionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermisstionCheck == PackageManager.PERMISSION_GRANTED && writePermisstionCheck==PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0) {
                    for (int i=0;i<grantResults.length;i++)
                    {
                        if(grantResults[i]==PackageManager.PERMISSION_DENIED)
                        {
                            Toast.makeText(this, "파일 읽기,쓰기 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    recipeLoad(recipes);
                }
                break;
            }
        }
    }

    public void recipeLoad(ArrayList<Recipe> recipes)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/recipeFileDocument";
        File loadFile = new File(path);
        File[] files = loadFile.listFiles();
        if(files!=null)
        {
            for(int i=0;i<files.length;i++) {
                try {
                    File file = files[i];
                    String line;
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String recipeName = file.getName().substring(0,file.getName().lastIndexOf(".txt"));
                    Recipe addRecipe = new Recipe(recipeName);
                    while ((line = br.readLine()) != null) {
                        try {
                            StringTokenizer st = new StringTokenizer(line, "\t");
                            String name = st.nextToken();
                            double usage = Double.parseDouble(st.nextToken());
                            double price = Double.parseDouble(st.nextToken());
                            addRecipe.addMaterial(name, usage, price,-1);
                            addRecipe.useToCheck = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    recipes.add(addRecipe);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "파일을 불러오는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"현재 저장된 레시피가 없습니다.",Toast.LENGTH_SHORT).show();
        }
        Collections.sort(recipes);
    }

    public boolean fileDelete(Recipe deleteRecipe)
    {
        File deleteFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/recipeFileDocument/"+deleteRecipe.getRecipeName()+".txt");
        if(deleteFile.delete())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
