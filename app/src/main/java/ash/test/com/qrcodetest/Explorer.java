package ash.test.com.qrcodetest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Explorer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        // QR 코드 이미지 파일들의 목록을 출력해줄 리스트뷰 객체 생성
        ListView listView1 = (ListView) findViewById(R.id.listView1);

        // QR 코드 이미지 파일들이 위치하는 폴더 경로 지정
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        final String filePath = extStorageDirectory + "/QR Code Image/";

        File file = new File(filePath);
        File[] imageFileList = file.listFiles();

        // 폴더가 존재하지 않으면 폴더 생성
        if(!file.exists())
            file.mkdir();

        ArrayList<String> fileList = new ArrayList<>();

        // 폴더에 파일이 존재하지 않으면
        if(imageFileList == null){
            Toast.makeText(Explorer.this, "QR 코드 이미지가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
        // 폴더에 파일이 존재하면
        else{
            // 파일의 개수만큼 반복하여서 파일명을 ArrayList에 저장
            for(int i = 0; i < imageFileList.length; i++) {
                File subFile = imageFileList[i];
                String absPath = subFile.getName();
                fileList.add(absPath);
                Log.d("absPath", absPath);
            }

            // 어댑터를 이용하여서 커스텀 리스트뷰와 연결하고, 파일들 이름을 커스텀 리스트뷰 객체의 항목으로 출력
            final ArrayAdapter<String> fileAdapter = new ArrayAdapter<>(this, R.layout.item, R.id.listItem1, fileList);
            listView1.setAdapter(fileAdapter);

            // 커스텀 리스트뷰 항목의 클릭리스너 등록
            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // 파일을 공유하기 위해서 intent 설정하고, 파일 공유하기를 실행함
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");

                    File filetest = new File(filePath, fileAdapter.getItem(position));

                    Log.d("path", filetest.getAbsolutePath());

                    Uri uri = Uri.fromFile(filetest);

                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent,"공유"));

                }
            });
        }



    }
}
