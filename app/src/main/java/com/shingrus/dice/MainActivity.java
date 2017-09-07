package com.shingrus.dice;

    import android.content.Context;
    import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
    import android.os.Vibrator;
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

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MAIN";
    private static final int DICE_ROLL_ITERATIONS = 12;
    public boolean diceThreadStarted = false;
    public static final int DICE_SIZE = 6;

    List<String> dice = new ArrayList<>(DICE_SIZE);

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private    TextView diceView ;
    final Random random = new Random();
    private Vibrator vibrator = null;

    int rollingColor = 0;
    int readyColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        diceView = (TextView) findViewById(R.id.dice);

        rollingColor = getResources().getColor(R.color.diceRolling);
        readyColor = getResources().getColor(R.color.diceReady);

        for (int i= 1 ; i<=DICE_SIZE; i++) {dice.add(String.format(Locale.US,"%d", i));}

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startRollingDice(false);
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                Log.d(LOG_TAG, String.format(Locale.US,"Shake count: %d", count));

                if (count >=2 )
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
            Log.d(LOG_TAG, "Start thread");
            //TODO: fix vibration - doesn't work on my s8 :(
//            if(isShake && vibrator != null ) {
//                vibrator.vibrate(500);
//                Log.d(LOG_TAG, "vibrate");
//            }
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
