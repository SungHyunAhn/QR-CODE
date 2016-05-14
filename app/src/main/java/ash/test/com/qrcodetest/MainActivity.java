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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ListView view;
    ListViewAdapter adapter;

    Button exp;
    ImageButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (ListView) findViewById(R.id.listView);

        adapter = new ListViewAdapter(this);
        view.setAdapter(adapter);

        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(view, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    adapter.remove(position);
                }
                adapter.notifyDataSetChanged();
            }
        });

        view.setOnTouchListener(touchListener);
        view.setOnScrollListener(touchListener.makeScrollListener());

        exp = (Button) findViewById(R.id.expBtn);
        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Explorer.class);
                startActivity(intent);
            }
        });


        SharedPreferences sp = this.getSharedPreferences("sp", MODE_PRIVATE);
        int size = 0;
        try {
            if (!sp.getString("size", "").equals(""))
                size = Integer.parseInt(sp.getString("size", ""));
        }
        catch (NumberFormatException e){

        }

        Log.d("size", String.valueOf(size));

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

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputData data = adapter.mListData.get(position);
                Log.d("data",data.getFreshness());
                Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                String pos = String.valueOf(position);
                intent.putExtra("position", pos);
                intent.putExtra("name", data.getName());
                intent.putExtra("freshness", data.getFreshness());
                intent.putExtra("stock", data.getStock());

                startActivityForResult(intent, 0);
            }
        });

        btn = (ImageButton) findViewById(R.id.imageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = adapter.getCount();
                Intent intent = new Intent(MainActivity.this, InputDataActivity.class);
                String pos = String.valueOf(size);
                intent.putExtra("position", pos);
                intent.putExtra("new","new");

                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 앱의 데이터를 로컬 저장
        SharedPreferences sp = this.getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        for (int i = 0; i < adapter.getCount(); i++) {
            inputData data = adapter.mListData.get(i);
            String str = data.getName() + "!@#@!" + data.getFreshness() + "!@#@!" + data.getStock();
            editor.putString(String.valueOf(i), str);
        }
        editor.putString("size", String.valueOf(adapter.getCount()));
        editor.commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        String position;
        Log.d("requestCode", String.valueOf(requestCode));

        // 다른 액티비티에서 데이터를 받는 경우
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    position = intent.getStringExtra("position");
                    inputData data = adapter.mListData.get(Integer.parseInt(position));
                    data.setName(intent.getStringExtra("name"));
                    String date;
                    date = intent.getStringExtra("year") + "-" + intent.getStringExtra("month") + "-" + intent.getStringExtra("day");
                    data.setFreshness(date);
                    data.setStock(intent.getStringExtra("stock"));
                    adapter.notifyDataSetChanged();

                } else if (resultCode == 2) {
                    adapter.remove(Integer.parseInt(intent.getStringExtra("position")));
                }
                adapter.notifyDataSetChanged();
                break;
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

            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview, null);

                holder.listName = (TextView) convertView.findViewById(R.id.name);
                holder.listFreshness = (TextView) convertView.findViewById(R.id.freshness);
                holder.listStock = (TextView) convertView.findViewById(R.id.stock);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            inputData mData = mListData.get(position);

            holder.listName.setText(mData.getName());
            holder.listFreshness.setText(mData.getFreshness());
            holder.listStock.setText(mData.getStock());

            long now = System.currentTimeMillis();
            long diffDays = 0;
            Date nowDate = getDate();

            String freshness = holder.listFreshness.getText().toString();
            SimpleDateFormat dateFormat = new  SimpleDateFormat("yy-MM-dd", java.util.Locale.getDefault());

            try {
                Date freshnessDate = dateFormat.parse(freshness);
                long diff = freshnessDate.getTime() - nowDate.getTime();
                diffDays = diff / (24 * 60 * 60 * 1000);
                Log.d("day",String.valueOf(diffDays));
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            if (diffDays<3) {
                convertView.setBackgroundColor(Color.RED);
            }
            else if (diffDays<5) {
                convertView.setBackgroundColor(Color.YELLOW);
            }
            else{
                convertView.setBackgroundColor(Color.WHITE);
            }

            adapter.notifyDataSetChanged();

            return convertView;
        }

        public void addItem(String name, String freshness, String stock) {
            inputData addInfo;
            addInfo = new inputData(name, freshness, stock);

            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        public void dataChange() {
            adapter.notifyDataSetChanged();
        }
    }

    public static Date getDate() {
        /*
        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH);
        int date = cal.get(cal.DATE);
        cal.set(year, month, date);
        cal.set(Calendar.MILLISECOND, 0);
        */

        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH);
        int date = cal.get(cal.DATE);

        cal.set(year, month, date, 0, 0, 0);

        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
}