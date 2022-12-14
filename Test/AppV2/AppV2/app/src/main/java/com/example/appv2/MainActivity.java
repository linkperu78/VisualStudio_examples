package com.example.appv2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.rscja.deviceapi.RFIDWithUHFUART;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPagerAdapter MyViewPagerAdapter;
    private String TAG = "MainActivity";
    public RFIDWithUHFUART mReader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.table_layout);
        viewPager2 = findViewById(R.id.view_pager);
        MyViewPagerAdapter = new MyViewPagerAdapter(this);
        viewPager2.setAdapter(MyViewPagerAdapter);
        viewPager2.setUserInputEnabled(false);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
        //      Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                viewPager2.setCurrentItem(tab.getPosition());
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select(); }
    });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            Log.d(TAG, "initUHF: " + " NOOOO Se logro inicializar");
            return;
        }

        if (mReader != null) {
            Log.d(TAG, "initUHF: " + " Se logro inicializar");
            try {
                mReader.init();
            }
            catch (Exception exception){
                Log.d(TAG, "initUHF: " + " NOOOO Se logro inicializar");
                return;
            }
            mReader.free();
            Log.d(TAG, "initUHF: " + " CERRAR");
        }
    }

}