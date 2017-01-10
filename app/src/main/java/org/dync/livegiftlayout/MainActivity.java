package org.dync.livegiftlayout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.button1:
                startActivity(new Intent(MainActivity.this, Gift1Activity.class));
                break;
            case R.id.button2:
                startActivity(new Intent(MainActivity.this, Gift2Activity.class));
                break;
        }
    }
}
