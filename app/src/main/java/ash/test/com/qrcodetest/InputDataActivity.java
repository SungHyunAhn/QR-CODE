package ash.test.com.qrcodetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Created by 성현 on 2016-05-05.
 */
public class InputDataActivity extends Activity{

    Button QRdataRead;
    Button makeQRbtn;
    Button delData;
    Button changeData;
    EditText editName;
    //EditText editFreshness;
    Spinner spinnerYear;
    Spinner spinnerMonth;
    Spinner spinnerDay;
    EditText editStock;
    String position;
    Intent getIntent;
    int[] arr = new int[3];
    String data;
    String fileName;

    Date day;
    public static Date getDate(int year, int month, int date, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, date, hour, minute, second);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        QRdataRead = (Button)findViewById(R.id.QRdataRead);
        makeQRbtn = (Button)findViewById(R.id.makeQRbtn);

        delData = (Button)findViewById(R.id.delData);
        changeData = (Button)findViewById(R.id.changeData);

        delData.setText("식재료 삭제");
        changeData.setText("식재료 등록/변경");

        editName = (EditText)findViewById(R.id.editName);

        spinnerYear = (Spinner)findViewById(R.id.spinnerYear);
        ArrayAdapter adsy = ArrayAdapter.createFromResource(this, R.array.year, R.layout.spinnerlayout);
        adsy.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerYear.setAdapter(adsy);

        spinnerMonth = (Spinner)findViewById(R.id.spinnerMonth);
        ArrayAdapter adsm = ArrayAdapter.createFromResource(this, R.array.month, R.layout.spinnerlayout);
        adsm.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerMonth.setAdapter(adsm);

