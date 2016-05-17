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

public class InputDataActivity extends Activity{

    Button QRdataRead;
    Button makeQRbtn;
    Button delData;
    Button changeData;
    EditText editName;
    Spinner spinnerYear;
    Spinner spinnerMonth;
    Spinner spinnerDay;
    EditText editStock;
    String position;
    Intent getIntent;
    int[] dateArray = new int[3];
    String data;
    String fileName;

    Date day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // QR 코드로 읽기 버튼 객체 생성
        QRdataRead = (Button)findViewById(R.id.QRdataRead);
        // QR 코드 만들기 버튼 객체 생성
        makeQRbtn = (Button)findViewById(R.id.makeQRbtn);

        // 항목 삭제 버튼 객체 생성
        delData = (Button)findViewById(R.id.delData);
        // 항목 입력,변경 버튼 객체 생성
        changeData = (Button)findViewById(R.id.changeData);

        delData.setText("식재료 삭제");
        changeData.setText("식재료 등록/변경");

        // 식료품명을 입력하는 에디트텍스트 객체 생성
        editName = (EditText)findViewById(R.id.editName);

        // 유통기한 날짜 중 연도에 해당하는 스피너 객채 생성 및 객체를 어댑터에 연결하고 레이아웃 설정
        spinnerYear = (Spinner)findViewById(R.id.spinnerYear);
        ArrayAdapter adsy = ArrayAdapter.createFromResource(this, R.array.year, R.layout.spinnerlayout);
        adsy.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerYear.setAdapter(adsy);

        // 유통기한 날짜 중 달에 해당하는 스피너 객채 생성 및 객체를 어댑터에 연결하고 레이아웃 설정
        spinnerMonth = (Spinner)findViewById(R.id.spinnerMonth);
        ArrayAdapter adsm = ArrayAdapter.createFromResource(this, R.array.month, R.layout.spinnerlayout);
        adsm.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerMonth.setAdapter(adsm);

        // 유통기한 날짜 중 일에 해당하는 스피너 객채 생성 및 객체를 어댑터에 연결하고 레이아웃 설정
        spinnerDay = (Spinner)findViewById(R.id.spinnerDay);
        ArrayAdapter adsd = ArrayAdapter.createFromResource(this, R.array.day, R.layout.spinnerlayout);
        adsd.setDropDownViewResource(R.layout.spinnerlayout);
        spinnerDay.setAdapter(adsd);

