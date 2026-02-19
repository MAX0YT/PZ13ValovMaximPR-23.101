package com.example.pz13valovmaximpr_23101mobil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), findViewById(android.R.id.content));
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        findViewById(R.id.btn_exit).setOnClickListener(v -> finishAffinity());

        // Удалить сохранения
        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            SharedPreferences prefs1 = getSharedPreferences("length_prefs", MODE_PRIVATE);
            prefs1.edit().clear().apply();
            SharedPreferences prefs2 = getSharedPreferences("difficult_prefs", MODE_PRIVATE);
            prefs2.edit().clear().apply();
            Toast.makeText(this, "Все сохранения удалены!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.lo_ai).setOnClickListener(v -> startGame(true));
        findViewById(R.id.lo_pvp).setOnClickListener(v -> startGame(false));
    }
    private void startGame(boolean isAI) {
        Intent intent = new Intent(MainMenu.this, MainActivity.class);
        intent.putExtra("isAI", isAI);
        startActivity(intent);
        // finish(); // раскомментируй, если хочешь, чтобы меню закрывалось при старте игры
    }
}