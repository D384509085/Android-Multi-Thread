package enbledu.downloaddemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import enbledu.downloaddemo.entities.FileInfo;

public class DownloadService extends Service {

    private String TAG = "DownloadService";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final int MSG_INIT = 0;

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i(TAG,fileInfo.toString()+"start");

            new InitThread(fileInfo).start();
        } else  if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i(TAG,fileInfo.toString()+"stop");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT: {
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i("test", "init"+ fileInfo.toString());
                }
            }
        }
    };
    /**
     * 初始化子线程
     */
    class InitThread extends Thread{
        
        private FileInfo mFileInfo = null;
        
        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            //连接网络文件
            try {
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int length = -1;
                length = conn.getContentLength();
                if (length<=0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if(!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, mFileInfo.getFileName());
                Log.i("run","last");
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();
                Log.i("Service", "handler");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                /*try {
                    //raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }
}