        spinnerDay = (Spinner)findViewById(R.id.spinnerDay);
        ArrayAdapter adsd = ArrayAdapter.createFromResource(this, R.array.day, R.layout.spinnerlayout);
        adsd.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerDay.setAdapter(adsd);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                arr[0] = Integer.parseInt(spinnerYear.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                arr[1] = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                arr[2] = Integer.parseInt(spinnerDay.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editStock = (EditText)findViewById(R.id.editStock);

        getIntent = getIntent();

        setData(getIntent);

        QRdataRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(InputDataActivity.this).initiateScan();
            }
        });


        makeQRbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    day = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                    fileName = String.valueOf(sdf.format(day));

                    AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                    builder.setTitle("식료품 삭제");
                    builder.setMessage("정말로 식료품을 목록에서 삭제하시겠습니까?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("식료품명", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(editName.length() == 0){
                                Toast.makeText(InputDataActivity.this, "식료품명을 입력해주세요.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                fileName = fileName + " 식료품명 : " + editName.getText().toString();
                                data = editName.getText().toString();
                                makeQRImage("식료품명");
                            }
                        }
                    });

                    builder.setNeutralButton("재고등록", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            if(editName.length() == 0 || editStock.length() == 0){
                                Toast.makeText(InputDataActivity.this, "재고의 정보를 전부 입력해주세요.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                fileName = fileName + " 식료품명 : " + editName.getText().toString() + " 재고수량 : " + editStock.getText().toString();
                                data = editName.getText().toString() + "!@#@!" + editStock.getText().toString();
                                makeQRImage("재고등록");
                            }
                        }
                    });

                    builder.setNegativeButton("재고수량", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(editStock.length() == 0){
                                Toast.makeText(InputDataActivity.this, "재고수량을 입력해주세요.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                fileName = fileName + " 재고수량 : " + editStock.getText().toString();
                                data = editStock.getText().toString();
                                makeQRImage("재고수량");
                            }
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.d("e", "error");
                }
            }
        });


        changeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                intent.putExtra("name", editName.getText().toString());
                intent.putExtra("year", String.valueOf(arr[0]));
                intent.putExtra("month", String.valueOf(arr[1]));
                intent.putExtra("day", String.valueOf(arr[2]));
                intent.putExtra("stock", editStock.getText().toString());

                setResult(RESULT_OK, intent);

                finish();
            }
        });

        delData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (getIntent.getStringExtra("new").equals("new"))
                        Toast.makeText(InputDataActivity.this, "새 항목 만들기여서 삭제할 항목이 선택되지 않았습니다.", Toast.LENGTH_LONG).show();
                }
                catch(NullPointerException e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                    builder.setTitle("식료품 삭제")
                            .setMessage("정말로 해당 항목을 목록에서 삭제하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent();
                                    intent.putExtra("position", position);
                                    setResult(2, intent);
                                    finish();
                                }
                            })

                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {
                Log.d("InputDataActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
            else {
                Log.d("InputDataActivity", "Scanned");
                String r = result.getContents();

                if(isNumber(r))
                    editStock.setText(r);
                else {
                    String[] getDataArray;
                    String name;
                    String stock;
                    try {
                        getDataArray = r.trim().split("!@#@!");
                        name = getDataArray[0];
                        stock = getDataArray[1];
                        editName.setText(name);
                        editStock.setText(stock);
                        Log.d("name",name);
                        Log.d("stock",stock);
                    }
                    catch(ArrayIndexOutOfBoundsException e){
                        editName.setText(r);
                    }
                }
            }

        }
        else {
            Log.d("InputDataActivity", "Weird");
            super.onActivityResult(requestCode, resultCode, data);
            Toast.makeText(InputDataActivity.this, "else", Toast.LENGTH_LONG).show();
        }
    }

    private void makeQRImage(String type){
        QRCodeWriter gen = new QRCodeWriter();

        final int WIDTH = 480;
        final int HEIGHT = 480;

        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bmx = null;
        try {
            bmx = gen.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < WIDTH; ++i)
            for (int j = 0; j < HEIGHT; ++j) {
                bitmap.setPixel(i, j, bmx.get(i, j) ? Color.BLACK : Color.WHITE);
            }

        ImageView view = (ImageView) findViewById(R.id.imageView);

        view.setImageBitmap(bitmap);

        view.invalidate();

        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        String filePath = extStorageDirectory + "/QR Code Image/";
        File dir = new File(filePath);

        if(!dir.exists()){
            dir.mkdir();
        }

        fileName = filePath + fileName + ".png";

        File file = new File(fileName);

        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            if(!type.equals("재고등록"))
                Toast.makeText(InputDataActivity.this, type + " : " + data + " QR CODE Image Saved", Toast.LENGTH_LONG).show();
            else{
                String[] arrayData = new String[2];
                arrayData = data.trim().split("!@#@!");
                Toast.makeText(InputDataActivity.this, "식료품명 : " + arrayData[0] + " 재고수량 : " + arrayData[1] + " QR CODE Image Saved", Toast.LENGTH_LONG).show();
            }
            Log.d("file create", "File create");

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(InputDataActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d("FILE NOT FOUND", "File NOT FOUND");
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(InputDataActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d("IOE", "IOE");
        }
    }

    private void setData(Intent getIntent) {
        if(getIntent.getStringExtra("name") != null)
            editName.setText(getIntent.getStringExtra("name"));

        String[] freshness;
        try {
            freshness = getIntent.getStringExtra("freshness").split("-");

            switch(freshness[0]){
                case "16" :
                    spinnerYear.setSelection(0);
                    break;
                case "17" :
                    spinnerYear.setSelection(1);
                    break;
                case "18" :
                    spinnerYear.setSelection(2);
                    break;
                case "19" :
                    spinnerYear.setSelection(3);
                    break;
                case "20" :
                    spinnerYear.setSelection(4);
                    break;
                case "21" :
                    spinnerYear.setSelection(5);
                    break;
                case "22" :
                    spinnerYear.setSelection(6);
                    break;
                case "23" :
                    spinnerYear.setSelection(7);
                    break;
            }

            switch (freshness[1]){
                case "1" :
                    spinnerMonth.setSelection(0);
                    break;
                case "2" :
                    spinnerMonth.setSelection(1);
                    break;
                case "3" :
                    spinnerMonth.setSelection(2);
                    break;
                case "4" :
                    spinnerMonth.setSelection(3);
                    break;
                case "5" :
                    spinnerMonth.setSelection(4);
                    break;
                case "6" :
                    spinnerMonth.setSelection(5);
                    break;
                case "7" :
                    spinnerMonth.setSelection(6);
                    break;
                case "8" :
                    spinnerMonth.setSelection(7);
                    break;
                case "9" :
                    spinnerMonth.setSelection(8);
                    break;
                case "10" :
                    spinnerMonth.setSelection(9);
                    break;
                case "11" :
                    spinnerMonth.setSelection(10);
                    break;
                case "12" :
                    spinnerMonth.setSelection(11);
                    break;
            }

            switch (freshness[2]){
                case "1" :
                    spinnerDay.setSelection(0);
                    break;
                case "2" :
                    spinnerDay.setSelection(1);
                    break;
                case "3" :
                    spinnerDay.setSelection(2);
                    break;
                case "4" :
                    spinnerDay.setSelection(3);
                    break;
                case "5" :
                    spinnerDay.setSelection(4);
                    break;
                case "6" :
                    spinnerDay.setSelection(5);
                    break;
                case "7" :
                    spinnerDay.setSelection(6);
                    break;
                case "8" :
                    spinnerDay.setSelection(7);
                    break;
                case "9" :
                    spinnerDay.setSelection(8);
                    break;
                case "10" :
                    spinnerDay.setSelection(9);
                    break;
                case "11" :
                    spinnerDay.setSelection(10);
                    break;
                case "12" :
                    spinnerDay.setSelection(11);
                    break;
                case "13" :
                    spinnerDay.setSelection(12);
                    break;
                case "14" :
                    spinnerDay.setSelection(13);
                    break;
                case "15" :
                    spinnerDay.setSelection(14);
                    break;
                case "16" :
                    spinnerDay.setSelection(15);
                    break;
                case "17" :
                    spinnerDay.setSelection(16);
                    break;
                case "18" :
                    spinnerDay.setSelection(17);
                    break;
                case "19" :
                    spinnerDay.setSelection(18);
                    break;
                case "20" :
                    spinnerDay.setSelection(19);
                    break;
                case "21" :
                    spinnerDay.setSelection(20);
                    break;
                case "22" :
                    spinnerDay.setSelection(21);
                    break;
                case "23" :
                    spinnerDay.setSelection(22);
                    break;
                case "24" :
                    spinnerDay.setSelection(23);
                    break;
                case "25" :
                    spinnerDay.setSelection(24);
                    break;
                case "26" :
                    spinnerDay.setSelection(25);
                    break;
                case "27" :
                    spinnerDay.setSelection(26);
                    break;
                case "28" :
                    spinnerDay.setSelection(27);
                    break;
                case "29" :
                    spinnerDay.setSelection(28);
                    break;
                case "30" :
                    spinnerDay.setSelection(29);
                    break;
                case "31" :
                    spinnerDay.setSelection(30);
                    break;
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        if(getIntent.getStringExtra("stock") != null)
            editStock.setText(getIntent.getStringExtra("stock"));

        if(getIntent.getStringExtra("position") != null) {
            position = getIntent.getStringExtra("position");
        }
    }

    protected static boolean isNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }

}
