package org.dync.livegiftlayout;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dync.giftlibrary.util.GiftPanelControl;
import org.dync.giftlibrary.widget.GiftControl;
import org.dync.giftlibrary.widget.GiftFrameLayout;
import org.dync.giftlibrary.widget.GiftModel;
import org.dync.giftlibrary.widget.Viewpager;

public class Gift1Activity extends AppCompatActivity {

    private GiftFrameLayout giftFrameLayout1;
    private GiftFrameLayout giftFrameLayout2;

    private LinearLayout giftLayout;
    private LinearLayout ll_portrait;
    private LinearLayout ll_landscape;
    private TextView tvGiftNum;
    private ImageView btnGift;
    private Viewpager mViewpager;
    private LinearLayout mDotsLayout;

    private String giftstr = "";
    private RecyclerView mRecyclerView;
    private GiftControl giftControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift1);
        giftFrameLayout1 = (GiftFrameLayout) findViewById(R.id.gift_layout1);
        giftFrameLayout2 = (GiftFrameLayout) findViewById(R.id.gift_layout2);

        initGiftLayout();

        giftControl = new GiftControl(Gift1Activity.this);
        giftControl.setGiftLayout(giftFrameLayout1, giftFrameLayout2);
        tvGiftNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showGiftDialog();
            }
        });
        btnGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(giftstr)) {
                    Toast.makeText(getApplication(), "你还没选择礼物呢", Toast.LENGTH_SHORT).show();
                } else {
                    String numStr = tvGiftNum.getText().toString();
                    if (!TextUtils.isEmpty(numStr)) {
                        int giftnum = Integer.parseInt(numStr);
                        if (giftnum == 0) {
                            return;
                        } else {
                            giftControl.loadGift(new GiftModel(giftstr, "安卓机器人", giftnum, "http://www.baidu.com", "123", "Lee123", "http://www.baidu.com"));
                        }
                    }
                }
            }
        });
        findViewById(R.id.action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (giftLayout.getVisibility() == View.VISIBLE) {
                    giftLayout.setVisibility(View.GONE);
                } else {
                    giftLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initGiftLayout() {
        ll_portrait = (LinearLayout) findViewById(R.id.ll_portrait);
        ll_landscape = (LinearLayout) findViewById(R.id.ll_landscape);
        giftLayout = (LinearLayout) findViewById(R.id.giftLayout);
        tvGiftNum = (TextView) findViewById(R.id.toolbox_tv_gift_num);
        btnGift = (ImageView) findViewById(R.id.toolbox_iv_face);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_gift);
        mViewpager = (Viewpager) findViewById(R.id.toolbox_pagers_face);
        mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);

        giftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里的作用是消费掉点击事件
            }
        });

        GiftPanelControl giftPanelControl = new GiftPanelControl(this, mViewpager, mRecyclerView, mDotsLayout);
        giftPanelControl.setGiftListener(new GiftPanelControl.GiftListener() {
            @Override
            public void getGiftStr(String giftStr) {
                giftstr = giftStr;
            }
        });
    }

    private void showGiftDialog() {
        final GiftDialogFrament giftDialogFrament = new GiftDialogFrament();
        giftDialogFrament.show(getFragmentManager(), "GiftDialogFrament");
        giftDialogFrament.setGiftListener(new GiftDialogFrament.GiftListener() {
            @Override
            public void giftNum(String giftNum) {
                tvGiftNum.setText(giftNum);
                giftDialogFrament.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁动画
        if (giftControl != null) {
            giftControl.cleanAll();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
//            Log.e(TAG, "onConfigurationChanged: " + "横屏");
            onConfigurationLandScape();

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Log.e(TAG, "onConfigurationChanged: " + "竖屏");
            onConfigurationPortrait();
        }
    }

    private void onConfigurationPortrait() {
        ll_portrait.setVisibility(View.VISIBLE);
        ll_landscape.setVisibility(View.GONE);
    }

    private void onConfigurationLandScape() {
        ll_portrait.setVisibility(View.GONE);
        ll_landscape.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (giftLayout.getVisibility() == View.VISIBLE) {
                    giftLayout.setVisibility(View.GONE);
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
