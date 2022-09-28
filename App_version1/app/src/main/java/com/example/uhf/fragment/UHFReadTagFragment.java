package com.example.uhf.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.UhfInfo;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.NumberTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UHFReadTagFragment extends KeyDwonFragment {
    private static final String TAG = "UHFReadTagFragment";
    private boolean loopFlag = false;


    private List<String> tempDatas = new ArrayList<>();

    private ArrayList<HashMap<String, String>> tagList;
    private ArrayList<String> EPC_numbers;
    final static String codigo_empresa = "3034";

    SimpleAdapter adapter;
    Button BtClear;
    TextView tvTime;
    TextView tv_count;
    TextView tv_total;
    Button BtInventory;
    ListView LvTags;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;


    private long time;


    private String[] estado_lector = {"Start","Stop","Clear"};
    private int estado_int;


    public ArrayList<String[]> data_from_file = new ArrayList<>();
    public String[] header_from_file;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            if(info.getEPC().substring(0,4).equals(codigo_empresa)) {
                addDataToList(info.getEPC(), EPC_norm(info.getEPC()), info.getRssi());
            }
            setTotalTime();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onCreateView");
        return inflater.inflate(R.layout.uhf_readtag_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        estado_int=0;
        mContext = (UHFMainActivity) getActivity();
        mContext.currentFragment=this;
        tagList = new ArrayList<HashMap<String, String>>();
        EPC_numbers = new ArrayList<String>();
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tvTime = (TextView) getView().findViewById(R.id.tvTime);
        tvTime.setText("0s");
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        tv_total = (TextView) getView().findViewById(R.id.tv_total);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);

        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi});
        LvTags.setAdapter(adapter);


        BtClear.setOnClickListener(new BtClearClickListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());


        //clearData();
        tv_count.setText(mContext.tagList.size()+"");
        tv_total.setText(""+"");
        Log.i(TAG, "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        if(!UHFMainActivity.data.isEmpty()) {
            Log.d(TAG, "Length of Data" + UHFMainActivity.data.size());
            Log.d(TAG, "Length of Header" + UHFMainActivity.header.length);
        }
    }


    @Override
    public void onPause() {
        Log.i(TAG, "UHFReadTagFragment.onPause");
        super.onPause();
        stopInventory();
    }

    /**
     * En esta funcion se guardan los datos en el diccionario "map"
     * y luego se agregan a  "tagList"
     *
     */
    private void addDataToList(String epc,String epcAndTidUser, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            String epc_tag = EPC_norm(epc);
            int index = checkIsExist(epc_tag);
            map = new HashMap<String, String>();
            map.put("tagUii", epc_tag);
            map.put("tagCount", String.valueOf(1));

            if(index == -1){
                tagList.add(map);
                EPC_numbers.add(epc);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());

            }else{
                boolean index2 = EPC_numbers.contains(epc);
                if(!index2){
                    EPC_numbers.add(epc);
                    int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
                    map.put("tagCount", String.valueOf(tagcount));
                    tagList.set(index, map);
                }

            }
            adapter.notifyDataSetChanged();

        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
            selectItem=-1;
            mContext.uhfInfo=new UhfInfo();
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tagList.clear();
        EPC_numbers.clear();
        Log.i("MY", "tagList.size " + tagList.size());
        adapter.notifyDataSetChanged();
    }



    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        switch (estado_int) {
            case 0: // Se presiona una vez el gatillo, comienza la lectura
                if (mContext.mReader.startInventoryTag()) {
                    loopFlag = true;
                    time = System.currentTimeMillis();
                    new TagThread().start(); // Se inicia la lectura de los RFIDS en segundo plano
                    } else {
                    mContext.mReader.stopInventory();
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
                }
                break;
            case 1: // Se presiona dos veces el gatillo, se pausa la lectura
                stopInventory();
                setTotalTime();
                break;
            case 2: // Se presiona tres veces el gatillo, se reinicia la lectura
                clearData();
                selectItem=-1;
                mContext.uhfInfo=new UhfInfo();
                break;
            default:
                break;
        }
        estado_int++; estado_int = estado_int%3;
        BtInventory.setText(estado_lector[estado_int]);
    }

    /*      STOP READ
            stopInventory();
            setTotalTime();
     */

    private void setTotalTime() {
        float useTime = (System.currentTimeMillis() - time) / 1000.0F;
        tvTime.setText(NumberTool.getPointDouble(1, useTime) + "s");
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * 判断EPC是否在列表中
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    handler.sendMessage(msg);
                    mContext.playSound(1);
                }
            }
        }
    }

    /** AQUI ESTA LA ESTRUCTURA DE COMO SE RECIBEN LOS DATOS PARA
     *  ALMACENARLOS

     */
    private String EPC_norm(String epc) {
        String data=epc.substring(0,15);             // El numero EPC
        return  data;
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }


    //-----------------------------
    private int  selectItem=-1;
    public final class ViewHolder {
        public TextView tvEPCTID;
        public TextView tvTagCount;
        public TextView tvTagRssi;
    }

    /**
     * ESTA CLASS ES LA QUE INGRESA DATOS AL LISTVIEW.
     * adapter = new MyAdapter(Context context);
     */

}
