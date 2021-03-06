package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Vibrator vibrator;

    // The amount of time that can pass before the user is guaranteed to find a monster (seconds)
    // Currently 1 for debug purposes
    private static long maxFindTime = 1;

    private Button[] buttons = new Button[3];
    private ImageView foundImg;
    private TextView titleText;

    private Romon foundRomon;
    private boolean zAdded = false;
    private boolean zRemoved = false;
    private int dbVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //android.os.Debug.waitForDebugger();
        super.onCreate(savedInstanceState);

        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Getthing the database version information
        //SharedPreferences prefs = getPreferences(0);
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        dbVersion = prefs.getInt("dbVersion", 1);

        // Updating our db version with the remote
        DatabaseHelper db = new DatabaseHelper(this.getApplicationContext(), dbVersion);
        dbVersion = db.getRemoteVersion();

        // Updating our database version
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt("dbVersion", dbVersion);
        prefEditor.commit();

        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        // Button initilisation
        buttons[0] = (Button) findViewById(R.id.battleButton);
        buttons[1] = (Button) findViewById(R.id.tradeButton);
        buttons[2] = (Button) findViewById(R.id.listButton);
        foundImg = (ImageView) findViewById(R.id.foundImage);
        titleText = (TextView) findViewById(R.id.titleText);

        for (int i = 0; i < 3; ++i)
            buttons[i].setOnClickListener(this);

        foundImg.setOnClickListener(this);
        titleText.setOnClickListener(this);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        Log.i(TAG, "vibrator: " + vibrator.hasVibrator());

        // Clearing the bank for testing
        //db.clearBank();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        // Getting the preferences file and editor
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor prefEditor = prefs.edit();

        prefEditor.putLong("closeTime", System.currentTimeMillis() / 1000L);
        prefEditor.putBoolean("firstRun", false);
        prefEditor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        long openTime = System.currentTimeMillis() / 1000L;
        long prevOpened = prefs.getLong("closeTime", -1L);

        DatabaseHelper db = new DatabaseHelper(this.getApplicationContext(), dbVersion);
        Random rand = new Random();

        Log.i(TAG, "openTime: " + openTime);
        Log.i(TAG, "prevOpened: " + prevOpened);

        LocationHelper locHelper = new LocationHelper(MainActivity.this);
        /*Toast.makeText(getApplicationContext(),
                "Lat: " + locHelper.getLatitude() + "\nLong: " + locHelper.getLongtitude(),
                Toast.LENGTH_LONG).show();*/

        // User hasn't opened the app before, give them a freebie
        if (prevOpened == -1) {
            Log.i(TAG, "Monster found -- initial");

            // Giving the user a random romon for the first time
            foundRomon = db.getDexRomon(rand.nextInt(db.getDexCount()));
            foundImg.setImageResource(foundRomon.getDrawableResource());
        }
        else
        {
            double timeDiff = openTime - prevOpened;
            double findChance = timeDiff / maxFindTime;
            Random r = new Random();
            float roll = r.nextFloat();

            // Checking if we've found a monster
            if (findChance > roll)
            {
                Log.i(TAG, "Monster found -- rolled");

                // Ensuring that the we can click the image
                foundImg.setVisibility(View.VISIBLE);

                foundRomon = spawnRomon(locHelper.getLatitude(), locHelper.getLongtitude(), db);
                foundImg.setImageResource(foundRomon.getDrawableResource());

                //foundRomon = new Romon("A-mon", "Alan", R.drawable.unknown_romon);
                //foundImg.setImageResource(foundRomon.getDrawableResource());
            }
            else
            {
                Log.i(TAG, "No monster found!");
            }
        }
    }

    Romon spawnRomon(double latitude, double longtitiude, DatabaseHelper db)
    {
        double seedval = Math.abs(latitude) + Math.abs(longtitiude);
        int id;

        for(id = db.getDexCount(); id > 1; id--)
        {
            if(seedval % id == 0)
                break;
        }
        return db.getDexRomon(id);
    }

    public void onClick(View view) {
        // Giving the user a bit of feedback
        vibrator.vibrate(100);

        // Battle
        if (view == buttons[0]) {
            Log.i(TAG, "Battle button pressed!");
            Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
            startActivity(intent);
        }

        // Trade
        if (view == buttons[1]) {
            Log.i(TAG, "Trade button pressed!");
            Intent intent = new Intent(getApplicationContext(), TradeActivity.class);
            startActivity(intent);
        }

        // List
        if (view == buttons[2]) {
            Log.i(TAG, "List button pressed!");
            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(intent);
        }
        if (view == foundImg) {


            // Preventing horrible things
            if(foundRomon != null) {
                DatabaseHelper db = new DatabaseHelper(this.getApplicationContext(), dbVersion);
                db.addRomonBank(foundRomon);

                // Preventing adding the same romon multiple times
                foundImg.setVisibility(View.INVISIBLE);
                foundRomon = null;
            }
        }

        if(view == titleText)
        {
            DatabaseHelper db = new DatabaseHelper(this.getApplicationContext(), dbVersion);
            if(!zAdded)
            {
                // Adding Z-mon to the remote DB
                Romon zmon = new Romon("Z-mon", R.drawable.z_romon, 0);
                db.addRomonDexRemote(zmon);
                zAdded = true;
            }
            else if(!zRemoved)
            {
                db.removeRomonDexRemote("Z-mon");
                zRemoved = true;
            }
            Toast.makeText(this, "Remote version: " + db.getRemoteVersion(), Toast.LENGTH_SHORT).show();
        }
    }

}
