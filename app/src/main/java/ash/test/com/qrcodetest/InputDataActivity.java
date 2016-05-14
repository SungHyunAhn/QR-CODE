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
    Button QRdataDelete;
    Button makeQRbtn;
    Button delData;
    Button changeData;
    EditText editName;
    //EditText editFreshness;
    Spinner spinnerYear;
    Spinner spinnerMonth;
    Spinner spinnerDay;
    EditText editStock;
    String freshness;
    String position;
    Intent getIntent;
    int[] arr = new int[3];

    Date date;

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
        QRdataDelete = (Button)findViewById(R.id.QRdataDelete);
        makeQRbtn = (Button)findViewById(R.id.makeQRbtn);

        delData = (Button)findViewById(R.id.delData);
        changeData = (Button)findViewById(R.id.changeData);

        delData.setText("식재료 삭제");
        changeData.setText("식재료 등록/변경");

        editName = (EditText)findViewById(R.id.editName);

        //editFreshness = (EditText)findViewById(R.id.editFreshness);

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

        if(getIntent.getStringExtra("name") != null)
            editName.setText(getIntent.getStringExtra("name"));

        if(getIntent.getStringExtra("freshness") != null){
            if(getIntent.getStringExtra("freshness").equals("좋음"))
                spinnerYear.setSelection(0);
            else if(getIntent.getStringExtra("freshness").equals("보통"))
                spinnerYear.setSelection(1);
            else
                spinnerYear.setSelection(2);
        }

        if(getIntent.getStringExtra("stock") != null)
            editStock.setText(getIntent.getStringExtra("stock"));

        if(getIntent.getStringExtra("position") != null) {
            position = getIntent.getStringExtra("position");
            Log.d("position", position);
        }

        QRdataRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(InputDataActivity.this).initiateScan();
            }
        });

        QRdataDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(InputDataActivity.this).initiateScan();
            }
        });

        makeQRbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeWriter gen = new QRCodeWriter();

                //String data = editName.getText().toString() + "!@#@!" +editFreshness.getText().toString() + "!@#@!" + editStock.getText().toString();a

                /*
                if (editName.length() == 0)
                    Toast.makeText(InputDataActivity.this, "식재료를 입력하세요", Toast.LENGTH_SHORT).show();

                else if(editFreshness.length() == 0){
                    Toast.makeText(InputDataActivity.this, "신선도를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else if(editStock.length() == 0)
                    Toast.makeText(InputDataActivity.this, "재고수를 입력하세요", Toast.LENGTH_SHORT).show();
                */
                if(editName.length() == 0 && editStock.length() == 0)
                    Toast.makeText(InputDataActivity.this, "식료품명이나 재고수량을 입력하세요", Toast.LENGTH_SHORT).show();

                else {
                    try {
                        String data;
                        String fileName;
                        Date day = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                        fileName = String.valueOf(sdf.format(day));

                        if(!editName.getText().toString().equals("")) {
                            fileName = fileName + " " + editName.getText().toString();
                            data = editName.getText().toString();
                        }
                        else if(!editStock.getText().toString().equals("")) {
                            fileName = fileName + " " + editStock.getText().toString();
                            data = editStock.getText().toString();
                        }
                        else {
                            fileName = fileName + " " + editName.getText().toString() + editStock.getText().toString();
                            data =  editName.getText().toString() + "!@#@!" + editStock.getText().toString();
                        }

                        final int WIDTH = 480;
                        final int HEIGHT = 480;

                        Hashtable hints = new Hashtable();
                        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                        BitMatrix bmx = gen.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
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

                        fileName = filePath+ fileName + ".png";

                        File file = new File(fileName);

                        OutputStream outStream = null;
                        try {
                            outStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                            outStream.flush();
                            outStream.close();

                            Toast.makeText(InputDataActivity.this, "QR CODE Image Saved", Toast.LENGTH_LONG).show();
                            Log.d("file create", "File create");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(InputDataActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            Log.d("FILE NOT FOUND", "File NOT FOUND");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(InputDataActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            Log.d("IOE", "IOE");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("e", "error");
                    }
                }
            }
        });


        changeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                intent.putExtra("name", editName.getText().toString());
                //intent.putExtra("freshness", editFreshness.getText().toString());
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
                AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                builder.setTitle("식료품 삭제")
                        .setMessage("정말로 식료품을 목록에서 삭제하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int whichButton){
                                Intent intent = new Intent();
                                intent.putExtra("position", position);
                                setResult(2,intent);
                                finish();
                            }
                        })

                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int whichButton){
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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
                    }
                    catch(ArrayIndexOutOfBoundsException e){

                    }

                    editName.setText(r);
                }
            }

        }
        else {
            Log.d("InputDataActivity", "Weird");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
