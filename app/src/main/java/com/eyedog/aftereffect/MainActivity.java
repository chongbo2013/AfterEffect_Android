package com.eyedog.aftereffect;

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

    public void startCamera(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    public void startJni(View view) {

    }
}
