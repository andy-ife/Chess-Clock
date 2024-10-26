package com.andyslab.android.chessclock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class Settings extends AppCompatActivity {

    RadioGroup radioGroup;
    AppCompatButton customTimeButton;
    AppCompatButton startGameButton;
    RadioButton selected;
    long[] timeControls = new long[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        radioGroup = findViewById(R.id.radioGroup);
        customTimeButton = findViewById(R.id.customTimeButton);
        startGameButton = findViewById(R.id.startGameButton);

        loadPrevCheckedButton();

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameButtonClick();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selected = findViewById(checkedId);
                String content = selected.getContentDescription().toString();

                // get time controls chosen by player, store them in time controls array
                String[] arr = content.split("\\+");

                for(int i=0; i<arr.length; i++){
                    timeControls[i] = Long.parseLong(arr[i]);
                }
            }
        });

    }

    public void startGameButtonClick(){
        Intent intent = new Intent(getApplicationContext(), Clock.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("TIME_CONTROLS", timeControls);
        finishAfterTransition();
        startActivity(intent);
    }

    //selects the right radio button based on the user's past time settings
    private void loadPrevCheckedButton(){
        ArrayList<AppCompatRadioButton> radioButtons = new ArrayList<AppCompatRadioButton>();

        //get saved time settings
        String str = Arrays.toString(getIntent().getExtras().getLongArray("CHECKED_BUTTON"));

        //make the string match the content description format for the radio buttons in xml(e.g "300000+3000(5+3)", "10+0")
        String contentDescMatcher = str.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").replaceAll("\\,","+");

        for(int i = 0; i<radioGroup.getChildCount(); i++){
            View v = radioGroup.getChildAt(i);
            if(v instanceof AppCompatRadioButton){
            radioButtons.add((AppCompatRadioButton) v);
            }
        }

        for(AppCompatRadioButton r: radioButtons){
            if(r.getContentDescription().toString().equals(contentDescMatcher)){
                r.setChecked(true);
                //also set the correct time controls
                String[] arr = r.getContentDescription().toString().split("\\+");
                for(int i = 0; i<arr.length; i++){
                    timeControls[i] = Long.parseLong(arr[i]);
                }
            }
        }
    }

}