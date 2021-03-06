package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class ListActivity extends Activity implements View.OnClickListener
{
    private final static String TAG = "ListActivity";

    private Button toggleButton;
    private boolean viewingDex = true;
    private DatabaseHelper dbHelper;

    private ArrayList<Romon> dexRomon;
    private ArrayList<Romon> bankRomon;

    private ArrayList<TextView> dexNames = new ArrayList<TextView>();
    private ArrayList<ImageView> dexIcons = new ArrayList<ImageView>();

    private ArrayList<TextView> bankNames= new ArrayList<TextView>();
    private ArrayList<ImageView> bankIcons = new ArrayList<ImageView>();

    private GridLayout gridLayout;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate!");

        setContentView(R.layout.list_layout);
        dbHelper = new DatabaseHelper(this, getSharedPreferences("prefs", 0).getInt("dbVersion", 1));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gridLayout = (GridLayout)findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(2);

        // Setting up the toggleButton
        toggleButton = (Button)findViewById(R.id.toggleButton);
        toggleButton.setText("View Banked Romon");
        toggleButton.setOnClickListener(this);

        // Populating our romon lists
        dexRomon = dbHelper.getDexRomon();
        bankRomon = dbHelper.getBankRomon();

        //GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        //layoutParams.setGravity(Gravity.RIGHT);

        // Setting up the dex view
        for(int i = 0; i < dexRomon.size(); ++i)
        {
            dexNames.add(i, new TextView(this));
            dexNames.get(i).setText(dexRomon.get(i).getName());

            dexIcons.add(i, new ImageView(this));
            dexIcons.get(i).setImageResource(dexRomon.get(i).getDrawableResource());
        }

        for(int i = 0; i < bankRomon.size(); ++i)
        {
            bankNames.add(i, new TextView(this));
            bankNames.get(i).setText(bankRomon.get(i).getNickname());

            bankIcons.add(i, new ImageView(this));
            bankIcons.get(i).setImageResource(bankRomon.get(i).getDrawableResource());
        }

        // Showing the dex values initially
        showDex();
    }

    public void onClick(View view)
    {
        Log.i(TAG, "onClick");
        if(view == toggleButton)
        {
            Log.i(TAG, "toggle!");
            if(viewingDex)
            {
                toggleButton.setText("View Romon Dex");
                showBank();
            }
            else
            {
                toggleButton.setText("View Banked Romon");
                showDex();
            }

            viewingDex = !viewingDex;
        }
    }

    private void showDex()
    {
        // Clearing the layout
        gridLayout.removeAllViewsInLayout();

        // Populating the layouts
        for(int i = 0; i < dexRomon.size(); ++i)
        {
            //namesLayout.addView(dexNames.get(i));
            gridLayout.addView(dexNames.get(i));
            gridLayout.addView(dexIcons.get(i));
        }

    }

    private void showBank()
    {
        // Clearing the layouts
        gridLayout.removeAllViewsInLayout();

        // Populating the namesLayout
        for(int i = 0; i < bankRomon.size(); ++i)
        {
            gridLayout.addView(bankNames.get(i));
            gridLayout.addView(bankIcons.get(i));
        }
    }
}
