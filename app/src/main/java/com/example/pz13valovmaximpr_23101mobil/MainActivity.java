package com.example.pz13valovmaximpr_23101mobil;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    public float length;
    public float car1Pos;
    public float car2Pos;
    public boolean Started;
    public boolean Finished;
    public int screenWidth;
    public int finishWidth;
    public View Car1;
    public View Car2;
    public int carLength;
    public int startMargin;
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
        length = 100;
        car1Pos = 0;
        car2Pos = 0;
        Started = false;
        Finished = false;
        screenWidth = new DisplayMetrics().widthPixels;
        finishWidth = findViewById(R.id.finish).getWidth();
        Car1 = (View)findViewById(R.id.green_player);
        Car2 = (View)findViewById(R.id.red_player);
        carLength = Car1.getWidth();
        startMargin = ((ViewGroup.MarginLayoutParams)Car1.getLayoutParams()).leftMargin;
    }

    public void Start(View view){
        Button button = (Button)findViewById(R.id.btn_start);
        if(!Finished){
            if(!Started){
                button.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                button.setText("Пауза");
                Started = true;
                length = 100;
                car1Pos = 0;
                car2Pos = 0;
                screenWidth = new DisplayMetrics().widthPixels;
                finishWidth = findViewById(R.id.finish).getWidth();
                Car1 = (View)findViewById(R.id.green_player);
                Car2 = (View)findViewById(R.id.red_player);
                carLength = Car1.getWidth();
                startMargin = ((ViewGroup.MarginLayoutParams)Car1.getLayoutParams()).leftMargin;
            }
            else{
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
        Button button = (Button)findViewById(R.id.btn_start);
        TextView result = (TextView)findViewById(R.id.lb_result);
        if (Started && !Finished){
            car1Pos += 1;
            UpdateCarPoses();
            if (car1Pos >= length){
                result.setText("Победа первого игрока");
                button.setText("Заново");
                result.setTextColor(0xffe91e63);
                Finished = true;
            }
        }
    }
    public void Drive2(View view){
        Button button = (Button)findViewById(R.id.btn_start);
        TextView result = (TextView)findViewById(R.id.lb_result);
        if (Started && !Finished){
            car2Pos += 1;
            UpdateCarPoses();
            if (car2Pos >= length){
                result.setText("Победа второго игрока");
                button.setText("Заново");
                result.setTextColor(0xfff00000);
                Finished = true;
            }
        }
    }
    public void UpdateCarPoses(){
        View car1 = this.Car1;
        View car2 = this.Car2;
        ViewGroup.MarginLayoutParams margin1 = (ViewGroup.MarginLayoutParams)car1.getLayoutParams();
        ViewGroup.MarginLayoutParams margin2 = (ViewGroup.MarginLayoutParams)car2.getLayoutParams();
        float distance = screenWidth - carLength - finishWidth - startMargin;
        margin1.leftMargin = (int)(startMargin + distance * car1Pos / length);
        margin2.leftMargin = (int)(startMargin + distance * car2Pos / length);
        car1.requestLayout();
        car2.requestLayout();
    }
}