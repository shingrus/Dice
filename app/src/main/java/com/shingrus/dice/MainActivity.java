package com.shingrus.dice;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MAIN";
    private static final int DICE_ROLL_ITERATIONS = 12;
    public boolean diceThreadStarted = false;
    public static final int DICE_SIZE = 6;

    List<String> dice = new ArrayList<>(DICE_SIZE);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final TextView diceView = (TextView) findViewById(R.id.dice);
        final Random random = new Random();

        final int rollingColor = getResources().getColor(R.color.diceRolling);
        final int readyColor = getResources().getColor(R.color.diceReady);


        for (int i= 1 ; i<=DICE_SIZE; i++) {dice.add(String.format(Locale.US,"%d", i));}

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //start my own thread and
                if (!diceThreadStarted) {
                    diceThreadStarted = true;
                    Log.d(LOG_TAG, "Start thread");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Collections.shuffle(dice);

                            int currentDiceRollIterations = DICE_ROLL_ITERATIONS - random.nextInt(DICE_ROLL_ITERATIONS/2);
                            Log.d(LOG_TAG, "Thorow iterations: "  + Integer.toString(currentDiceRollIterations));
                            for (int i = 0; i < currentDiceRollIterations; i++) {

                                final String diceValue = dice.get(i%DICE_SIZE);

                                if (i == 0) diceView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        diceView.setTextColor(rollingColor);
                                    }
                                });

                                diceView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        diceView.setText(diceValue);
                                    }
                                });

                                if (i == currentDiceRollIterations -1) diceView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        diceView.setTextColor(readyColor);
                                    }
                                });

                                try {
                                    Thread.sleep(100 - i*2);
                                } catch (InterruptedException e) {
                                    Log.e(LOG_TAG, e.getMessage());
                                }
                            }
                            Log.d(LOG_TAG,"Finish thread");
                            diceThreadStarted = false;
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