        // 연도 선택시 해당하는 아이템 값을 날짜를 저장하는 배열에 저장
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateArray[0] = Integer.parseInt(spinnerYear.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 달 선택시 해당하는 아이템 값을 날짜를 저장하는 배열에 저장
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateArray[1] = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 일 선택시 해당하는 아이템 값을 날짜를 저장하는 배열에 저장
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateArray[2] = Integer.parseInt(spinnerDay.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 재고수량을 입력하는 에디트텍스트 객체 생성
        editStock = (EditText)findViewById(R.id.editStock);

        // 최초 실행 화면에서 넘겨준 데이터를 저장하는 변수
        getIntent = getIntent();

        setData(getIntent); // 넘겨온 데이터를 화면에 세팅하는 메소드

        // QR 코드 읽기 버튼을 눌렀을 때 작동하는 버튼리스너 등록
        QRdataRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(InputDataActivity.this).initiateScan();
            }
        });

        // QR 코드 이미지 생성 버튼 눌렀을 때 작동하는 버튼리스너 등록
        makeQRbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    day = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA); // 파일 이름에 오늘 날짜를 적기 위해서 포맷 설정
                    fileName = String.valueOf(sdf.format(day)); // 파일이름에 오늘 날짜를 저장

                    /*
                    AlertDialog로 버튼 클릭시 3개의 버튼을 생성,

                    Positive 버튼 -> 식료품명만 QR 코드의 데이터로 저장
                    Negative 버튼 -> 재고수량만 QR 코드의 데이터로 저장
                    Neutral 버튼 -> 재고수량 + 유통기한(추가예정) + 재고수량 모두 QR 코드의 데이터로 저장

                    makeQRImage(String string) -> QR 코드 이미지를 생성하는 메소드

                     */
                    AlertDialog.Builder builder = new AlertDialog.Builder(InputDataActivity.this);
                    builder.setTitle("QR 코드 생성");
                    builder.setMessage("QR 코드를 생성할 방식을 정해주세요.");
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

        // 항목 입력, 변경 버튼의 버튼리스너 등록, 이 데이터를 최초 실행 화면으로 넘겨준다
        changeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("position", position);
                intent.putExtra("name", editName.getText().toString());
                intent.putExtra("year", String.valueOf(dateArray[0]));
                intent.putExtra("month", String.valueOf(dateArray[1]));
                intent.putExtra("day", String.valueOf(dateArray[2]));
                intent.putExtra("stock", editStock.getText().toString());

                setResult(RESULT_OK, intent);

                finish();
            }
        });

        // 항목 삭제 버튼 버튼리스너 등록, 새 항목 만들기일 경우에는 삭제할 항목이 없어서 삭제X, 아닐경우에는 해당 항목 삭제
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

        // QR 코드로 스캔한 경우
        if(result != null) {
            // QR 코드 스캔이 실패한 경우
            if(result.getContents() == null) {
                Log.d("InputDataActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
            // QR 코드 스캔이 성공한 경우
            else {
                Log.d("InputDataActivity", "Scanned");
                String r = result.getContents();

                /*
                isNumber(String string) -> 입력한 문자열이 숫자로 변환 가능한지 알려주는 메소드

                1. 입력받은 값이 숫자다 -> 식료품명 중에 숫자로만 되어 있는건 없으니까 재고수량에 집어넣는다
                2. 입력받은 값이 숫자가 아니다
                  2.1 입력받은 값 중에 파싱용 스트링(!@#@!)이 존재한다 -> 문자열을 파싱하여서 식료품명과 재고수량에 각각 집어넣는다
                  2.2 입력받은 값 중에 파싱용 스트링이 존재하지 않는다 -> 해당 값을 식료품명에 집어넣는다

                2.1의 입력 데이터에서 유통기한도 추가할 예정
                 */
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

    // QR 코드 이미지를 생성하는 메소드
    private void makeQRImage(String type){
        // QR 코드를 만드는 객체 생성
        QRCodeWriter gen = new QRCodeWriter();

        // QR 코드 이미지 크기
        final int WIDTH = 480;
        final int HEIGHT = 480;

        // QR 코드는 기본적으로 UTF-8 방식을 지원하지 않으므로, Encode 방식을 UTF-8로 변경
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // 비트맵매트릭스를 이용하여서 QR 코드를 Encode
        BitMatrix bmx = null;
        try {
            bmx = gen.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }

        // 비트맵 객체를 하나 생성하고, 그 객체의 픽셀값으로 생성해둔 QR 코드의 비트맵매트릭스 픽셀을 집어넣는다.
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < WIDTH; ++i)
            for (int j = 0; j < HEIGHT; ++j) {
                bitmap.setPixel(i, j, bmx.get(i, j) ? Color.BLACK : Color.WHITE);
            }

        // QR 코드 이미지를 보여줄 이미지뷰 객체 생성
        ImageView view = (ImageView) findViewById(R.id.imageView);

        // 이미지뷰 객체에 QR 코드 이미지를 보여줌
        view.setImageBitmap(bitmap);

        // 이미지뷰의 화면 갱신
        view.invalidate();

        // 생성한 비트맵 이미지를 휴대폰에 저장하기 위해서 휴대폰 내장메모리의 경로를 저장
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        // 내장메모리에 QR Code Image 폴더를 만들고, 폴더가 존재하지 않으면 폴더를 생성
        String filePath = extStorageDirectory + "/QR Code Image/";
        File dir = new File(filePath);

        if(!dir.exists()){
            dir.mkdir();
        }

        // 저장될 QR 코드 이미지 파일의 경로는 내장메모리/QR Code Image/yyyy-MM-dd 데이터.png
        fileName = filePath + fileName + ".png";

        File file = new File(fileName);

        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            /*
            makeQRImage() 메소드의 인자로 넘어온 값에 따라서 파일명을 다르게 생성함
            type = 식료품명,재고수량 -> yyyy-MM-dd 식료품명(or 재고수량) : 값.png
            type = 재고등록 -> yyyy-MM-dd 식료품명 : 값 재고수량 : 값.png
            */
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

    // 전역변수로 만들어진 변수들의 값을 설정하는 함수, 즉 식료품명, 유통기한, 재고수량의 값들을 알맞게 세팅함
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

    // 입력받은 문자열이 정수형으로 변환 가능한지 확인하는 메소드
    private static boolean isNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }

}
