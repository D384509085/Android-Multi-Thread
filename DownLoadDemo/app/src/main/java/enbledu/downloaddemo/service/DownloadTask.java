package enbledu.downloaddemo.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
    //总共完成的
    private volatile int allHaveFinished = 0;
    private  int mThreadCount = 0;
    private List<DownloadThread> threadList = null;

    public boolean isPause = false;


    //初始化时得到ThreadDAOImpl实例，可对数据库进行操作
    public DownloadTask(Context mContex, FileInfo mFileInfo, int threadCount) {
        this.mContext = mContex;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContex);
        this.mThreadCount = threadCount;
    }



    public void download() {
        //threadInfoList存储每个thread的信息
        List<ThreadInfo> threadInfoList = mDao.getThreads(mFileInfo.getUrl());

        for (ThreadInfo info : threadInfoList) {
            //Log.i("debug issue", "end 为"+String.valueOf(info.getEnd()));

        }

        //如果数据库里没有信息,则说明第一次下载,重新构造线程信息
        if(threadInfoList.size() == 0) {
            //获得每个线程下载长度
            int eachlength = mFileInfo.getLength() / mThreadCount;
            for(int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),eachlength*i, (i+1)*eachlength-1, 0);
                //最后一个线程
                if(i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //Log.d("holo", threadInfo.toString());

                threadInfoList.add(threadInfo);
                Log.d("DownLoadTask", "end为 "+ threadInfo.getEnd());
                //将每个线程的信息写入数据库
                mDao.insertThread(threadInfo);
            }

        }
        //用threadList管理thread,可用线程池优化
        threadList = new ArrayList<DownloadThread>();
            for (ThreadInfo threadInfo : threadInfoList) {
                DownloadThread thread = new DownloadThread(threadInfo);
                thread.start();
                //将线程添加到集合中
                threadList.add(thread);
            }
    }

    /**
     * 判断是否所有线程都执行完了,每个线程都有可能调用,所以加锁
     */
    private synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;
        //遍历线程集合
        for (DownloadThread thread : threadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            //从数据库删除线程信息
            mDao.deleteThread(mFileInfo.getUrl());
            //发送广播,通知下载结束
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", mFileInfo);
            intent.putExtra("id", mFileInfo.getId());
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        //线程是否执行完毕,用于判断是否结束
        public boolean isFinished = false;
        private int threadHaveFinished;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
            threadHaveFinished = 0;
        }
        public void run() {
            //向数据库插入线程信息

            Log.d("download", "hello");
            Log.d("download", "current thread is " + mThreadInfo.getUrl() + getId());
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
                Log.d("download","bytes="+start+"-"+mThreadInfo.getEnd());
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent1 = new Intent(DownloadService.ACTION_UPDATE);
                Intent intent2 = new Intent(DownloadService.ACTION_FINISHED);
                //得到当前线程下载进度,累加总共的下载进度
                threadHaveFinished += mThreadInfo.getFinished();
                allHaveFinished += mThreadInfo.getFinished();
                //Log.d("dow", mThreadInfo.getUrl() + getId() + "   " + conn.getResponseCode());

                input = conn.getInputStream();


                byte[] buffer = new byte[1024*4];
                int len = -1;
                long time = System.currentTimeMillis();
                while ((len = input.read(buffer))!= -1) {
                    raf.write(buffer,0,len);
                    //累加整个文件的进度
                    allHaveFinished += len;
                    //累加每个线程的进度
                    threadHaveFinished += len;
                    mThreadInfo.setFinished(threadHaveFinished);

                    if (System.currentTimeMillis() - time > 100) {
                        time = System.currentTimeMillis();
                        intent1.putExtra("finished", allHaveFinished*100/mFileInfo.getLength());
                        intent1.putExtra("id", mFileInfo.getId());
                        mContext.sendBroadcast(intent1);
                    }
                    if(isPause) {
                        mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), threadHaveFinished);
                        return;
                    }
                }
                //标记该线程已执行
                isFinished = true;

                //检查下载任务是否执行完毕
                //Log.d("DownLoadTask", String.valueOf(DownloadTask.this==null));
                DownloadTask.this.checkAllThreadsFinished();
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
