package ash.test.com.qrcodetest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ListView view; // 리스트뷰
    ListViewAdapter adapter; // 리스트뷰 어댑터

    Button del; // QR 코드 스캔하여서 항목 삭제하는 버튼
    Button exp; // QR 코드 이미지 목록 보는 버튼
    ImageButton btn; // 새 항목 추가 버튼

    String delData; // QR 코드로 항목 삭제시 사용하는 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (ListView) findViewById(R.id.listView); // 리스트뷰 객체 생성

        adapter = new ListViewAdapter(this); // 어댑터 객채 생성
        view.setAdapter(adapter); // 커스텀 리스트뷰에 어댑터 연결


        /*
        리스트뷰를 스와이프 하여서 커스텀 리스트뷰의 항목을 삭제하는 코드
        https://github.com/romannurik/Android-SwipeToDismiss 소스 사용
         */

        // 커스텀 리스트뷰의 터치리스너 생성
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(view, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            // 항목을 스퐈이프 하였을 때
            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    adapter.remove(position); // 선택한 항목을 삭제
                }
                adapter.notifyDataSetChanged();
            }
        });

        // 생성한 터치리스너를 커스텀 리스트뷰에 등록
        view.setOnTouchListener(touchListener);
        view.setOnScrollListener(touchListener.makeScrollListener());

        // QR 코드로 항목 삭제하는 버튼의 객체 생성, 후에 버튼리스너 등록
        del = (Button)findViewById(R.id.delBtn);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "삭제할 식료품명을 스캔하면 즉시 삭제됩니다.", Toast.LENGTH_LONG).show();
                // QR 코드 라이브러리(ZXING)를 이용하여서 QR 코드 스캔창을 실행, QR 코드로 스캔한 데이터는 OnActivityResult 메소드에서 받아온다.
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });

        // QR 코드 이미지 목록을 여는 버튼의 객체 생성, 후에 버튼 리스너 등록
        exp = (Button) findViewById(R.id.expBtn);
        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Explorer.class);
                startActivity(intent); // 탐색기 액티비티 시작
            }
        });

        /*
        앱의 데이터를 휴대폰의 로컬데이터로 저장하는 SharedPreferences 인터페이스 사용
        앱의 데이터는 onDestroy() 호출시에 로컬 데이터로 저장한다
        앱 시작시 onCreate() 호출시에 로컬 데이터로 저장되어 있는 정보들을 파싱하여서 커스텀 리스트뷰의 각 항목에 다시 집어넣는다.
         */
        SharedPreferences sp = this.getSharedPreferences("sp", MODE_PRIVATE);
        int size = 0;
        try {
            // onDestroy()에서 로컬 데이터로 저장한 커스텀 리스트뷰의 항목 개수를 받아와서 int형 변수로 변환
            if (!sp.getString("size", "").equals(""))
                size = Integer.parseInt(sp.getString("size", ""));
        }
        catch (NumberFormatException e){
            e.printStackTrace();
        }

        Log.d("size", String.valueOf(size));

        // 반복문을 이용하여서 로컬 데이터를 파싱하여서 커스텀 리스트뷰의 항목으로 추가
        for (int i = 0; i < size; i++) {
            String name;
            String freshness;
            String stock;
            String data = sp.getString(String.valueOf(i), "");
            String[] dataArray = data.split("!@#@!");

            name = dataArray[0];
            freshness = dataArray[1];
            stock = dataArray[2];

            adapter.addItem(name, freshness, stock);

            adapter.notifyDataSetChanged();
        }

        // 커스텀 리스트뷰의 아이템 클릭 리스너 등록
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // inputData 클래스는 데이터를 입력하여 저장하고 있는 클래스
                inputData data = adapter.mListData.get(position);
                Log.d("data",data.getFreshness());
                Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                String pos = String.valueOf(position);
                intent.putExtra("position", pos);
                intent.putExtra("name", data.getName());
                intent.putExtra("freshness", data.getFreshness());
                intent.putExtra("stock", data.getStock());

                startActivityForResult(intent, 0); // 항목 선택시에 현재 항목의 데이터를 intent로 데이터 입력 화면으로 전달
            }
        });

        // 새 항목 추가 버튼 객체 생성 및 버튼 리스너 등록
        btn = (ImageButton) findViewById(R.id.imageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = adapter.getCount();
                Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                String pos = String.valueOf(size);
                intent.putExtra("position", pos);
                intent.putExtra("new","new"); // 새 항목 버튼 만들기라는걸 알려줘서, 데이터 입력 화면에서 항목 삭제 버튼 기능이 작동되지 않도록 함

                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 앱의 데이터를 로컬 데이터로 저장하기 위한 객체 생성
        SharedPreferences sp = this.getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // 반복문을 이용하여서 커스텀 리스트뷰의 각 항목을 순서대로 저장
        for (int i = 0; i < adapter.getCount(); i++) {
            inputData data = adapter.mListData.get(i);
            String str = data.getName() + "!@#@!" + data.getFreshness() + "!@#@!" + data.getStock();
            editor.putString(String.valueOf(i), str);
        }
        editor.putString("size", String.valueOf(adapter.getCount())); // 커스텀 리스트뷰의 항목 개수를 저장
        editor.commit(); // 로컬 데이터로 저장
    }

    // 다른 액티비티에서 데이터를 받아오는 경우에 자동으로 호출되는 메소드
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // QR 코드를 스캔한 경우 그 결과값이 저장되는 변수
       IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        // QR 코드 스캔한 경우
       if(result != null){
           // QR 코드로 스캔한 데이터가 없는 경우
           if(result.getContents() == null) {
               Log.d("MainActivity", "Cancelled scan");
               Toast.makeText(this, "Cancelled Scan", Toast.LENGTH_LONG).show();
           }
           // QR 코드로 스캔한 데이터가 있는 경우
           else{
               // QR 코드로 읽어온 데이터에 파싱용 스트링이 있는지 체크하기 ㅇ위한 변수
               int tmp = result.getContents().indexOf("!@#@!");
               // QR 코드로 읽어온 데이터에서 파싱용 스트링이 없으면
               if(tmp == -1){
                   // QR 코드로 읽어온 데이터를 삭제에 사용할 변수에 저장
                   delData = result.getContents();
                   Log.d("delData",delData);
               }
               // QR 코드로 읽어온 데이터에서 파싱용 스트링이 있으면
               else{
                   // QR 코드로 읽어온 데이터에서 파싱용 스트링 앞까지 잘라내여서 삭제에 사용할 변수에 저장
                   delData = result.getContents().substring(0,tmp);
                   Log.d("delData",delData);
               }
               // 커스텀 리스트뷰의 항목 개수만큼 반복
               for(int i = 0; i < adapter.getCount(); i++){
                   inputData data = adapter.mListData.get(i);
                   // 삭제에 사용할 변수와 항목의 식료품명이 같다면 삭제
                   if(data.getName().equals(delData)) {
                       adapter.remove(i);
                       Toast.makeText(MainActivity.this, delData + " 항목을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                   }
               }
           }
       }
       // 다른 액티비티(데이터 입력 화면)에서 데이터를 받는 경우
       else{
            // 선택한 항목의 위치를 저장하는 변수
            String position;
            Log.d("requestCode", String.valueOf(requestCode));

           // requestCode를 사용해서 어떠한 요청인지 구분한다. 0 : 항목을 클릭하여서 데이터를 수정하는 경우, 1: 새로운 항목을 입력하는 경우
            switch (requestCode) {
                case 0:
                    // resultCode를 이용하여서 데이터 입력 화면에서 어떠한 결과를 처리하여서 데이터를 넘겨주는지 확인한다, RESULT_OK : 데이터 입력,수정, 2 : 데이터 삭제
                    if (resultCode == RESULT_OK) {
                        // 항목의 위치를 받아와서 저장
                        position = intent.getStringExtra("position");
                        // 해당 항목의 위치를 설정하여서 데이터 입력, 수정
                        inputData data = adapter.mListData.get(Integer.parseInt(position));
                        data.setName(intent.getStringExtra("name"));
                        String date;
                        date = intent.getStringExtra("year") + "-" + intent.getStringExtra("month") + "-" + intent.getStringExtra("day");
                        data.setFreshness(date);
                        data.setStock(intent.getStringExtra("stock"));
                        adapter.notifyDataSetChanged();

                    }
                    // 데이터 입력 화면에서 항목 삭제 버튼을 눌렀을 경우 해당 항목 삭제
                    else if (resultCode == 2) {
                        adapter.remove(Integer.parseInt(intent.getStringExtra("position")));
                    }
                    adapter.notifyDataSetChanged();
                    break;
                // 최초 화면에서 새 항목 이미지버튼 눌러서 실행된 데이터 입력 화면에서 입력한 데이터가 넘어왔을 때 리스트뷰의 항목으로 저장
                case 1:
                    if (resultCode == RESULT_OK) {
                        String date;
                        date = intent.getStringExtra("year") + "-" + intent.getStringExtra("month") + "-" + intent.getStringExtra("day");
                        adapter.addItem(intent.getStringExtra("name"), date, intent.getStringExtra("stock"));
                    }
                  adapter.notifyDataSetChanged();
                  break;
            }
       }
    }

    // 뷰홀더
    private class ViewHolder {
        public TextView listName;
        public TextView listFreshness;
        public TextView listStock;
    }

    // 커스텀 리스트 뷰 어댑터
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<inputData> mListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // view가 없다면 새로운 뷰를 생성
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview, null);

                // 뷰홀더를 통해 커스텀 리스트뷰 내부의 객체 생성
                holder.listName = (TextView) convertView.findViewById(R.id.name);
                holder.listFreshness = (TextView) convertView.findViewById(R.id.freshness);
                holder.listStock = (TextView) convertView.findViewById(R.id.stock);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            // inputData 클래스를 통해서 데이터를 가져오고 해당 데이터를 커스텀 리스트뷰의 각 항목에 설정
            inputData mData = mListData.get(position);

            holder.listName.setText(mData.getName());
            holder.listFreshness.setText(mData.getFreshness());
            holder.listStock.setText(mData.getStock());

            long diffDays = 0;
            Date nowDate = getDate(); // 앱을 실행한 날짜(yy-MM-dd)를 받아옴

            String freshness = holder.listFreshness.getText().toString(); // 유통기한 값을 받아와서 저장
            SimpleDateFormat dateFormat = new  SimpleDateFormat("yy-MM-dd", java.util.Locale.getDefault());

            try {
                Date freshnessDate = dateFormat.parse(freshness); // String으로 저장된 유통기한 값을 Date 형식으로 변환
                long diff = freshnessDate.getTime() - nowDate.getTime(); // 유통기한 날짜와 현재 날짜의 차이를 계산
                diffDays = diff / (24 * 60 * 60 * 1000);
                Log.d("day",String.valueOf(diffDays));
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            if (diffDays<3) {
                convertView.setBackgroundColor(Color.RED); // 유통기한 날짜가 2일 이하로 남으면 해당 항목의 배경을 빨강으로 변경
            }
            else if (diffDays<5) {
                convertView.setBackgroundColor(Color.YELLOW); // 유통기한 날짜가 3~4일 남으면 해당 항목의 배경을 노랑으로 변경
            }
            else{
                convertView.setBackgroundColor(Color.WHITE); // 유통기한 날짜가 5일 이상 남아있다면 해당 항목의 배경을 하양으로 변경
            }

            adapter.notifyDataSetChanged();

            return convertView;
        }

        // 새 항목 추가
        public void addItem(String name, String freshness, String stock) {
            inputData addInfo;
            addInfo = new inputData(name, freshness, stock);

            mListData.add(addInfo);
        }

        // 항목 삭제
        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        // 데이터 변경을 알려줌
        public void dataChange() {
            adapter.notifyDataSetChanged();
        }
    }

    // 현재 날짜를 계산하는 메소드(yy-MM-dd)
    public static Date getDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);

        cal.set(year, month, date, 0, 0, 0);

        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
}