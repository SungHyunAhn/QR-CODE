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

/**
 * Created by 성현 on 2016-05-05.
 */
public class Explorer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        ListView listView1 = (ListView) findViewById(R.id.listView1);

        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        final String filePath = extStorageDirectory + "/QR Code Image/";

        File file = new File(filePath);
        File[] files = file.listFiles();

        if(!file.exists())
            file.mkdir();

        ArrayList<String> fileList = new ArrayList<String>();

        if(files == null){
            Toast.makeText(Explorer.this, "QR 코드 이미지가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            for (int i = 0; i < files.length; i++) {
                File subFile = files[i];
                String absPath = subFile.getName();
                fileList.add(absPath);
                Log.d("absPath", absPath);
            }
            final ArrayAdapter<String> fileAdapter = new ArrayAdapter<String>(this, R.layout.item, R.id.listItem1, fileList);
            listView1.setAdapter(fileAdapter);

            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

                startActivity(intent);
                */

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");

                    String path = filePath + fileAdapter.getItem(position);

                    intent.putExtra(intent.EXTRA_STREAM, Uri.parse(path));
                    startActivity(Intent.createChooser(intent,"공유"));

                }
            });
        }



    }
}
