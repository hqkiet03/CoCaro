package com.example.cocaro;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class InitialActivity extends AppCompatActivity {
    MediaPlayer mp, mp1;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        mp = MediaPlayer.create(this, R.raw.nen);
        mp.start();

        mp1 = MediaPlayer.create(this, R.raw.sound);
        if (mp1.isPlaying()) {
            mp1.stop();
        }
        Button btnNewGame = findViewById(R.id.btnNewGame);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnExit = findViewById(R.id.btnBack);

        dialog = new Dialog(InitialActivity.this);
        dialog.setContentView(R.layout.dialog_question);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialogbox));
        dialog.setCancelable(false);
        btnExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.show();
            }
        });
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnEXIT = dialog.findViewById(R.id.btnDialogExit);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnEXIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mp.stop();
                mp1.stop();
                finish();
            }
        });

        btnNewGame.setOnClickListener(v -> {
            mp.stop();
            mp1.setOnCompletionListener(mp -> mp.start());
            mp1.start();
            Intent intent = new Intent(InitialActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btnHistory.setOnClickListener(v -> {
            Intent intent_history = new Intent(InitialActivity.this, HistoryActivity.class);
            startActivity(intent_history);

        });


    }
}
