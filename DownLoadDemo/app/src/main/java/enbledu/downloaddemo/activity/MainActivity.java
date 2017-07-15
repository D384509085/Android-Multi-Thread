package enbledu.downloaddemo.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import enbledu.downloaddemo.R;
import enbledu.downloaddemo.adapter.FileListAdapter;
import enbledu.downloaddemo.entities.FileInfo;
import enbledu.downloaddemo.service.DownloadService;


public class MainActivity extends AppCompatActivity{

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ListView listview =null;
    private List<FileInfo> filelist = null;
    private FileListAdapter fileListAdapter = null;
    BroadcastReceiver mReceiver;
    FileInfo fileInfo1;
    FileInfo fileInfo2;
    FileInfo fileInfo3;
    FileInfo fileInfo0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.listview);
        //文件的集合
        filelist = new ArrayList<FileInfo>();
        //创建文件信息对象
        fileInfo0 = new FileInfo(0,"http://10.0.2.2:8080/0.jpg","0.jpg",0,0);
        fileInfo1 = new FileInfo(1,"http://10.0.2.2:8080/1.jpg","1.jpg",0,0);
        fileInfo2 = new FileInfo(2,"http://10.0.2.2:8080/2.jpg","2.jpg",0,0);
        fileInfo3 = new FileInfo(3,"http://10.0.2.2:8080/3.jpg","3.jpg",0,0);
        filelist.add(fileInfo0);
        filelist.add(fileInfo1);
        filelist.add(fileInfo2);
        filelist.add(fileInfo3);
        fileListAdapter = new FileListAdapter(this, filelist);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        listview.setAdapter(fileListAdapter);


       /* String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/beauty.png";
        img = (ImageView) findViewById(R.id.img);
        File file = new File(filepath);
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(filepath);
            //将图片显示到ImageView中
            img.setImageBitmap(bm);
        }*/
        Log.i("asd","asdsad");


        //广播接收器
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                    int finished = intent.getIntExtra("finished",0);

                }
            }
        };
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fielInfo", fileInfo0);
                    startService(intent);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
*/
    /*@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start: {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_CONTACTS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }else {
                    Intent intent = new Intent(MainActivity.this, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    startService(intent);
                }
                break;
            }
        }
    }*/
}
