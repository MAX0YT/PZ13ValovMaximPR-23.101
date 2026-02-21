package com.example.pz13valovmaximpr_23101mobil;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public float travelDistance;
    public float length;
    public float car1Pos;
    public float car2Pos;
    public boolean Started;
    public boolean Finished;
    public boolean Stopped;
    public ImageView Car1;
    public ImageView Car2;
    public int carLength;
    public int startMargin;

    private boolean isAI;
    private Spinner spinnerDifficulty;
    private int botInterval = 700;
    private int currentBotDifficulty = 1;
    private Handler botHandler;
    private Runnable botRunnable;
    private TextView difficultLabel;

    // === СКИНЫ (отдельные для каждой машины) ===
    private final List<Skin> allSkins = new ArrayList<>();
    private Set<String> boughtSkins = new HashSet<>();
    private String selectedSkinIdGreen = "blue1";   // зелёная машина (игрок 1)
    private String selectedSkinIdRed = "blue1";     // красная машина (игрок 2 / бот)
    private int coins = 0;
    private Dialog currentShopDialog;
    private int editingCar = 0; // 1 = green, 2 = red

    private Spinner spinnerLength;
    private int selectedLength = 100;
    private TextView lengthLabel;

    private Button btnGreen;
    private Button btnRed;

    private int fixedCarWidth = 0;
    private int fixedCarHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        } catch (NullPointerException ignored) {}
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_exit).setOnClickListener(v -> onBackPressed());
        (findViewById(R.id.btn_start)).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isAI = getIntent().getBooleanExtra("isAI", false);

        Car1 = findViewById(R.id.green_player);
        Car2 = findViewById(R.id.red_player);
        btnGreen = findViewById(R.id.btn_green);
        btnRed = findViewById(R.id.btn_red);
        spinnerLength = findViewById(R.id.spinner_length);
        lengthLabel = findViewById(R.id.tv_length_label);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        difficultLabel = findViewById(R.id.tv_difficult_label);
        botHandler = new Handler(Looper.getMainLooper());

        loadProgress();
        initSkins();
        applySkinsToCars();
        measureAndFixCarSize();

        initLengthSpinner();

        if (isAI) {
            initDifficultySpinner();
            spinnerDifficulty.setVisibility(View.VISIBLE);
            difficultLabel.setVisibility(View.VISIBLE);
            btnRed.setVisibility(View.GONE);
        } else {
            spinnerDifficulty.setVisibility(View.GONE);
            difficultLabel.setVisibility(View.GONE);
        }

        btnGreen.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        spinnerLength.setVisibility(View.VISIBLE);
        lengthLabel.setVisibility(View.VISIBLE);

        // Клик по машинам → магазин для конкретной машины
        Car1.setOnClickListener(v -> {
            if (!Started) {               // только когда игра ещё не началась
                editingCar = 1;
                showSkinShop();
            }
        });

        Car2.setOnClickListener(v -> {
            if (!Started) {
                editingCar = 2;
                showSkinShop();
            }
        });

        carLength = Car1.getWidth();
        startMargin = ((ViewGroup.MarginLayoutParams) Car1.getLayoutParams()).leftMargin;
    }
    private void measureAndFixCarSize() {
        Car1.post(() -> {
            if (fixedCarWidth == 0 && Car1.getMeasuredWidth() > 0) {
                fixedCarWidth = Car1.getMeasuredWidth();
                fixedCarHeight = Car1.getMeasuredHeight();
            }
            applyFixedSizeToBothCars();
        });
    }
    private void initSkins() {
        allSkins.clear();
        allSkins.add(new Skin("blue1", "Развалюха", R.drawable.blue1, 100, 0.5f));
        allSkins.add(new Skin("yellow1", "Машинка", R.drawable.yellow1, 300, 1.0f));
        allSkins.add(new Skin("car1", "Зелёный луг", R.drawable.car1, 1000, 1.3f));
        allSkins.add(new Skin("yellow2", "Яркое солнце", R.drawable.yellow2, 2500, 1.6f));
        allSkins.add(new Skin("orange1", "Скоростной огонёк", R.drawable.orange1, 3700, 1.9f));
        allSkins.add(new Skin("car2", "Красная ярость", R.drawable.car2, 5000, 2.3f));
        allSkins.add(new Skin("purple1", "Плазма", R.drawable.purple1, 10000, 2.7f));
        allSkins.add(new Skin("white1", "Вспышка", R.drawable.white1, 20000, 4f));
        allSkins.add(new Skin("blue2", "Ракета", R.drawable.blue2, 50000, 6f));
    }

    private void loadProgress() {
        SharedPreferences prefs = getSharedPreferences("race_prefs", MODE_PRIVATE);
        coins = prefs.getInt("coins", 0);
        boughtSkins = new HashSet<>(prefs.getStringSet("bought_skins", new HashSet<>()));
        selectedSkinIdGreen = prefs.getString("selected_skin_green", "blue1");
        selectedSkinIdRed = prefs.getString("selected_skin_red", "blue1");

        if (!boughtSkins.contains("blue1")) {
            boughtSkins.add("blue1");
            selectedSkinIdGreen = "blue1";
            selectedSkinIdRed = "blue1";
        }
    }

    private void saveProgress() {
        SharedPreferences prefs = getSharedPreferences("race_prefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("coins", coins)
                .putStringSet("bought_skins", boughtSkins)
                .putString("selected_skin_green", selectedSkinIdGreen)
                .putString("selected_skin_red", selectedSkinIdRed)
                .apply();
    }

    private void applySkinsToCars() {
        Skin skinGreen = findSkinById(selectedSkinIdGreen);
        Skin skinRed = findSkinById(selectedSkinIdRed);

        Car1.setImageResource(skinGreen.getDrawableRes());
        Car2.setImageResource(skinRed.getDrawableRes());

        measureAndFixCarSize();  // ← сразу выравниваем размер
    }

    private Skin findSkinById(String id) {
        for (Skin s : allSkins) if (s.getId().equals(id)) return s;
        return allSkins.get(0);
    }

    private void applyFixedSizeToBothCars() {
        if (fixedCarWidth == 0) return;

        ViewGroup.LayoutParams lp1 = Car1.getLayoutParams();
        lp1.width = fixedCarWidth;
        lp1.height = fixedCarHeight;
        Car1.setLayoutParams(lp1);

        ViewGroup.LayoutParams lp2 = Car2.getLayoutParams();
        lp2.width = fixedCarWidth;
        lp2.height = fixedCarHeight;
        Car2.setLayoutParams(lp2);

        Car1.requestLayout();
        Car2.requestLayout();
        carLength = fixedCarWidth;
    }
    private void showSkinShop() {
        currentShopDialog = new Dialog(this);
        currentShopDialog.setContentView(R.layout.activity_dialog_skin_shop);

        TextView tvCoins = currentShopDialog.findViewById(R.id.tv_coins);
        tvCoins.setText("Монеты: " + coins);

        ListView listView = currentShopDialog.findViewById(R.id.list_skins);
        SkinAdapter adapter = new SkinAdapter(getSortedSkins(editingCar), editingCar);
        listView.setAdapter(adapter);

        currentShopDialog.show();

        // Делаем диалог почти на всю ширину экрана
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Window window = currentShopDialog.getWindow();
        if (window != null) {
            window.setLayout((int) (dm.widthPixels * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private List<Skin> getSortedSkins(int editingCar) {
        String currentSelected = (editingCar == 1) ? selectedSkinIdGreen : selectedSkinIdRed;
        List<Skin> sorted = new ArrayList<>(allSkins);
        sorted.sort((a, b) -> {
            int statusA = getSkinStatus(a, currentSelected);
            int statusB = getSkinStatus(b, currentSelected);
            if (statusA != statusB) return Integer.compare(statusB, statusA);

            if (statusA == 1) return Integer.compare(b.getCost(), a.getCost()); // купленные: дорогие → дешёвые
            else return Integer.compare(a.getCost(), b.getCost());             // некупленные: дешёвые → дорогие
        });
        return sorted;
    }

    private int getSkinStatus(Skin s, String currentSelected) {
        if (s.getId().equals(currentSelected)) return 2;
        if (boughtSkins.contains(s.getId())) return 1;
        return 0;
    }

    public void Start(View view){
        Button button = findViewById(R.id.btn_start);
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
        if (!(Started && !Finished && !Stopped)) return;

        Skin skin = findSkinById(selectedSkinIdGreen);
        car1Pos += skin.getSpeedMultiplier();
        UpdateCarPoses();

        if (car1Pos >= length) {
            TextView result = findViewById(R.id.lb_result);
            Button button = findViewById(R.id.btn_start);
            result.setText(isAI ? "Победа игрока!" : "Победа первого игрока");
            result.setTextColor(0xffe91e63);
            button.setText("Заново");
            Finished = true;

            if (isAI) giveReward();
            if (botHandler != null) botHandler.removeCallbacks(botRunnable);
        }
    }

    private void giveReward() {
        Skin playerSkin = findSkinById(selectedSkinIdGreen);
        Skin botSkin = findSkinById(selectedSkinIdRed);
        int reward = (int)((Math.pow((double)botSkin.getCost(), 2) / 10 / (double)playerSkin.getCost()) * (Math.pow(1.6, length / 50 - 1)) * (1 + (Math.pow(10, currentBotDifficulty) - 1)/10));
        coins += reward;
        saveProgress();
        Toast.makeText(this, "Победа над ботом! +" + reward + " монет", Toast.LENGTH_LONG).show();
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

        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
        btnStart.setText("Старт");

        TextView result = findViewById(R.id.lb_result);
        result.setText("");
        result.setTextColor(Color.WHITE);

        btnGreen.setVisibility(View.GONE);
        btnRed.setVisibility(View.GONE);
        spinnerLength.setVisibility(View.VISIBLE);
        lengthLabel.setVisibility(View.VISIBLE);
        if (isAI) {
            spinnerDifficulty.setVisibility(View.VISIBLE);
            difficultLabel.setVisibility(View.VISIBLE);
        }

        applySkinsToCars();
        applyFixedSizeToBothCars();
        if (botHandler != null) botHandler.removeCallbacks(botRunnable);
    }

    private void initLengthSpinner(){
        String[] lengthOptions = {"50", "75", "100", "125", "150", "200", "250"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                lengthOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLength.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("length_prefs", MODE_PRIVATE);
        selectedLength = prefs.getInt("selected_length", 100);

        int pos = adapter.getPosition(String.valueOf(selectedLength));
        spinnerLength.setSelection(pos != -1 ? pos : 2);

        spinnerLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLength = Integer.parseInt(parent.getItemAtPosition(position).toString());
                prefs.edit().putInt("selected_length", selectedLength).apply();

                if (!Started) {
                    length = selectedLength;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        length = selectedLength;
    }

    private void initDifficultySpinner() {
        String[] difficulties = {"Лёгкий", "Средний", "Сложный"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("difficult_prefs", MODE_PRIVATE);
        currentBotDifficulty = prefs.getInt("selected_difficult", 0);

        String diff = "";
        switch(currentBotDifficulty){
            case 0: diff="Лёгкий"; break;
            case 1: diff="Средний"; break;
            case 2: diff="Сложный"; break;
        }
        int pos = adapter.getPosition(diff);
        spinnerDifficulty.setSelection(pos != -1 ? pos : 1);

        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                switch (selected){
                    case "Лёгкий": currentBotDifficulty=0; break;
                    case "Средний": currentBotDifficulty=1; break;
                    case "Сложный": currentBotDifficulty=2; break;
                }
                if (!Started) {
                    switch (position) {
                        case 0: botInterval = 570; break;
                        case 1: botInterval = 230;  break;
                        case 2: botInterval = 130;  break;
                    }
                }
                prefs.edit().putInt("selected_difficult", currentBotDifficulty).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void moveCar2(boolean isBotMove) {
        if (!(Started && !Finished && !Stopped)) return;

        Skin skin = findSkinById(selectedSkinIdRed);
        car2Pos += skin.getSpeedMultiplier();
        UpdateCarPoses();

        if (car2Pos >= length) {
            TextView result = findViewById(R.id.lb_result);
            Button button = findViewById(R.id.btn_start);
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
        botRunnable = () -> moveCar2(true);
        botHandler.postDelayed(botRunnable, botInterval);
    }

    private class SkinAdapter extends ArrayAdapter<Skin> {
        private final int editingCar;

        SkinAdapter(List<Skin> skins, int editingCar) {
            super(MainActivity.this, 0, skins);
            this.editingCar = editingCar;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_item_skin, parent, false);
            }

            Skin skin = getItem(position);
            ImageView iv = convertView.findViewById(R.id.iv_skin);
            TextView tvName = convertView.findViewById(R.id.tv_name);
            TextView tvSpeed = convertView.findViewById(R.id.tv_speed);
            TextView tvCost = convertView.findViewById(R.id.tv_cost);
            Button btn = convertView.findViewById(R.id.btn_action);

            iv.setImageResource(skin.getDrawableRes());
            tvName.setText(skin.getName());
            tvSpeed.setText(String.format("Скорость: %.1fx", skin.getSpeedMultiplier()));
            tvCost.setText(skin.getCost() == 0 ? "Бесплатно" : skin.getCost() + " монет");

            String currentSelected = (editingCar == 1) ? selectedSkinIdGreen : selectedSkinIdRed;
            int status = getSkinStatus(skin, currentSelected);

            if (status == 2) {
                btn.setText("Выбрано");
                btn.setEnabled(false);
            } else if (status == 1) {
                btn.setText("Выбрать");
                btn.setEnabled(true);
            } else {
                btn.setText("Купить за " + skin.getCost());
                btn.setEnabled(true);
            }

            btn.setOnClickListener(v -> {
                if (status == 2) return;

                if (status == 1) {
                    if (editingCar == 1) selectedSkinIdGreen = skin.getId();
                    else selectedSkinIdRed = skin.getId();
                } else {
                    if (coins >= skin.getCost()) {
                        coins -= skin.getCost();
                        boughtSkins.add(skin.getId());
                        if (editingCar == 1) selectedSkinIdGreen = skin.getId();
                        else selectedSkinIdRed = skin.getId();
                        saveProgress();
                        Toast.makeText(MainActivity.this, "Скин куплен!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Недостаточно монет!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                saveProgress();
                applySkinsToCars();        // ← обновляем картинки
                applyFixedSizeToBothCars();        // ← ИСПРАВЛЕНИЕ №2: сразу выравниваем размер

                if (currentShopDialog != null) currentShopDialog.dismiss();
                showSkinShop(); // обновляем список
            });

            return convertView;
        }
    }
}