package com.score.user.recipemanagementprogram;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity; // import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FileExplorer extends AppCompatActivity { // public class MainActivity extends Activity {
    private String mFileName;
    private ListView lvFileControl;
    private Context mContext = this;
    private List<String> lItem = null;
    private List<String> lPath = null;
    private String mRoot =null;
    private TextView mPath;
    private String directoryName;
    private String version;
    private DbOpenHelper mDbOpenHelper;
    int value;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDbOpenHelper = new DbOpenHelper(this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();
        authrization();
        Intent saveIntent = getIntent();
        int mode = saveIntent.getIntExtra("Mode",2);
        if(mode ==0){version="원가표";}
        else if(mode ==1){version="레시피";}
        else{version="에러";}

        mPath = (TextView) findViewById(R.id.tvPath);
        lvFileControl = (ListView) findViewById(R.id.lvFileControl);
        mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        getDir(mRoot);

        Button saveClick = (Button)findViewById(R.id.saveClick);
        saveClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameToSave = (EditText)findViewById(R.id.saveName);
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                String getName = nameToSave.getText().toString();
                Workbook xlsx = new XSSFWorkbook();
                if(version.equals("원가표"))
                {
                    createCostFile(xlsx,getName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else if(version.equals("레시피"))
                {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File dir = new File(root,"recipeFileDocument");
                    File[] files = dir.listFiles();
                    new ProgressDlgTest(FileExplorer.this).execute(files.length);
                    //createRecipeFile(xlsx,getName);
                }
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
            }
        });

        lvFileControl.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                File file = new File(lPath.get(position));
                EditText nameToSave = (EditText)findViewById(R.id.saveName);
                if (file.isDirectory()) {
                    if (file.canRead())
                    {
                        nameToSave.setText(null);
                        directoryName = lPath.get(position);
                        getDir(lPath.get(position));
                    }
                    else {
                        Toast.makeText(mContext, "폴더에 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mFileName = file.getName();
                    nameToSave.setText(mFileName.substring(0,mFileName.lastIndexOf('.')));
                }
            }
        });

    }

    private void getDir(String dirPath) {
        mPath.setText("Location: " + dirPath);
        lItem = new ArrayList<String>();
        lPath = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if (!dirPath.equals(mRoot)) {
            //item.add(root); //to root.
            //path.add(root);
            lItem.add("../"); //to parent folder
            lPath.add(f.getParent());

        }
        try {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                lPath.add(file.getAbsolutePath());
                if (file.isDirectory())
                    lItem.add(file.getName() + "/");
                else
                    lItem.add(file.getName());
            }
            ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItem);
            lvFileControl.setAdapter(fileList);
        }
        catch (Exception e)
        {

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
                    getDir(mRoot);
                }
                break;
            }
        }
    }

    public boolean isText(String extension)
    {
        if (extension.equals("txt")) { return true; }
        return false;
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

    public void createRecipeFile(Workbook xlsx,String fileName)
    {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(root,"recipeFileDocument");
        if(!dir.exists())
        {
            Toast.makeText(getApplicationContext(),"레시피가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {

            File[] files = dir.listFiles();
            if(files!=null)
            {
                for (int i = 0; i < files.length; i++)
                {
                    File recipeFile = files[i];
                    String line;
                    BufferedReader br = null;
                    double totalUsage =0;
                    double totalPrice = 0;
                    try
                    {
                        br = new BufferedReader(new FileReader(recipeFile));
                        Sheet sheet = xlsx.createSheet(recipeFile.getName().substring(0,recipeFile.getName().lastIndexOf(".txt")));
                        Row row = sheet.createRow(0);
                        Cell cell;
                        cell = row.createCell(0);
                        cell.setCellValue("재료명");
                        cell = row.createCell(1);
                        cell.setCellValue("사용량");
                        cell = row.createCell(2);
                        cell.setCellValue("가격");
                        int costIndex=0;
                        while ((line = br.readLine()) != null)
                        {
                            costIndex++;
                            StringTokenizer st = new StringTokenizer(line, "\t");
                            row = sheet.createRow(costIndex);
                            String name = st.nextToken();
                            double usage = Double.parseDouble(st.nextToken());
                            double price = Double.parseDouble(st.nextToken());
                            totalUsage+=usage;
                            totalPrice+=price;
                            cell = row.createCell(0);
                            cell.setCellValue(name);
                            cell = row.createCell(1);
                            cell.setCellValue(usage);
                            cell = row.createCell(2);
                            cell.setCellValue(price);
                        }
                        row = sheet.createRow(costIndex+2);
                        cell = row.createCell(0);
                        cell.setCellValue("총 사용 재료 수 : "+costIndex);
                        cell = row.createCell(1);
                        cell.setCellValue("총 사용량 : "+totalUsage);
                        cell = row.createCell(2);
                        cell.setCellValue("총 가격 :" + totalPrice);
                    }
                    catch (IOException e)
                    {
                        Toast.makeText(getApplicationContext(),"파일을 불러오는데 문제가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
                File excel = new File(directoryName,fileName+".xlsx");
                try{
                    FileOutputStream os = new FileOutputStream(excel);
                    xlsx.write(os);

                    Toast.makeText(getApplicationContext(),"파일이 저장되었습니다.",Toast.LENGTH_SHORT).show();
                }catch (IOException e){
                    Toast.makeText(getApplicationContext(),"파일 저장에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(),"불러올 레시피가 없습니다.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void createCostFile(Workbook xlsx, String fileName)
    {
        Sheet newSheet = xlsx.createSheet(version);
        Row row = newSheet.createRow(0);
        int i=0;
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue("재료명");
        cell = row.createCell(1);
        cell.setCellValue("단가");
        cell = row.createCell(2);
        cell.setCellValue("중량");
        cell = row.createCell(3);
        cell.setCellValue("1g랑 가격");
        Cursor c = mDbOpenHelper.selectColumns();
        while(c.moveToNext())
        {
            row = newSheet.createRow(i+1);
            cell = row.createCell(0);
            cell.setCellValue(c.getString(0));
            cell = row.createCell(1);
            cell.setCellValue(c.getDouble(1));
            cell = row.createCell(2);
            cell.setCellValue(c.getDouble(2));
            cell = row.createCell(3);
            cell.setCellValue(c.getDouble(3));
            i++;
        }
        File excel = new File(directoryName,fileName+".xlsx");
        try{
            FileOutputStream os = new FileOutputStream(excel);
            xlsx.write(os);
            Toast.makeText(getApplicationContext(),"파일이 저장되었습니다.",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"파일 저장에 실패하였습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    public class ProgressDlgTest extends AsyncTask< Integer//excute()실행시 넘겨줄 데이터타입
            , String//진행정보 데이터 타입 publishProgress(), onProgressUpdate()의 인수
            , Integer//doInBackground() 종료시 리턴될 데이터 타입 onPostExecute()의 인수
            > {
        //ProgressDialog를 멤버로 하나 넣어줌
        private ProgressDialog mDlg;
        private Context mContext;

        public ProgressDlgTest(Context context) {
            mContext = context;
        }

        //onPreExecute 함수는 이름대로 excute()로 실행 시 doInBackground() 실행 전에 호출되는 함수
        //여기서 ProgressDialog 생성 및 기본 세팅하고 show()
        @Override
        protected void onPreExecute() {
            mDlg = new ProgressDialog(mContext);
            mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDlg.setMessage("작업 시작");
            mDlg.show();

            super.onPreExecute();
        }

        //doInBackground 함수는 excute() 실행시  실행됨
        //여기서 인수로는 작업개수를 넘겨주었다.
        @Override
        protected Integer doInBackground(Integer... params) {

            final int taskCnt = params[0];
            //넘겨받은 작업개수를 ProgressDialog의 맥스값으로 세팅하기 위해 publishProgress()로 데이터를 넘겨준다.
            //publishProgress()로 넘기면 onProgressUpdate()함수가 실행된다.
            publishProgress("max", Integer.toString(taskCnt));
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(root,"recipeFileDocument");
            if(!dir.exists())
            {
                Toast.makeText(getApplicationContext(),"레시피가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                EditText nameToSave = (EditText)findViewById(R.id.saveName);
                String getName = nameToSave.getText().toString();
                Workbook xlsx = new XSSFWorkbook();
                File[] files = dir.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        publishProgress("progress", Integer.toString(i), "작업 번호 " + Integer.toString(i) + "번 수행중");
                        File recipeFile = files[i];
                        String line;
                        BufferedReader br = null;
                        double totalUsage = 0;
                        double totalPrice = 0;
                        try {
                            br = new BufferedReader(new FileReader(recipeFile));
                            Sheet sheet = xlsx.createSheet(recipeFile.getName().substring(0, recipeFile.getName().lastIndexOf(".txt")));
                            Row row = sheet.createRow(0);
                            Cell cell;
                            cell = row.createCell(0);
                            cell.setCellValue("재료명");
                            cell = row.createCell(1);
                            cell.setCellValue("사용량");
                            cell = row.createCell(2);
                            cell.setCellValue("가격");
                            int costIndex = 0;
                            while ((line = br.readLine()) != null) {
                                costIndex++;
                                StringTokenizer st = new StringTokenizer(line, "\t");
                                row = sheet.createRow(costIndex);
                                String name = st.nextToken();
                                double usage = Double.parseDouble(st.nextToken());
                                double price = Double.parseDouble(st.nextToken());
                                totalUsage += usage;
                                totalPrice += price;
                                cell = row.createCell(0);
                                cell.setCellValue(name);
                                cell = row.createCell(1);
                                cell.setCellValue(usage);
                                cell = row.createCell(2);
                                cell.setCellValue(price);
                            }
                            row = sheet.createRow(costIndex + 2);
                            cell = row.createCell(0);
                            cell.setCellValue("총 사용 재료 수 : " + costIndex);
                            cell = row.createCell(1);
                            cell.setCellValue("총 사용량 : " + totalUsage);
                            cell = row.createCell(2);
                            cell.setCellValue("총 가격 :" + totalPrice);
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "파일을 불러오는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                        }

                    }
                    File excel = new File(directoryName, getName + ".xlsx");
                    try {
                        FileOutputStream os = new FileOutputStream(excel);
                        xlsx.write(os);
                        //Toast.makeText(getApplicationContext(), "파일이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "파일 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "불러올 레시피가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            //작업 진행, 여기선 넘겨준 작업개수 * 100 만큼 sleep() 걸어줌
            //작업이 끝나고 작업된 개수를 리턴 . onPostExecute()함수의 인수가 됨
            return taskCnt;
        }

        //onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                mDlg.setProgress(Integer.parseInt(progress[1]));
                mDlg.setMessage(progress[2]);
            }
            else if (progress[0].equals("max")) {
                mDlg.setMax(Integer.parseInt(progress[1]));
            }
        }
        //onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
        @Override
        protected void onPostExecute(Integer result) {
            mDlg.dismiss();
            Toast.makeText(mContext,  "파일이 저장되었습니다", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

}
