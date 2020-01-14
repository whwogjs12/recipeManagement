package com.score.user.recipemanagementprogram;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class CostTableActivity extends AppCompatActivity implements TextView.OnEditorActionListener
{
    CostTableData CTD = new CostTableData();
    private DbOpenHelper mDbOpenHelper;
    int emphasizeIndex = -1;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();
        CTD.addDB(mDbOpenHelper);
        addDropBox(CTD.costName);

        Button backToMain = (Button)findViewById(R.id.costToMainButton);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button addCostButton = (Button)findViewById(R.id.addButton);

        addCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button tempButton = (Button)view;
                String buttonString = tempButton.getText().toString();
                if(buttonString.equals("추가"))
                {
                    addCost();
                }
                else
                {
                    adjustCost();
                }
            }
        });

        Button deleteButton = (Button)findViewById(R.id.deleteCostButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner delDropBox = (Spinner)findViewById(R.id.spinner);
                int index = CTD.costName.indexOf(delDropBox.getSelectedItem().toString());
                String deleteName = CTD.costName.get(index);
                mDbOpenHelper.deleteColumn(deleteName);
                CTD.delete(index);
                addDropBox(CTD.costName);
                Toast.makeText(getApplicationContext(),deleteName+"의 값이 제거되었습니다.",Toast.LENGTH_LONG).show();
                delDropBox.setSelection(0);
            }
        });

        final Spinner costDropBox = (Spinner)findViewById(R.id.spinner);
        costDropBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempName = adapterView.getItemAtPosition(i).toString();
                LinearLayout sv = (LinearLayout)findViewById(R.id.scrollLayout);
                sv.removeAllViews();
                EditText et1 = (EditText)findViewById(R.id.costEdit);
                EditText et2 = (EditText)findViewById(R.id.weightEdit);
                if(tempName.equals("전체보기"))
                {
                    EditText visibleText = (EditText)findViewById(R.id.nameEdit);
                    visibleText.setVisibility(View.VISIBLE);
                    Button deleteCostButton = (Button)findViewById(R.id.deleteCostButton);
                    deleteCostButton.setVisibility(View.GONE);
                    Button addCostButton = (Button)findViewById(R.id.addButton);
                    addCostButton.setText("추가");
                    et1.setHint("추가할 단가");
                    et2.setHint("추가할 양");
                    allViewAttach(sv,emphasizeIndex);
                    emphasizeIndex=-1;
                }
                else
                {
                    Button addCostButton = (Button)findViewById(R.id.addButton);
                    Button deleteCostButton = (Button)findViewById(R.id.deleteCostButton);
                    int index = CTD.costName.indexOf(tempName);
                    sv.addView(addCostFull(CTD.costName.get(index),CTD.costPrice.get(index),CTD.costWeight.get(index),CTD.pricePerGram.get(index)));
                    deleteCostButton.setVisibility(View.VISIBLE);
                    addCostButton.setText("수정");
                    EditText invisibleText = (EditText)findViewById(R.id.nameEdit);
                    invisibleText.setVisibility(View.GONE);
                    et1.setHint("수정할 단가");
                    et2.setHint("수정할 양");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        EditText costEdit = (EditText)findViewById(R.id.costEdit);
        costEdit.setOnEditorActionListener(this);
        EditText weightEdit = (EditText)findViewById(R.id.weightEdit);
        weightEdit.setOnEditorActionListener(this);
    }

    public void addCost()
    {
        EditText nameEdit = (EditText)findViewById(R.id.nameEdit);
        EditText costEdit = (EditText)findViewById(R.id.costEdit);
        EditText weightEdit = (EditText)findViewById(R.id.weightEdit);

        try {
            if(nameEdit.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"재료명을 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            else if(costEdit.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"단가를 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            else if(weightEdit.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"사용량을 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            else
            {
                String name = nameEdit.getText().toString();
                double cost = Double.parseDouble(costEdit.getText().toString());
                double weight = Double.parseDouble(weightEdit.getText().toString());
                if(CTD.add(name,cost,weight,"false"))
                {
                    LinearLayout sv = (LinearLayout)findViewById(R.id.scrollLayout);
                    mDbOpenHelper.insertColumn(name,cost,weight,Math.round((cost/weight)*100)/100.0);
                    sv.addView(addCostFull(name,cost,weight,Math.round((cost/weight)*100)/100.0));
                    addDropBox(CTD.costName);
                    emphasizeIndex = CTD.costName.indexOf(name);
                    Toast.makeText(getApplicationContext(),"재료가 추가되었습니다.",Toast.LENGTH_LONG).show();
                    nameEdit.setText(null);
                    costEdit.setText(null);
                    weightEdit.setText(null);
                    nameEdit.requestFocus();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"이미 사용중인 재료명입니다.",Toast.LENGTH_LONG).show();
                    nameEdit.setText(null);
                }
            }
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(getApplicationContext(),"단가와 중량은 숫자만 입력 가능합니다.",Toast.LENGTH_LONG).show();
            costEdit.setText(null);
            weightEdit.setText(null);
        }
    }

    public TextView addText(String str,float weight)
    {
        TextView text = new TextView(this);
        Drawable dra = getResources().getDrawable(R.drawable.border);
        text.setText(str);
        text.setTextSize(18);
        text.setBackground(dra);
        text.setHorizontallyScrolling(true);
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setMaxLines(1);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,weight));
        return text;
    }

    public void addDropBox(ArrayList<String> names)
    {
        ArrayList<String> addDrop = new ArrayList<>();
        addDrop.add(0,"전체보기");
        addDrop.addAll(names);
        Spinner sp = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_dropdown_item,addDrop);
        sp.setAdapter(arrayAdapter);
    }

    public Sub addCostFull(String name, double cost, double weight,double pricePerGram)
    {
        Sub addLayout = new Sub(getApplicationContext());
        addLayout.addView(addText(name,2));
        addLayout.addView(addText(Integer.toString((int)Math.round(cost)),1));
        addLayout.addView(addText(Integer.toString((int)Math.round(weight)),1));
        addLayout.addView(addText(Double.toString(pricePerGram),1));
        return addLayout;
    }

    public Sub EmphasizeNewAreas(String name, double cost, double weight,double pricePerGram)
    {
        Sub addLayout = new Sub(getApplicationContext());
        addLayout.addView(addEmphasizeText(name,2));
        addLayout.addView(addEmphasizeText(Integer.toString((int)Math.round(cost)),1));
        addLayout.addView(addEmphasizeText(Integer.toString((int)Math.round(weight)),1));
        addLayout.addView(addEmphasizeText(Double.toString(pricePerGram),1));
        return addLayout;
    }

    public TextView addEmphasizeText(String str, float weight)
    {
        TextView text = new TextView(this);
        Drawable dra = getResources().getDrawable(R.drawable.border);
        text.setText(str);
        text.setTextSize(18);
        text.setBackground(dra);
        text.setHorizontallyScrolling(true);
        text.setTextColor(getResources().getColor(R.color.orange));
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setMaxLines(1);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,weight));
        return text;
    }

    public void allViewAttach(LinearLayout sv, int index)
    {
        for(int x=0;x<CTD.costName.size();x++)
        {
            if(index!=x)
            {
                sv.addView(addCostFull(CTD.costName.get(x),CTD.costPrice.get(x),CTD.costWeight.get(x),CTD.pricePerGram.get(x)));
            }
            else
            {
                sv.addView(EmphasizeNewAreas(CTD.costName.get(x),CTD.costPrice.get(x),CTD.costWeight.get(x),CTD.pricePerGram.get(x)));
            }
        }
    }

    public void adjustCost()
    {
        EditText et1 = (EditText)findViewById(R.id.costEdit);
        EditText et2 = (EditText)findViewById(R.id.weightEdit);
        Spinner adjDropBox = (Spinner)findViewById(R.id.spinner);
        String text_1 = et1.getText().toString();
        String text_2 = et2.getText().toString();
        try
        {
            int index = CTD.costName.indexOf(adjDropBox.getSelectedItem().toString());
            if(et1.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"단가를 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            else if(et2.getText().toString().length()==0)
            {
                Toast.makeText(getApplicationContext(),"사용량을 입력해주세요.",Toast.LENGTH_LONG).show();
            }
            else {
                double priceEdit = Double.parseDouble(text_1);
                double weightEdit= Double.parseDouble(text_2);
                CTD.adjust(index, priceEdit, weightEdit);
                et1.setText(null);
                et2.setText(null);
                Toast.makeText(getApplicationContext(), adjDropBox.getSelectedItem().toString() + "의 값이 수정되었습니다.", Toast.LENGTH_LONG).show();
                mDbOpenHelper.updateColumn(adjDropBox.getSelectedItem().toString(),priceEdit,weightEdit,(Math.round(priceEdit/weightEdit*100))/100.0);
                adjDropBox.setSelection(0);
            }
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(getApplicationContext(),"단가와 사용량은 숫자만 입력 가능합니다.",Toast.LENGTH_LONG).show();
            et1.setText(null);
            et2.setText(null);
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId== EditorInfo.IME_ACTION_DONE && v.getId() == R.id.weightEdit)//중량 입력 창일때 완료버튼
        {
            Button tempButton = (Button)findViewById(R.id.addButton);
            String buttonString = tempButton.getText().toString();
            if(buttonString.equals("추가"))
            {
                addCost();
            }
            else
            {
                adjustCost();
            }
            return true;
        }
        else if(actionId== EditorInfo.IME_ACTION_DONE && v.getId() == R.id.costEdit)//단가 입력 창일때 완료버튼
        {
            EditText weightEdit = (EditText)findViewById(R.id.weightEdit);
            weightEdit.requestFocus();
            return true;
        }
        return false;
    }
}
