package com.andyslab.android.chessclock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class Clock extends AppCompatActivity {
    private static Clock instance;
    ClockLogic clockLogic;
    RelativeLayout player1Layout;
    RelativeLayout player2Layout;
    AppCompatTextView player1Time;
    AppCompatTextView player2Time;
    AppCompatTextView player1MoveCount;
    AppCompatTextView player2MoveCount;

    AppCompatImageButton resetButton;
    AppCompatImageButton playPauseButton;
    AppCompatImageButton settingsButton;

    static long[] timeControls = new long[2];
    long[] timeControlsInit = new long[2];

    public Clock(){
        super();
        instance = this;
    }

    public static Clock getClock() {
        if (instance == null) {
            instance = new Clock();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        Intent intent = getIntent();
        loadTimeSettings();

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        player1Time = findViewById(R.id.player1Time);
        player2Time = findViewById(R.id.player2Time);

        player1MoveCount = findViewById(R.id.player1MoveCount);
        player2MoveCount = findViewById(R.id.player2MoveCount);

        resetButton = findViewById(R.id.resetButton);

        playPauseButton = findViewById(R.id.playPauseButton);

        settingsButton = findViewById(R.id.settingsButton);

        clockLogic = new ClockLogic(this, player1Layout, player2Layout, player1Time, player2Time, playPauseButton);

        if(intent.hasExtra("TIME_CONTROLS")){
            timeControls = intent.getExtras().getLongArray("TIME_CONTROLS");
        }

        //set chosen time controls
        if(timeControls!=null){
            clockLogic.setStartTime(timeControls[0]);
            clockLogic.setIncrement(timeControls[1]);
        }

        timeControlsInit = timeControls;
        saveTimeSettings();
        clockLogic.initializeClocks();


        player1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player1LayoutClick();
            }
        });

        player2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player2LayoutClick();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtonClick();
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAndPauseButtonClick();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButtonClick();
            }
        });

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent goHome = new Intent(Intent.ACTION_MAIN);
        goHome.addCategory(Intent.CATEGORY_HOME);
        goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goHome);
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockLogic.pauseTimer();
    }

    public void player1LayoutClick(){clockLogic.changeTurn(player1Layout, player2Layout);}

    public void player2LayoutClick(){clockLogic.changeTurn(player2Layout, player1Layout);}

    public void resetButtonClick(){
        clockLogic.initializeClocks();
    }

    public void playAndPauseButtonClick(){
        clockLogic.playOrPauseTimer();
    }

    public void settingsButtonClick(){
        Intent intent = new Intent(this, Settings.class);
        intent.putExtra("CHECKED_BUTTON", timeControls);
        clockLogic.pauseTimer();
        startActivity(intent);
    }

    private void saveTimeSettings(){
        SharedPreferences prefs = Clock.getClock().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TIME_SETTINGS", Arrays.toString(timeControls));
        editor.clear();
        editor.apply();
    }

    private void loadTimeSettings(){
        SharedPreferences prefs = Clock.getClock().getPreferences(Context.MODE_PRIVATE);
        String str = prefs.getString("TIME_SETTINGS", "[600000, 0]");
        String[] stringArr = str.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");


        try{
        for(int i = 0; i<stringArr.length; i++){
            timeControls[i] = Long.parseLong(stringArr[i]);
        }
        }catch(NumberFormatException e){
            timeControls[0] = 600000;
            timeControls[1] = 0;
        }
    }

}















