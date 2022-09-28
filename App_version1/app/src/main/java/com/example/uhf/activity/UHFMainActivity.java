package com.example.uhf.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;


import com.example.uhf.R;
import com.example.uhf.UhfInfo;
import com.example.uhf.fragment.SettingFragment;
import com.example.uhf.fragment.UHFReadTagFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * UHF使用demo
 * 1、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 * 更多函数的使用方法请查看API说明文档
 *
 * @author zhopin
 * 更新于 2020年7月23日
 */
public class UHFMainActivity extends BaseTabFragmentActivity {

    private final static String TAG = "MainActivity";
    String[] perms = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE};

    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    public UhfInfo uhfInfo=new UhfInfo();
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();


    /**
     ---------------------------------------------------
     SE INICIA EL PROGRAMA EN ESTA FUNCION      onCreate
     ---------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PackageManager pm =  getPackageManager();

        if(!check_all_files_permission()){
            popup_permissions();
        }

        setTitle(String.format(getString(R.string.app_name) + "(v%s)", getVerName()));
        initSound();
        initUHF();
        initViewPageData();

    }

    protected void initViewPageData() {

        fm = getSupportFragmentManager();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, fm, R.id.realtabcontent);


        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_msg_tab_scan)).setIndicator(getString(R.string.uhf_msg_tab_scan)),
                UHFReadTagFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.uhf_setting_fragment)).setIndicator(getString(R.string.uhf_setting_fragment)),
                SettingFragment.class,null);

    }

    @Override
    protected void onDestroy() {
        Log.e("zz_pp","onDestroy()");
        releaseSoundPool();
        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }




    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;

    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
    }

    private void releaseSoundPool() {
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public void playSound(int id) {
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolume / audioMaxVolume;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0不循环，-1永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public boolean check_all_files_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int per2 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if(per2 >=0){
                Toast.makeText(getApplicationContext(),"PERMISSION ALREADY GRANTED",Toast.LENGTH_SHORT).show();
                return true;
            }else{ return false;}

        }
        else{
            return false;
        }
    }

    /**
     * Ventanas de solicitud de permisos -----------------------------------------------------------
     *
     * */
    public void popup_request(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);


    }

    public void popup_all_files_permission(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    public void popup_permissions(){
            //popup_all_files_permission();
            popup_request();
    }
     /**
      * -------------------------------------------------------------------------------------------
      * */



}
