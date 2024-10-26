package com.andyslab.android.chessclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

//class containing logic for basic chess clock functions - play, pause, change player turn etc
public class ClockLogic {
    Context context;

    public RelativeLayout player1Layout;
    public RelativeLayout player2Layout;
    public RelativeLayout currentRunningClock;
    public RelativeLayout lastPressedClock;

    AppCompatTextView player1Time;
    AppCompatTextView player2Time;

    AppCompatImageButton playPauseButton;

    boolean gameOver = false;

    static int moveCount;
    static int niftyCounter;
    static int tapCounter;

    public static long startTime = 600000;//millisecs
    public static long increment;

    private static long player1TimeLeft;
    private static long player2TimeLeft;

    public static CountDownTimer timer;

    //quick getters and setters for the time controls
    public long getStartTime(){
        return startTime;
    }
    public void setStartTime(long t){
        this.startTime = t;
    }
    public long getIncrement(){
        return increment;
    }
    public void setIncrement(long t){
        this.increment = t;
    }

    public ClockLogic(Context context, RelativeLayout player1Layout, RelativeLayout player2Layout, AppCompatTextView player1Time, AppCompatTextView player2Time, AppCompatImageButton playPauseButton){
        this.context = context;
        this.player1Layout = player1Layout;
        this.player2Layout = player2Layout;
        this.player1Time = player1Time;
        this.player2Time = player2Time;
        this.playPauseButton = playPauseButton;

        player1TimeLeft = startTime;
        player2TimeLeft = startTime;

        lastPressedClock = player1Layout;
        currentRunningClock = player2Layout;

    }

