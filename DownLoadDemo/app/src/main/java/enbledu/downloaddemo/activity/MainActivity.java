package enbledu.downloaddemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import enbledu.downloaddemo.R;

public class MainActivity extends AppCompatActivity {

    private TextView fileName;
    private ProgressBar progressBar;
    private Button btnStop;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileName = (TextView) findViewById(R.id.text);
        progressBar = (ProgressBar) findViewById(R.id.download_progress);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStart = (Button) findViewById(R.id.btn_start);
    }

}
