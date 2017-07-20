package enbledu.downloaddemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import enbledu.downloaddemo.R;
import enbledu.downloaddemo.entities.FileInfo;
import enbledu.downloaddemo.service.DownloadService;

/**
 * Created by Administrator on 2017/7/16 0016.
 */

public class FileListAdapter extends BaseAdapter{

    private Context mContext = null;
    private List<FileInfo> mFileList;
    private FileInfo fileInfo;

    public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
           convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.text);
            holder.btnStart = (Button) convertView.findViewById(R.id.btn_start);
            holder.btnStop = (Button) convertView.findViewById(R.id.btn_stop);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.download_progress);
            holder.imageView = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        final FileInfo fileInfo = mFileList.get(position);
        //Log.i("Adapter", fileInfo.toString());
        holder.textView.setText(fileInfo.getFileName());
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(fileInfo.getFinished());
        holder.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });
        holder.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                mContext.startService(intent);
            }
        });

        if (fileInfo.getIsfinished()) {
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + fileInfo.getFileName();
            File file = new File(filepath);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(filepath);
                //将图片显示到ImageView中
                holder.imageView.setImageBitmap(bm);
            }
        }
        return convertView;
    }


    /**
     * 更新列表的进度条
     */
    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }
    public void setImage(int id) {
        FileInfo fileInfo = mFileList.get(id);
            fileInfo.setIsfinished(true);
            notifyDataSetChanged();
        //Log.i("asd","asdsad");
    }
    //静态类只会被加载一次
    static class ViewHolder {
        TextView textView;
        Button btnStart;
        Button btnStop;
        ProgressBar progressBar;
        ImageView imageView;
    }
}