    public void initializeClocks(){
        if(timer!=null) timer.cancel();

        gameOver = false;

        NumberFormat f = new DecimalFormat("0");
        NumberFormat g = new DecimalFormat("00");

        long min = (startTime / 60000) % 60;
        long sec = (startTime / 1000) % 60;

        player1Time.setText(f.format(min) + ":" + g.format(sec));
        player2Time.setText(f.format(min) + ":" + g.format(sec));

        player1Time.setTextColor(Color.parseColor("#202020"));
        player2Time.setTextColor(Color.parseColor("#202020"));

        player1Layout.setClickable(true);
        player2Layout.setClickable(true);

        player1Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);
        player2Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);

        niftyCounter = 0;
        moveCount = 0;
        tapCounter = 0;

        playPauseButton.setImageResource(R.drawable.play_icon);

        player1TimeLeft = startTime;
        player2TimeLeft = startTime;
        //also update the move counts
        updatePlayerMoveCounts((AppCompatTextView) player1Layout.getChildAt(1));
        updatePlayerMoveCounts((AppCompatTextView) player2Layout.getChildAt(1));
    }

    //wrapper function that does a lot of stuff
    public void changeTurn(RelativeLayout current, RelativeLayout next){
        AppCompatTextView currentTimer = (AppCompatTextView) current.getChildAt(0);
        AppCompatTextView nextTimer = (AppCompatTextView) next.getChildAt(0);
        AppCompatTextView currentMoveCount = (AppCompatTextView) current.getChildAt(1);

        updatePlayerTimers(currentTimer, nextTimer);
        updatePlayerMoveCounts(currentMoveCount);
        updatePlayerButtons(current, next);
    }

    //split this function into separate functions for play and pause
    public void playOrPauseTimer(){
        if(!gameOver) {
            //play clocks
            if (Objects.equals(player1Layout.getBackground().getConstantState(), player2Layout.getBackground().getConstantState())) {
                AppCompatTextView t1 = (AppCompatTextView) lastPressedClock.getChildAt(0);
                AppCompatTextView t2 = (AppCompatTextView) currentRunningClock.getChildAt(0);

                updatePlayerTimers(t1, t2);
                updatePlayerButtons(lastPressedClock, currentRunningClock);
            }
            //pause clocks
            else {
                if (timer != null) timer.cancel();

                player1Time.setTextColor(Color.parseColor("#202020"));
                player2Time.setTextColor(Color.parseColor("#202020"));

                player1Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);
                player2Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);

                playPauseButton.setImageResource(R.drawable.play_icon);
            }
        }

    }

    //lazy pause function. Will split play and pause into separate functions.
    public void pauseTimer(){
        //if game is over no need to pause
        if(!gameOver){
            if(timer!=null) timer.cancel();

            player1Time.setTextColor(Color.parseColor("#202020"));
            player2Time.setTextColor(Color.parseColor("#202020"));

            player1Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);
            player2Layout.setBackgroundResource(R.drawable.greyed_out_button_padding);

            playPauseButton.setImageResource(R.drawable.play_icon);
        }
    }

    private void updatePlayerTimers(AppCompatTextView current, AppCompatTextView next) {
        //to format minutes and seconds as one and two digits respectively
        NumberFormat f = new DecimalFormat("0");
        NumberFormat g = new DecimalFormat("00");

        RelativeLayout parentCurrent = (RelativeLayout) current.getParent();
        RelativeLayout parentNext = (RelativeLayout) next.getParent();

        //check if game is just starting, game is paused or it's current player's turn
        if ((Objects.equals(parentCurrent.getBackground().getConstantState(), parentNext.getBackground().getConstantState())) || ((tapCounter > 0) && Objects.equals(parentCurrent.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.blue_button_padding).getConstantState()))){
            long timeLeft;
            //check if game is just starting
            if ((tapCounter == 0) || lastPressedClock == parentCurrent) {
                timeLeft = (current.getId() == R.id.player1Time) ? player2TimeLeft : player1TimeLeft;
            }//no time increment after first tap
            else if(tapCounter == 1){
                timeLeft = (current.getId() == R.id.player1Time) ? player2TimeLeft : player1TimeLeft;
                long m = (current.getId() == R.id.player1Time) ? ((player1TimeLeft + increment) / 60000) % 60 : ((player2TimeLeft + increment) / 60000) % 60;
                long s = (current.getId() == R.id.player1Time) ? ((player1TimeLeft + increment) / 1000) % 60 : ((player2TimeLeft + increment) / 1000) % 60;
                current.setText(f.format(m) + ":" + g.format(s));
            }//there is time increment on all subsequent taps
            else {
                timeLeft = (current.getId() == R.id.player1Time) ? player2TimeLeft + increment : player1TimeLeft + increment;
                long m = (current.getId() == R.id.player1Time) ? ((player1TimeLeft + increment) / 60000) % 60 : ((player2TimeLeft + increment) / 60000) % 60;
                long s = (current.getId() == R.id.player1Time) ? ((player1TimeLeft + increment) / 1000) % 60 : ((player2TimeLeft + increment) / 1000) % 60;
                current.setText(f.format(m) + ":" + g.format(s));
            }

            if (timer != null) timer.cancel();

            timer = new CountDownTimer(timeLeft, 1) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long min = (millisUntilFinished / 60000) % 60;
                    long sec = (millisUntilFinished / 1000) % 60;
                    next.setText(f.format(min) + ":" + g.format(sec));

                    if (current.getId() == R.id.player1Time)
                        player2TimeLeft = millisUntilFinished;
                    else
                        player1TimeLeft = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    parentNext.setBackground(context.getResources().getDrawable(R.drawable.red_flagged_button_padding));
                    parentCurrent.setClickable(false);
                    parentNext.setClickable(false);
                    gameOver = true;
                }
            }.start();
        }
        tapCounter++;
    }


    private void updatePlayerButtons (RelativeLayout current, RelativeLayout next){
        int inactive = R.drawable.greyed_out_button_padding;
        int active = R.drawable.blue_button_padding;

        AppCompatTextView currTime = (AppCompatTextView) current.getChildAt(0);
        AppCompatTextView nextTime = (AppCompatTextView) next.getChildAt(0);

        //check if game is just starting
        if (Objects.equals(current.getBackground().getConstantState(), next.getBackground().getConstantState())) {
            next.setBackgroundResource(active);
            nextTime.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            current.setBackgroundResource(inactive);
            currTime.setTextColor(Color.parseColor("#202020"));

            next.setBackgroundResource(active);
            nextTime.setTextColor(Color.WHITE);
        }

        //update the playPauseButton
        playPauseButton.setImageResource(R.drawable.pause_icon);
        currentRunningClock = next;
        lastPressedClock = current;

    }

    private void updatePlayerMoveCounts (AppCompatTextView current){
        RelativeLayout parent = (RelativeLayout) current.getParent();

        //if button is active(blue)
        if ((Objects.equals(parent.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.blue_button_padding).getConstantState()))){
            //increases moveCount on every second button press
            niftyCounter++;
            moveCount = (niftyCounter % 2 == 1) ? moveCount + 1 : moveCount;
            current.setText("Moves: " + moveCount);
        }
        else{
            current.setText("Moves: " + moveCount);
        }
    }
}


