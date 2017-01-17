package org.dync.livegiftlayout;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.dync.giftlibrary.util.GiftPanelControl;
import org.dync.giftlibrary.widget.GiftModel;
import org.dync.giftlibrary.widget.LeftGiftControl;
import org.dync.giftlibrary.widget.LeftGiftsItemLayout;


public class Gift2Activity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private LinearLayout giftLl;

    private LeftGiftsItemLayout gift1;
    private LeftGiftsItemLayout gift2;


    private LinearLayout giftLayout;
    private LinearLayout ll_portrait;
    private LinearLayout ll_landscape;
    private ImageView btnGift;
    private ViewPager mViewPager;
    private LinearLayout mDotsLayout;

    private LeftGiftControl leftGiftControl;
    private String giftstr = "";
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift2);
        giftLl = (LinearLayout) findViewById(R.id.giftLl);
        gift1 = (LeftGiftsItemLayout) findViewById(R.id.gift1);
        gift2 = (LeftGiftsItemLayout) findViewById(R.id.gift2);


        initGiftLayout();

        giftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        leftGiftControl = new LeftGiftControl(this);
        leftGiftControl.setGiftsLayout(gift1, gift2);
    }

    private void initGiftLayout() {
        ll_portrait = (LinearLayout) findViewById(R.id.ll_portrait);
        ll_landscape = (LinearLayout) findViewById(R.id.ll_landscape);
        giftLayout = (LinearLayout) findViewById(R.id.giftLayout);
        btnGift = (ImageView) findViewById(R.id.toolbox_iv_face);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_gift);
        mViewPager = (ViewPager) findViewById(R.id.toolbox_pagers_face);
        mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);

        btnGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(giftstr)) {
                    Toast.makeText(getApplication(), "你还没选择礼物呢", Toast.LENGTH_SHORT).show();
                } else {
                    leftGiftControl.loadGift(GiftModel.create(giftstr, "安卓机器人", 1, "http://www.baidu.com", "123", "Lee125", "http://www.baidu.com"));
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

        GiftPanelControl giftPanelControl = new GiftPanelControl(this, mViewPager, mRecyclerView, mDotsLayout);
        giftPanelControl.setGiftListener(new GiftPanelControl.GiftListener() {
            @Override
            public void getGiftStr(String giftStr) {
                giftstr = giftStr;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (giftLl != null) {
            leftGiftControl.cleanAll();
        }
    }
}
