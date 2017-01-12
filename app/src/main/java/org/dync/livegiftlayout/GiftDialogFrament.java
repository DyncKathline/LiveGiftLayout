package org.dync.livegiftlayout;

import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by KathLine on 2017/1/10.
 */

public class GiftDialogFrament extends DialogFragment {

    private GiftListener giftListener;
    private TextView tvNum;
    private TextView tvOk;
    private GridView gridView;

    StringBuffer strBuffer = new StringBuffer();
    private int maxLength;

    public interface GiftListener {
        void giftNum(String giftNum);
    }

    public void setGiftListener(GiftListener listener) {
        giftListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_gift, container);
        setLayout();
        initView(view);
        return view;
    }

    private void initView(View view) {
        tvNum = (TextView) view.findViewById(R.id.tv_num);
        tvOk = (TextView) view.findViewById(R.id.tv_ok);
        gridView = (GridView) view.findViewById(R.id.gridView);

        initData();

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (giftListener != null) {
                    String numStr = tvNum.getText().toString();
                    if (!TextUtils.isEmpty(numStr)) {
                        if (!numStr.startsWith("0")) {
                            giftListener.giftNum(numStr);
                        }else {
                            Toast.makeText(getDialog().getContext(), "首位不能为0哦", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getDialog().getContext(), "数量不能为空或为0哦", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void initData() {
        final List<HashMap<String, String>> datas = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("keyborad", String.valueOf(i));
            datas.add(map);
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("keyborad", "取消");
        datas.add(map);
        map = new HashMap<>();
        map.put("keyborad", "0");
        datas.add(map);
        map = new HashMap<>();
        map.put("keyborad", "X");
        datas.add(map);


        maxLength = getMaxLengthForTextView(tvNum);

        SimpleAdapter arrayAdapter = new SimpleAdapter(getDialog().getContext(), datas, R.layout.item_keyborad, new String[]{"keyborad"}, new int[]{R.id.tv_num});

        gridView.setAdapter(arrayAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = datas.get(position);
                String keyborad = map.get("keyborad");
                if (!keyborad.equals("取消") && !keyborad.equals("X")) {
                    if (strBuffer.length() < maxLength) {
                        strBuffer.append(keyborad);
                        tvNum.setText(strBuffer);
                    }
                } else if (keyborad.equals("取消")) {
                    dismiss();
                } else if (keyborad.equals("X")) {
                    if (strBuffer.length() > 0) {
                        strBuffer.deleteCharAt(strBuffer.length() - 1);
                        tvNum.setText(strBuffer);
                    }
                }
            }
        });
    }

    /**
     * 获取TextView的最大长度，即xml中的android:maxLength属性
     *
     * @param textView
     * @return
     */
    public int getMaxLengthForTextView(TextView textView) {
        int maxLength = -1;

        for (InputFilter filter : textView.getFilters()) {
            if (filter instanceof InputFilter.LengthFilter) {
                try {
                    Field maxLengthField = filter.getClass().getDeclaredField("mMax");
                    maxLengthField.setAccessible(true);

                    if (maxLengthField.isAccessible()) {
                        maxLength = maxLengthField.getInt(filter);
                    }
                } catch (IllegalAccessException e) {
                    Log.w(filter.getClass().getName(), e);
                } catch (IllegalArgumentException e) {
                    Log.w(filter.getClass().getName(), e);
                } catch (NoSuchFieldException e) {
                    Log.w(filter.getClass().getName(), e);
                } // if an Exception is thrown, Log it and return -1
            }
        }

        return maxLength;
    }

    private void setLayout() {
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setGravity(Gravity.BOTTOM);
//        window.setWindowAnimations(R.style.AnimBottom);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0;
        window.setAttributes(lp);
    }
}
