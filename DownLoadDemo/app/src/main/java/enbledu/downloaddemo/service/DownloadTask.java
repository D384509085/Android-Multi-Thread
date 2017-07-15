package enbledu.downloaddemo.service;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import enbledu.downloaddemo.database.ThreadDAO;
import enbledu.downloaddemo.database.ThreadDAOImpl;
import enbledu.downloaddemo.entities.FileInfo;
import enbledu.downloaddemo.entities.ThreadInfo;

/**
 * Created by Administrator on 2017/7/15 0015.
 */

public class DownloadTask {
    private Context mContex = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    public DownloadTask(Context mContex, FileInfo mFileInfo) {
        this.mContex = mContex;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContex);
    }
    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }
        public void run() {
            //向数据库插入线程信息
            if(!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }
            try {
                URL url = new URL(mThreadInfo.getUrl());
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes="+start+"-"+mThreadInfo.getEnd());
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                InputStream input = conn.getInputStream();
                byte[] buffer = new byte[1024*4];
                int len = -1;
                
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }
}
