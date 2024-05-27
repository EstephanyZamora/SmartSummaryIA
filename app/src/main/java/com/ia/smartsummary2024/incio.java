package com.ia.smartsummary2024;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class incio extends AppCompatActivity {
    private Handler handler = new Handler();
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long tiempoEspera = 3000;

        runnable = new Runnable() {
            @Override
            public void run() {
                // Cambia a la siguiente actividad aquí
                Intent intent = new Intent(incio.this, MainActivity.class);
                startActivity(intent);
                finish(); // Opcional: finaliza esta actividad para que el usuario no pueda volver atrás
            }
    };
        handler.postDelayed(runnable, tiempoEspera);
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detiene el Runnable si la actividad se destruye antes del tiempo de espera
        handler.removeCallbacks(runnable);
    }
}