package org.dync.livegiftlayout;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dync.giftlibrary.adapter.FaceGVAdapter;
import org.dync.giftlibrary.adapter.FaceVPAdapter;
import org.dync.giftlibrary.util.ExpressionUtil;
import org.dync.giftlibrary.widget.GiftControl;
import org.dync.giftlibrary.widget.GiftFrameLayout;
import org.dync.giftlibrary.widget.GiftModel;
import org.dync.giftlibrary.widget.Viewpager;

import java.util.ArrayList;
import java.util.List;

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
    private LayoutInflater inflater;

    private int columns = 6;
    private int rows = 4;
    //每页显示的表情view
    private List<View> views = new ArrayList<>();
    private String giftstr;
    private RecyclerView rvGift;
    private ExpressionUtil expressionUtil;
    private List<String> staticFacesList;
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
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        giftLayout = (LinearLayout) findViewById(R.id.giftLayout);
        tvGiftNum = (TextView) findViewById(R.id.toolbox_tv_gift_num);
        btnGift = (ImageView) findViewById(R.id.toolbox_iv_face);

        rvGift = (RecyclerView) findViewById(R.id.rv_gift);
        mViewpager = (Viewpager) findViewById(R.id.toolbox_pagers_face);
        mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);

        mViewpager.addOnPageChangeListener(new PageChange());
        initViewPager();

        initGiftView();

        giftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里的作用是消费掉点击事件
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

    /**************************
     * 礼物面板
     **************************/

    private void initGiftView() {
        if (expressionUtil == null) {
            expressionUtil = new ExpressionUtil();
        }
        if (staticFacesList == null) {
            staticFacesList = expressionUtil.initStaticFaces(this);
        }

        expressionUtil.giftView(this, rvGift, staticFacesList);
    }

    /**
     * 初始化礼物面板
     */
    private void initViewPager() {
        expressionUtil = new ExpressionUtil();
        staticFacesList = expressionUtil.initStaticFaces(this);
        int pagesize = expressionUtil.getPagerCount(staticFacesList.size(), columns, rows);
        // 获取页数
        for (int i = 0; i < pagesize; i++) {
            views.add(expressionUtil.viewPagerItem(this, i, staticFacesList, columns, rows, null));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(10, 0, 10, 0);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewpager.setAdapter(mVpAdapter);
        mViewpager.setCurrentItem(0);
        mDotsLayout.getChildAt(0).setSelected(true);

        expressionUtil.setGiftClickListener(new ExpressionUtil.GiftClickListener() {
            @Override
            public void onClick(int position, String pngStr) {
                giftstr = pngStr;
            }
        });
    }

    /**
     * 表情页切换时，底部小圆点
     *
     * @param position
     * @return
     */
    private ImageView dotsItem(int position) {
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    private boolean isScrolling = false;
    private boolean left = false;//从右向左，positionOffset值逐渐增大
    private boolean right = false;//从左向右，positionOffset值逐渐减小
    private int lastValue = -1;

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChange implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //这里的position是当前屏幕可见页面的第一个页面
            if (isScrolling) {
                if (lastValue > positionOffsetPixels) {
                    //递减，向右滑动
                    right = true;
                    left = false;
                } else if (lastValue < positionOffsetPixels) {
                    //递增，向左滑动
                    right = false;
                    left = true;
                } else if (lastValue == positionOffsetPixels) {
                    right = left = false;
                }
            }
//            Log.i("CustormViewPager", "onPageScrolled: positionOffset =>" + positionOffset + "; positionOffsetPixels =>" + positionOffsetPixels);
//            Log.i("CustormViewPager", "onPageScrolled: right =>" + right + "; left =>" + left);
            lastValue = positionOffsetPixels;
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(position).setSelected(true);
            for (int i = 0; i < views.size(); i++) {//清除选中，当礼物页面切换到另一个礼物页面
                RecyclerView view = (RecyclerView) views.get(i);
                FaceGVAdapter adapter = (FaceGVAdapter) view.getAdapter();
                adapter.clearSelection();
                giftstr = "";
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //CustormViewPager.SCROLL_STATE_IDLE 空闲状态 0；CustormViewPager.SCROLL_STATE_DRAGGING 正在滑动 1
            //CustormViewPager.SCROLL_STATE_SETTLING 滑动完毕 2；页面开始滑动时，状态变化（1,2,0）
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                isScrolling = true;
            } else {
                isScrolling = false;
            }
            if (state == ViewPager.SCROLL_STATE_SETTLING) {
//                Log.i("CustormViewPager", "----------------right =>" + right + "; left =>" + left + "----------------------------");
                right = left = false;
//                lastValue = -1;
            }
        }
    }
}
