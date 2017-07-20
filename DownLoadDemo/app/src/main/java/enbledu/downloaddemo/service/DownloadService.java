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
import java.util.LinkedHashMap;
import java.util.Map;

import enbledu.downloaddemo.entities.FileInfo;

public class DownloadService extends Service {

    private String TAG = "DownloadService";
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<Integer, DownloadTask>();
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final int MSG_INIT = 0;

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_START.equals(intent.getAction())) {

                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                Log.i("onStartCommand", fileInfo.toString());
                InitThread mInitThread = new InitThread(fileInfo);
                DownloadTask.mExecutorService.execute(mInitThread);

            } else if (ACTION_STOP.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //从集合中取出下载任务
                DownloadTask task = mTasks.get(fileInfo.getId());
                if (task != null) {
                    task.isPause = true;
                }
            }
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
                    Log.i("DownloadService", "init" + fileInfo.toString());
                    //启动下载任务
                    DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
                    task.download();
                    mTasks.put(fileInfo.getId(), task);
                    break;
                }
            }
        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread {

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
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, mFileInfo.getFileName());
                Log.i("run", "last");
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
                Log.i("Service", "handler");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
