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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.os.Handler;
import android.os.Looper;

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
    private boolean isAI;
    private Spinner spinnerDifficulty;
    private String selectedDifficult;
    private TextView difficultLabel;
    private int botInterval = 700;        // средний по умолчанию
    private Handler botHandler;
    private Runnable botRunnable;
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

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), findViewById(android.R.id.content));
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        findViewById(R.id.btn_exit).setOnClickListener(v -> onBackPressed());
        ((Button)findViewById(R.id.btn_start)).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLengthSpinner();

        btnGreen = findViewById(R.id.btn_green);
        btnRed = findViewById(R.id.btn_red);
        lengthLabel = findViewById(R.id.tv_length_label);
        btnGreen.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        lengthLabel.setVisibility(View.VISIBLE);

        isAI = getIntent().getBooleanExtra("isAI", false);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        difficultLabel = findViewById(R.id.tv_difficult_label);
        botHandler = new Handler(Looper.getMainLooper());
        if (isAI) {
            initDifficultySpinner();
            spinnerDifficulty.setVisibility(View.VISIBLE);
            btnRed.setVisibility(View.GONE);           // никогда не показываем
            switch (selectedDifficult) {
                case "Лёгкий": botInterval = 1200; break; // Лёгкий
                case "Средний": botInterval = 700;  break; // Средний
                case "Сложный": botInterval = 350;  break; // Сложный
            }
        } else {
            spinnerDifficulty.setVisibility(View.GONE);
            difficultLabel.setVisibility(View.GONE);
        }

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
                if (!isAI) btnRed.setVisibility(View.VISIBLE);
                spinnerLength.setVisibility(View.GONE);
                lengthLabel.setVisibility(View.GONE);
                if (isAI) {
                    spinnerDifficulty.setVisibility(View.GONE);
                    difficultLabel.setVisibility(View.GONE);
                    startBot();
                }
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
                if (isAI) startBot();
                Stopped = false;
            }
            else
            {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                button.setText("Старт");
                if (botHandler != null) botHandler.removeCallbacks(botRunnable);
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
        moveCar2(false);
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
        if (isAI) {
            spinnerDifficulty.setVisibility(View.VISIBLE);
            difficultLabel.setVisibility(View.VISIBLE);
        }
        if (botHandler != null) botHandler.removeCallbacks(botRunnable);
    }
    private void initLengthSpinner(){
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
        SharedPreferences prefs = getSharedPreferences("length_prefs", MODE_PRIVATE);
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
    }
    private void initDifficultySpinner() {
        String[] difficulties = {"Лёгкий", "Средний", "Сложный"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("difficult_prefs", MODE_PRIVATE);
        selectedDifficult = prefs.getString("selected_difficult", "Средний");

        // Устанавливаем выбранный пункт
        int pos = adapter.getPosition(selectedDifficult);
        spinnerDifficulty.setSelection(pos != -1 ? pos : 1);

        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficult = parent.getItemAtPosition(position).toString();
                if (!Started) {
                    switch (position) {
                        case 0: botInterval = 1200; break; // Лёгкий
                        case 1: botInterval = 700;  break; // Средний
                        case 2: botInterval = 350;  break; // Сложный
                    }
                }
                prefs.edit().putString("selected_difficult", selectedDifficult).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void moveCar2(boolean isBotMove) {
        if (!(Started && !Finished && !Stopped)) return;

        car2Pos += 1;
        UpdateCarPoses();

        if (car2Pos >= length) {
            TextView result = (TextView) findViewById(R.id.lb_result);
            Button button = (Button) findViewById(R.id.btn_start);
            result.setText(isAI ? "Победа бота!" : "Победа второго игрока");
            button.setText("Заново");
            result.setTextColor(0xfff00000);
            Finished = true;
            if (botHandler != null) botHandler.removeCallbacks(botRunnable);
            return;
        }

        if (isBotMove) {
            botHandler.postDelayed(botRunnable, botInterval);
        }
    }
    private void startBot() {
        botRunnable = new Runnable() {
            @Override
            public void run() {
                moveCar2(true);
            }
        };
        botHandler.postDelayed(botRunnable, botInterval); // первый ход бота через интервал
    }
}