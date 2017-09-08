package com.shingrus.dice;

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.hardware.Sensor;
import android.hardware.SensorManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Vibrator;
    import android.support.constraint.ConstraintLayout;
    import android.support.constraint.ConstraintSet;
    import android.support.design.widget.FloatingActionButton;
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

    import it.sephiroth.android.library.tooltip.Tooltip;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MAIN";
    private static final int DICE_ROLL_ITERATIONS = 12;
    public boolean diceThreadStarted = false;
    public static final int DICE_SIZE = 6;
    int rollCounter = 0;

    ArrayList<ArrayList<String>> dices = new ArrayList<>(2);



    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
//    private    TextView diceView1,diceView2 ;
    private TextView[] dicesView = new TextView[2];
    final Random random = new Random();
    private Vibrator vibrator = null;
    Tooltip.TooltipView tooltip;
    Settings settings;

    int rollingColor = 0;
    int readyColor = 0;


    private class Settings {
        int dicesCount;
        boolean vibroEnabled;
        final static String PREF_KEY_DICES_COUNT = "Dices_Count";
        final static String PREF_KEY_VIBRO_ENABLED = "VIBRO_ENABLED";


        Settings(SharedPreferences preferences) {
            this.dicesCount = preferences.getInt(PREF_KEY_DICES_COUNT,1);
            this.vibroEnabled = preferences.getBoolean(PREF_KEY_VIBRO_ENABLED, true);
            Log.d(LOG_TAG, "Settings init: vibro: " + vibroEnabled + ", count: "+ dicesCount);
        }
        void store(SharedPreferences preferences) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_KEY_VIBRO_ENABLED, vibroEnabled);
            editor.putInt(PREF_KEY_DICES_COUNT, dicesCount == 0?1:dicesCount);
            editor.apply();
            Log.d(LOG_TAG, "Settings store: vibro: " + vibroEnabled + ", count: "+ dicesCount);
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        dicesView[0] = (TextView) findViewById(R.id.dice1);
        dicesView[1] = (TextView) findViewById(R.id.dice2);

        //init preferences
        settings = new Settings(getPreferences(0));

        if(settings.dicesCount==2) {
            dicesView[1].setVisibility(View.VISIBLE);
        }
        rollingColor = getResources().getColor(R.color.diceRolling);
        readyColor = getResources().getColor(R.color.diceReady);

        dices.add(new ArrayList<String>(DICE_SIZE));
        dices.add(new ArrayList<String>(DICE_SIZE));
        for (int i= 1 ; i<=DICE_SIZE; i++) {
            dices.get(0).add(String.format(Locale.US,"%d", i));
            dices.get(1).add(String.format(Locale.US,"%d", i));
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startRollingDice(false);
            }
        });

        tooltip = Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(fab, Tooltip.Gravity.TOP)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(getResources().getString(R.string.startTooltip))
                        .withArrow(true)
                        .floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                        .withOverlay(false)
                        .withStyleId(R.style.ToolTipLayoutDefaultStyle_StartTooltip)
                        .build()
        );

        tooltip.show();

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if (count >=1 )
                    startRollingDice(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    /**
     *
     * @param isShake - boolean if shaked - enable vibro
     */
    private void startRollingDice(boolean isShake ) {
        //start my own thread and
        if (!diceThreadStarted) {
            diceThreadStarted = true;
            if(rollCounter ==0 )  tooltip.hide();
            rollCounter++;
            if(isShake && settings.vibroEnabled && vibrator != null ) {
                long[] pattern = {50, 75, 40, 50};
                vibrator.vibrate(pattern,-1);
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    Collections.shuffle(dices.get(0));
                    Collections.shuffle(dices.get(1));

                    int currentDiceRollIterations = DICE_ROLL_ITERATIONS - random.nextInt(DICE_ROLL_ITERATIONS/2);
//                    Log.d(LOG_TAG, "Thorow iterations: "  + Integer.toString(currentDiceRollIterations));
                    for (int i = 0; i < currentDiceRollIterations; i++) {

                        final String diceValue1 = dices.get(0).get(i%DICE_SIZE);
                        final String diceValue2 = dices.get(1).get(i%DICE_SIZE);

                        if (i == 0) dicesView[0].post(new Runnable() {
                            @Override
                            public void run() {
                                dicesView[0].setTextColor(rollingColor);
                                dicesView[1].setTextColor(rollingColor);
                            }
                        });

                        dicesView[0].post(new Runnable() {
                            @Override
                            public void run() {
                                dicesView[0].setText(diceValue1);
                                dicesView[1].setText(diceValue2);
                            }
                        });

                        if (i == currentDiceRollIterations -1) dicesView[0].post(new Runnable() {
                            @Override
                            public void run() {
                                dicesView[0].setTextColor(readyColor);
                                dicesView[1].setTextColor(readyColor);
                            }
                        });

                        try {
                            Thread.sleep(100 - i*2);
                        } catch (InterruptedException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }
//                    Log.d(LOG_TAG,"Finish thread");
                    diceThreadStarted = false;
                }
            }).start();
        }
    }

    void changeDiceCount() {
        if (settings.dicesCount==2) {

            TextView dice2 = (TextView) findViewById(R.id.dice2);
            dice2.setVisibility(View.VISIBLE);
        }
        else { //dicesCuount == 1
            TextView dice2 = (TextView) findViewById(R.id.dice2);
            dice2.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_Enable_Vibro);
        if (!settings.vibroEnabled) {
            item.setTitle(R.string.action_Enable_Vibro);
        }
        item = menu.findItem(R.id.action_DicesCount);
        if(settings.dicesCount==2) {
            item.setTitle(R.string.action_Enable_1_Dice);
        }

        return true;
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }



        switch (id) {
            case R.id.action_DicesCount:
                if(settings.dicesCount ==1 ) {
                    settings.dicesCount = 2;
                    item.setTitle(R.string.action_Enable_1_Dice);
                }
                else {
                    settings.dicesCount = 1;
                    item.setTitle(R.string.action_Enable_2_Dices);
                }
                changeDiceCount();
                break;
            case R.id.action_Enable_Vibro:
                settings.vibroEnabled=!settings.vibroEnabled;
                item.setChecked(settings.vibroEnabled);
                if(settings.vibroEnabled) {
                    item.setTitle(R.string.action_Disale_Vibro);
                }
                else
                    item.setTitle(R.string.action_Enable_Vibro);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        settings.store(getPreferences(0));

        return super.onOptionsItemSelected(item);
    }
}
