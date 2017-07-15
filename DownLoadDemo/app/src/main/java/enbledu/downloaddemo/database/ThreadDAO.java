package enbledu.downloaddemo.database;

import java.util.List;

import enbledu.downloaddemo.entities.ThreadInfo;

/**
 * Created by Administrator on 2017/7/15 0015.
 */

public interface ThreadDAO {

    /**
     *
     * 插入线程信息
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     *
     * 删除线程
     */
    public void deleteThread(String url,int thread_id);

    /**
     * 更新线程下载进度
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url,int thread_id, int finished);

    /**
     * 查询线程信息
     */
    public List<ThreadInfo> getThreads(String url);
    /**
     * 线程信息是否存在
     */
    public boolean isExists(String url,int thread_id);
}
