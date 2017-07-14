package enbledu.downloaddemo.entities;

/**
 * Created by Administrator on 2017/7/14 0014.
 */

public class ThreadInfo {
    private int id;
    private String url;
    private int start;
    private int end;
    private int finished;

    public ThreadInfo(int id, String url, int start, int end, int finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", finished=" + finished +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getFinished() {
        return finished;
    }
}
