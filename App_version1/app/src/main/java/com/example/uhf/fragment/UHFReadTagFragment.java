package com.example.uhf.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

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
    private ArrayList<HashMap<String, String>> data_matched_hashmap;
    private ArrayList<String> EPC_numbers;
    final static String codigo_empresa = "3034";
    boolean buffer_stop = false;

    SimpleAdapter adapter;


    TextView BtClear;
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
    private int total_items = 0;

    static ArrayList<String[]> data_from_file = new ArrayList<>();
    static String[] header_from_file;
    static int pos_style = 0;
    static int pos_colorname = 2;
    static int pos_size = 3;
    static int pos_EPCnumber = -1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            if(info.getEPC().substring(0,4).equals(codigo_empresa) && buffer_stop) {
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
        BtClear = (TextView) getView().findViewById(R.id.BtClear);
        tvTime = (TextView) getView().findViewById(R.id.tvTime);
        tvTime.setText("0s");
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        tv_total = (TextView) getView().findViewById(R.id.tv_total);
        tv_total.setText(String.valueOf(total_items));

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);

        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi});
        LvTags.setAdapter(adapter);


        BtClear.setOnClickListener(new BtClearClickListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());

        LvTags.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "D= " + String.valueOf(position), Toast.LENGTH_SHORT).show();
                return true;
            }
        });



        clearData();
        tv_count.setText(mContext.tagList.size()+"");
        Log.i(TAG, "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
         if(!UHFMainActivity.data.isEmpty()) {
            BtClear.setText(UHFMainActivity.file_name);
            data_from_file = UHFMainActivity.data;
            header_from_file = UHFMainActivity.header;
            pos_EPCnumber = header_from_file.length-1;
        }
    }


    @Override
    public void onPause() {
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
                tv_count.setText("" + adapter.getCount());
                int total = Integer.parseInt((String) tv_total.getText());
                tv_total.setText(String.valueOf(total+1));

            }else{
                boolean index2 = EPC_numbers.contains(epc);
                if(!index2){
                    EPC_numbers.add(epc);
                    int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
                    int total = Integer.parseInt((String) tv_total.getText());
                    tv_total.setText(String.valueOf(total+1));
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
            mContext.uhfInfo=new UhfInfo();
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tagList.clear();

        EPC_numbers.clear();
        tv_total.setText("0");
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
                match_UPC_EPC_data(tagList);
                break;
            case 2: // Se presiona tres veces el gatillo, se reinicia la lectura
                clearData();
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
                Toast.makeText(mContext, "Se detuvo correctamente el escáner", Toast.LENGTH_SHORT).show();
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
            }
            buffer_stop = false;
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
                buffer_stop = true;
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

    /**
     * ESTA CLASS ES LA QUE INGRESA DATOS AL LISTVIEW.
     * adapter = new MyAdapter(Context context);
     */

    private void match_UPC_EPC_data(ArrayList<HashMap<String,String>> data_EPC){
        while (buffer_stop){

        }
        if(data_from_file.isEmpty())
        {
            Toast.makeText(mContext, "No se encuentra una base de datos", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i<tagList.size(); i++){
            String EPC_value = tagList.get(i).get("tagUii");

            Log.d(TAG, "SE ESTA LEYENDO EL EPC =  " + EPC_value);

            map = new HashMap<String, String>();
            map.put("tagUii", EPC_value);
            map.put("tagCount",tagList.get(i).get("tagCount"));
            map.put("tagRssi","??");
            map.put("tagLen","??");
            for(int k=0; k<data_from_file.size(); k++){
                String[] temp = data_from_file.get(k);
                if(temp[pos_EPCnumber].equals(EPC_value)){
                    //map = new HashMap<String, String>();
                    map.put("tagUii", temp[pos_style]);
                    //map.put("tagCount",tagList.get(i).get("tagCount"));
                    map.put("tagRssi",temp[pos_size]);
                    map.put("tagLen",temp[pos_colorname]);
                    break;
                }
            }

            tagList.set(i,map);
        }
        LvTags.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
