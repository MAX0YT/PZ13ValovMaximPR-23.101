package com.example.pz13valovmaximpr_23101mobil;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ((Button)findViewById(R.id.btn_start)).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public boolean Started = false;
    public boolean Finished = false;
    public void Start(View view){
        Button button = (Button)findViewById(R.id.btn_start);
        if(!Finished){
            if(!Started){
                button.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                button.setText("Пауза");
                Started = true;
            }
            else if(Started){
                button.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                button.setText("Старт");
                Started = false;
            }
        }
        else{
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
    public void Drive1(View view){
        Button button = (Button)findViewById(R.id.btn_green);
    }
    public void Drive2(View view){
        Button button = (Button)findViewById(R.id.btn_green);
    }
}