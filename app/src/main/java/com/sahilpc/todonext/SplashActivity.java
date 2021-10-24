package com.sahilpc.todonext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    sleep(3200);
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }catch (Exception e){

                }

            }
        };

        thread.start();

    }
}