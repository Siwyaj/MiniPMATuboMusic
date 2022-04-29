package com.example.minipmatubomusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity{
    private EditText setSpeed;
    private float speed;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempomatcher);
        //editor.putFloat("speed",5);
        SharedPreferences prefs = getSharedPreferences("speed", MODE_PRIVATE);
        setSpeed = findViewById(R.id.setSpeed);
        speed=prefs.getFloat("setSpeed",0);
        setSpeed.setText(String.valueOf(speed));
        editor = getSharedPreferences(
                "speed", MODE_PRIVATE).edit();
    }

    public void setSpeed(View view){
        String stringHolder = setSpeed.getText().toString();
        speed = Float.parseFloat(stringHolder);
        editor.putFloat("setSpeed",speed);
        editor.apply();
    }
}