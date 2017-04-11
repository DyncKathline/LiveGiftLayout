package org.dync.giftlibrary.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.dync.giftlibrary.R;
import org.dync.giftlibrary.adapter.FaceGVAdapter;
import org.dync.giftlibrary.adapter.FaceVPAdapter;
import org.dync.giftlibrary.widget.GiftModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by KathLine on 2017/1/12.
 */

public class GiftPanelControl {

    private int columns = 6;
    private int rows = 4;
    //每页显示的表情view
    private List<View> views = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ExpressionUtil expressionUtil;
    private LayoutInflater inflater;
    private List<GiftModel> mDatas;
    private Context mContext;
    private LinearLayout mDotsLayout;
    private ViewPager mViewpager;

    public interface GiftListener {
        void getGiftInfo(String giftPic, String giftName, String giftPrice);
    }

    private GiftListener giftListener;

    public void setGiftListener(GiftListener listener) {
        giftListener = listener;
    }

    /**
     * @param context
     * @param viewPager    竖屏礼物面板的ViewPager
     * @param recyclerView 横屏礼物面板的RecycleView
     * @param dotsLayout   竖屏礼物面板的小圆点父布局
     */
    public GiftPanelControl(Context context, ViewPager viewPager, RecyclerView recyclerView, LinearLayout dotsLayout) {
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mViewpager = viewPager;
        mRecyclerView = recyclerView;
        mDotsLayout = dotsLayout;
//        init();
    }

    /**
     *
     * @param datas datas为null时加载本地礼物图片
     */
    public void init(List<GiftModel> datas) {
        mDatas = datas;
        initPortraitGift();
        intitLandscapeGift();
    }

    /**
     * 初始化礼物面板，横屏时显示
     */
    private void intitLandscapeGift() {
        if (expressionUtil == null) {
            expressionUtil = new ExpressionUtil(mDatas != null);
        }
        if (mDatas == null) {
            mDatas = expressionUtil.initStaticGifts(mContext);
        }

        expressionUtil.giftView(mContext, mRecyclerView, mDatas);
    }

    /**
     * 初始化礼物面板，竖屏时显示
     */
    private void initPortraitGift() {
        if (expressionUtil == null) {
            expressionUtil = new ExpressionUtil(mDatas != null);
        }
        if (mDatas == null) {
            mDatas = expressionUtil.initStaticGifts(mContext);
        }
        int pagesize = expressionUtil.getPagerCount(mDatas.size(), columns, rows);
        // 获取页数
        for (int i = 0; i < pagesize; i++) {
            views.add(expressionUtil.viewPagerItem(mContext, i, mDatas, columns, rows, null));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(10, 0, 10, 0);
            if (pagesize > 1) {
                mDotsLayout.addView(dotsItem(i), params);
            }
        }
        if (pagesize > 1) {
            mDotsLayout.setVisibility(View.VISIBLE);
        } else {
            mDotsLayout.setVisibility(View.GONE);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewpager.setAdapter(mVpAdapter);
        mViewpager.setOnPageChangeListener(new PageChangeListener());
        mViewpager.setCurrentItem(0);
        if (pagesize > 1) {
            mDotsLayout.getChildAt(0).setSelected(true);
        }

        expressionUtil.setGiftClickListener(new ExpressionUtil.GiftClickListener() {
            @Override
            public void onClick(int position, String giftPic, String giftName, String giftPrice) {
                if (giftListener != null) {
                    giftListener.getGiftInfo(giftPic, giftName, giftPrice);
                }
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
    private boolean isClearStatus = true;//是否清除礼物选中的状态在切换页面时

    /**
     * 是否清除礼物选中的状态在切换页面时
     * @param isClearStatus
     */
    public void isClearStatus(boolean isClearStatus){
        this.isClearStatus = isClearStatus;
    }

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChangeListener implements ViewPager.OnPageChangeListener {

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
                if (isClearStatus){
                    adapter.clearSelection();
                    if (giftListener != null) {
                        giftListener.getGiftInfo("", "", "");
                    }
                }

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //ViewPager.SCROLL_STATE_IDLE 空闲状态 0；CustormViewPager.SCROLL_STATE_DRAGGING 正在滑动 1
            //ViewPager.SCROLL_STATE_SETTLING 滑动完毕 2；页面开始滑动时，状态变化（1,2,0）
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
