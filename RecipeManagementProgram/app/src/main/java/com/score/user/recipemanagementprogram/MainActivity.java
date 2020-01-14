package com.score.user.recipemanagementprogram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    DbOpenHelper mDbOpenHelper;
    CostTableData CTD = new CostTableData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbOpenHelper = new DbOpenHelper(this);
        setContentView(R.layout.activity_main);

        Button costB = (Button)findViewById(R.id.costButton);
        Button recipeB = (Button)findViewById(R.id.recipeButton);
        Button costOutput = (Button)findViewById(R.id.costPrintButton);
        Button recipeOutput = (Button)findViewById(R.id.recipePrintButton);
        Button recipeBackup = (Button)findViewById(R.id.recipeUpdateButton);

        costB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(),CostTableActivity.class);
                startActivity(intent);
            }
        });

        recipeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getApplicationContext(),RecipeTableActivity.class);
                startActivity(intent);
            }
        });

        costOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),FileExplorer.class);
                intent.putExtra("Mode",0);
                startActivity(intent);
            }
        });

        recipeOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),FileExplorer.class);
                intent.putExtra("Mode",1);
                startActivity(intent);
            }
        });

        recipeBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbOpenHelper.open();
                mDbOpenHelper.create();
                CTD.addDB(mDbOpenHelper);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/recipeFileDocument";
                File loadFile = new File(path);
                File[] files = loadFile.listFiles();
                if(files!=null)
                {
                    for(int i=0;i<files.length;i++)
                    {
                        try
                        {
                            File file = files[i];
                            String line;
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String recipeName = file.getName().substring(0,file.getName().lastIndexOf(".txt"));
                            Recipe addRecipe = new Recipe(recipeName);
                            while ((line = br.readLine()) != null)
                            {
                                StringTokenizer st = new StringTokenizer(line, "\t");
                                String name = st.nextToken();
                                double usage = Double.parseDouble(st.nextToken());
                                int costIndex = CTD.costName.indexOf(name);
                                if(costIndex!=-1)
                                {
                                    double price = (double) Math.round(usage*CTD.pricePerGram.get(costIndex)*100)/100;
                                    addRecipe.addMaterial(name,usage,price,-1);
                                }
                            }
                            file.delete();
                            File saveFile = new File(path+"/"+recipeName+".txt");
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));
                            for(int x= 0;x<addRecipe.materialName.size();x++)//레시피 속 재료수만큼 반복
                            {
                                String materialName = addRecipe.materialName.get(x);
                                double materialUsage = addRecipe.materialUsage.get(x);
                                double materialPrice = addRecipe.materialPrice.get(x);
                                pw.println(materialName+"\t"+materialUsage+"\t"+materialPrice);
                            }
                            pw.close();
                        }
                        catch (IOException e)
                        {
                            Toast.makeText(getApplicationContext(),"업데이트에 실패했습니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Toast.makeText(getApplicationContext(),"업데이트에 성공했습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
