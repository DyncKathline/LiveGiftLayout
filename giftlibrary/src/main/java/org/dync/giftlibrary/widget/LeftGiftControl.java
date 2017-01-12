package org.dync.giftlibrary.widget;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.dync.giftlibrary.R;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by KathLine on 2017/1/8.
 */
public class LeftGiftControl implements LeftGiftsItemLayout.LeftGiftAnimationStatusListener {

    /**
     * 礼物1
     */
    private LeftGiftsItemLayout mFirstItemGift;

    /**
     * 礼物2
     */
    private LeftGiftsItemLayout mSecondItemGift;

    /**
     * 礼物队列(在多个线程中使用此List)
     */
    private CopyOnWriteArrayList<GiftModel> mGiftQueue;

    /**
     * 礼物开始动画
     */
    private Animation mGiftEnterAnimation1;

    private Animation mGiftEnterAnimation2;

    /**
     * 礼物结束动画
     */
    private Animation mGiftExitAnimation1;

    private Animation mGiftExitAnimation2;


    public LeftGiftControl(Context context) {
        init(context);
    }

    private void init(Context context) {
        mGiftQueue = new CopyOnWriteArrayList<>();
        mGiftEnterAnimation1 = AnimationUtils.loadAnimation(context, R.anim.drop_from_left);
        mGiftEnterAnimation1.setFillAfter(true);
        mGiftEnterAnimation2 = AnimationUtils.loadAnimation(context, R.anim.drop_from_left);
        mGiftEnterAnimation2.setFillAfter(true);

        mGiftExitAnimation1 = AnimationUtils.loadAnimation(context, R.anim.hide_to_top);
        mGiftExitAnimation1.setFillAfter(true);
        mGiftExitAnimation2 = AnimationUtils.loadAnimation(context, R.anim.hide_to_top);
        mGiftExitAnimation2.setFillAfter(true);

        mGiftExitAnimation1.setAnimationListener(new GiftExitAnimationListener());
        mGiftExitAnimation2.setAnimationListener(new GiftExitAnimationListener());
    }

    public void setGiftsLayout(LeftGiftsItemLayout itemLayout1, LeftGiftsItemLayout itemLayout2) {
        mFirstItemGift = itemLayout1;
        mSecondItemGift = itemLayout2;

        mFirstItemGift.setIndex(0);
        mSecondItemGift.setIndex(1);

        mFirstItemGift.setGiftAnimationListener(this);
        mSecondItemGift.setGiftAnimationListener(this);
    }

    @Override
    public void dismiss(int index) {
        if (index == 0) {
            mFirstItemGift.startAnimation(mGiftExitAnimation1);
        } else if (index == 1) {
            mSecondItemGift.startAnimation(mGiftExitAnimation2);
        }
    }

    /**
     * 礼物退出动画监听
     */
    private class GiftExitAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mSecondItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.SHOWEND) {
                mSecondItemGift.setVisibility(View.INVISIBLE);
                mSecondItemGift.setCurrentShowStatus(LeftGiftsItemLayout.WAITING);
            }

            if (mFirstItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.SHOWEND) {
                mFirstItemGift.setVisibility(View.INVISIBLE);
                mFirstItemGift.setCurrentShowStatus(LeftGiftsItemLayout.WAITING);
            }

            showGift();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * 加入礼物
     *
     * @param data
     */
    public synchronized void loadGift(GiftModel data) {
        if (mGiftQueue != null) {

            if (mSecondItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.SHOWING) {
                if (mSecondItemGift.getCurrentGiftId().equals(data.getGiftId()) &&
                        mSecondItemGift.getCurrentSendUserId().equals(data.getSendUserId())) {
                    mSecondItemGift.setGiftCount(data.getGiftCuont());
                    return;

                }
            }

            if (mFirstItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.SHOWING) {
                if (mFirstItemGift.getCurrentGiftId().equals(data.getGiftId()) && mFirstItemGift.getCurrentSendUserId().equals(data.getSendUserId())) {
                    mFirstItemGift.setGiftCount(data.getGiftCuont());
                    return;
                }

            }
            addGiftQueue(data);
        }
    }

    private void addGiftQueue(GiftModel data) {
        if (mGiftQueue != null) {

            if (mGiftQueue.size() == 0) {
                mGiftQueue.add(data);
                showGift();
                return;
            }

            for (GiftModel gift : mGiftQueue) {
                if (gift.getGiftId().equals(data.getGiftId()) && gift.getSendUserId().equals(data.getSendUserId())) {
                    gift.setGiftCuont(gift.getGiftCuont() + data.getGiftCuont());
                } else {
                    mGiftQueue.add(data);
                }
            }
            showGift();
        }
    }

    /**
     * 显示礼物
     */
    public synchronized void showGift() {

        if (isEmpty()) {
            return;
        }
        if (mFirstItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.WAITING) {
            mFirstItemGift.setData(getGift());
            mFirstItemGift.setVisibility(View.VISIBLE);
            mFirstItemGift.startAnimation(mGiftEnterAnimation2);
            mFirstItemGift.startGiftAnimation();
        } else if (mSecondItemGift.getCurrentShowStatus() == LeftGiftsItemLayout.WAITING) {
            mSecondItemGift.setData(getGift());
            mSecondItemGift.setVisibility(View.VISIBLE);
            mSecondItemGift.startAnimation(mGiftEnterAnimation1);
            mSecondItemGift.startGiftAnimation();
        }
    }

    /**
     * 取出礼物
     *
     * @return
     */
    private synchronized GiftModel getGift() {
        GiftModel gift = null;
        if (mGiftQueue.size() != 0) {
            gift = mGiftQueue.get(0);
            mGiftQueue.remove(0);
        }
        return gift;
    }

    /**
     * 清除所有礼物
     */
    public synchronized void cleanAll() {
        if (mGiftQueue != null) {
            mGiftQueue.clear();
        }
        mSecondItemGift.setCurrentShowStatus(LeftGiftsItemLayout.SHOWEND);
        mFirstItemGift.setCurrentShowStatus(LeftGiftsItemLayout.SHOWEND);
    }

    /**
     * 礼物是否为空
     *
     * @return
     */
    public synchronized boolean isEmpty() {
        if (mGiftQueue == null || mGiftQueue.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

}
