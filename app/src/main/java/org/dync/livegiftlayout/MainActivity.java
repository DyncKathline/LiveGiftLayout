package org.dync.livegiftlayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.squareup.leakcanary.LeakCanary;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(MainActivity.this.getApplication());
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.button1:
                startActivity(new Intent(MainActivity.this, Gift1Activity.class));
                break;
        }
    }
}
