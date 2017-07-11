package enbledu.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ImageTest extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;
    private static String URL = "http://10.0.2.2:8080/1.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_test);
        imageView = (ImageView) findViewById(R.id.image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Log.d("kadsok", "ImageTest");
        new MyAsyncTask().execute(URL);
    }

    class MyAsyncTask extends AsyncTask<String,Void,Bitmap> {




        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            Log.d("hha", "onPreExecute");
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
        }

        //真正耗时操作
        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = null;
            URLConnection connection;
            InputStream is;
            try {
                URL Url = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)Url.openConnection();
                httpURLConnection.connect();
                is = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bitmap = BitmapFactory.decodeStream(bis);
                is.close();
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

    }

}
