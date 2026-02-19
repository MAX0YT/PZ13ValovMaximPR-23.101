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
import android.content.SharedPreferences;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    public float travelDistance;
    public float length;
    public float car1Pos;
    public float car2Pos;
    public boolean Started;
    public boolean Finished;
    public boolean Stopped;
    public View Car1;
    public View Car2;
    public int carLength;
    public int startMargin;
    private Spinner spinnerLength;
    private int selectedLength = 100;
    private Button btnGreen;
    private Button btnRed;
    private TextView lengthLabel;
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
        spinnerLength = findViewById(R.id.spinner_length);

        String[] lengthOptions = {"50", "75", "100", "125", "150", "200", "250"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                lengthOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLength.setAdapter(adapter);

        // Загружаем сохранённую длину
        SharedPreferences prefs = getSharedPreferences("race_prefs", MODE_PRIVATE);
        selectedLength = prefs.getInt("selected_length", 100);

        // Устанавливаем выбранный пункт
        int pos = adapter.getPosition(String.valueOf(selectedLength));
        spinnerLength.setSelection(pos != -1 ? pos : 2); // 2 = "100"

        // Слушатель выбора
        spinnerLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLength = Integer.parseInt(parent.getItemAtPosition(position).toString());
                prefs.edit().putInt("selected_length", selectedLength).apply();

                if (!Started) {
                    length = selectedLength;   // сразу применяем, если игра ещё не начата
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Устанавливаем начальную длину
        length = selectedLength;
        // ============================

        btnGreen = findViewById(R.id.btn_green);
        btnRed = findViewById(R.id.btn_red);
        lengthLabel = findViewById(R.id.tv_length_label);
        btnGreen.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        lengthLabel.setVisibility(View.VISIBLE);

        car1Pos = 0;
        car2Pos = 0;
        Started = false;
        Finished = false;
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
                Stopped = false;
                Started = true;
                btnGreen.setVisibility(View.VISIBLE);
                btnRed.setVisibility(View.VISIBLE);
                spinnerLength.setVisibility(View.GONE);
                lengthLabel.setVisibility(View.GONE);
                length = selectedLength;
                car1Pos = 0;
                car2Pos = 0;
                Car1 = (View)findViewById(R.id.green_player);
                Car2 = (View)findViewById(R.id.red_player);
                carLength = Car1.getWidth();
                startMargin = ((ViewGroup.MarginLayoutParams)Car1.getLayoutParams()).leftMargin;
                int initialCarLeft = Car1.getLeft();
                int finishLeft = findViewById(R.id.finish).getLeft();
                travelDistance = finishLeft - initialCarLeft - carLength;
            }
            else if (Stopped)
            {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                button.setText("Пауза");
                Stopped = false;
            }
            else
            {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                button.setText("Старт");
                Stopped = true;
            }
        }
        else{
            resetGame();
        }
    }
    public void Drive1(View view){
        Button button = (Button)findViewById(R.id.btn_start);
        TextView result = (TextView)findViewById(R.id.lb_result);
        if (Started && !Finished && !Stopped){
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
        if (Started && !Finished && !Stopped){
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
        float distance = travelDistance;
        margin1.leftMargin = (int)(startMargin + distance * car1Pos / length);
        margin2.leftMargin = (int)(startMargin + distance * car2Pos / length);
        car1.requestLayout();
        car2.requestLayout();
    }
    private void resetGame() {
        Started = false;
        Finished = false;
        Stopped = false;
        car1Pos = 0;
        car2Pos = 0;
        length = selectedLength;

        ViewGroup.MarginLayoutParams margin1 = (ViewGroup.MarginLayoutParams) Car1.getLayoutParams();
        ViewGroup.MarginLayoutParams margin2 = (ViewGroup.MarginLayoutParams) Car2.getLayoutParams();
        margin1.leftMargin = startMargin;
        margin2.leftMargin = startMargin;
        Car1.requestLayout();
        Car2.requestLayout();

        // Сбрасываем кнопку "Старт"
        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        btnStart.setText("Старт");

        // Очищаем результат
        TextView result = findViewById(R.id.lb_result);
        result.setText("");
        result.setTextColor(Color.WHITE);

        // Возвращаем видимость как в начале игры
        btnGreen.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        spinnerLength.setVisibility(View.VISIBLE);
        lengthLabel.setVisibility(View.VISIBLE);
    }
}