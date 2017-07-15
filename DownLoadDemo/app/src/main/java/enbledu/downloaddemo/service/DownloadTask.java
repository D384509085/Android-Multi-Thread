package enbledu.downloaddemo.service;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import enbledu.downloaddemo.database.ThreadDAO;
import enbledu.downloaddemo.database.ThreadDAOImpl;
import enbledu.downloaddemo.entities.FileInfo;
import enbledu.downloaddemo.entities.ThreadInfo;

/**
 * 下载任务类
 * Created by Administrator on 2017/7/15 0015.
 */

public class DownloadTask {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    private int mFinished = 0;
    private int mThreadCount = 1;
    private List<DownloadThread> threadList = null;

    public boolean isPause = false;


    public DownloadTask(Context mContex, FileInfo mFileInfo, int threadCount) {
        this.mContext = mContex;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContex);
        this.mThreadCount = threadCount;
    }

    public void download() {
        List<ThreadInfo> threadInfoList = mDao.getThreads(mFileInfo.getUrl());
        if(threadInfoList.size() == 0) {
            //获得每个线程下载长度
            int eachlength = mFileInfo.getLength() / mThreadCount;
            for(int i = 0; i < mThreadCount-1; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),eachlength*i, (i+1)*eachlength-1, 0);
                //最后一个线程
                if(i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threadInfoList.add(threadInfo);
            }
            threadList = new ArrayList<DownloadThread>();
            for (ThreadInfo threadInfo : threadInfoList) {
                DownloadThread thread = new DownloadThread(threadInfo);
                thread.start();
                //将线程添加到集合中
                threadList.add(thread);
            }

        }
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
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes="+start+"-"+mThreadInfo.getEnd());
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent1 = new Intent(DownloadService.ACTION_UPDATE);
                Intent intent2 = new Intent(DownloadService.ACTION_FINISHED);
                mFinished += mThreadInfo.getFinished();
                input = conn.getInputStream();
                byte[] buffer = new byte[1024*4];
                int len = -1;
                long time = System.currentTimeMillis();
                while ((len = input.read(buffer))!= -1) {
                    raf.write(buffer,0,len);
                    mFinished += len;

                    if (System.currentTimeMillis() - time > 500) {
                        time = System.currentTimeMillis();
                        intent1.putExtra("finished", mFinished*100/mFileInfo.getLength());
                        mContext.sendBroadcast(intent1);
                    }
                    if(isPause) {
                        mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                        return;
                    }
                    if (mFinished == mFileInfo.getFinished()) {

                    }
                }
                //从数据库删除线程信息
                mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                conn.disconnect();
                try {
                    input.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



        }
    }
}
