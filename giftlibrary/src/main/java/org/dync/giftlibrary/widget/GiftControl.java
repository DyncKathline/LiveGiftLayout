package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by KathLine on 2017/1/8.
 */

public class GiftControl implements GiftFrameLayout.LeftGiftAnimationStatusListener {

    private static final String TAG = "GiftControl";
    /**
     * 礼物队列(在多个线程中使用此List)
     */
    private CopyOnWriteArrayList<GiftModel> mGiftQueue;

    /**
     * 礼物1
     */
    private GiftFrameLayout mFirstItemGift;

    /**
     * 礼物2
     */
    private GiftFrameLayout mSecondItemGift;

    public GiftControl(Context context) {
        mGiftQueue = new CopyOnWriteArrayList<>();
    }

    public void setGiftLayout(GiftFrameLayout giftFrameLayout1, GiftFrameLayout giftFrameLayout2) {
        mFirstItemGift = giftFrameLayout1;
        mSecondItemGift = giftFrameLayout2;

        mFirstItemGift.setIndex(0);
        mSecondItemGift.setIndex(1);

        mFirstItemGift.firstHideLayout();
        mSecondItemGift.firstHideLayout();

        mFirstItemGift.setGiftAnimationListener(this);
        mSecondItemGift.setGiftAnimationListener(this);
    }

    public void loadGift(GiftModel gift){
        loadGift(gift, false);
    }
    /**
     * 加入礼物，具有实时连击效果
     *
     * @param gift
     * @param supportCombo 是否支持实时连击，如果为true：支持，否则不支持
     */
    public void loadGift(GiftModel gift, boolean supportCombo) {
        if (mGiftQueue != null) {
            if (supportCombo){
                if (mFirstItemGift.isShowing()) {
                    if (mFirstItemGift.getCurrentGiftId().equals(gift.getGiftId()) && mFirstItemGift.getCurrentSendUserId().equals(gift.getSendUserId())) {
                        //连击
                        mFirstItemGift.setGiftCount(gift.getGiftCuont());
                        return;
                    }
                }

                if (mSecondItemGift.isShowing()) {
                    if (mSecondItemGift.getCurrentGiftId().equals(gift.getGiftId()) && mSecondItemGift.getCurrentSendUserId().equals(gift.getSendUserId())) {
                        //连击
                        mSecondItemGift.setGiftCount(gift.getGiftCuont());
                        return;
                    }
                }
            }

            addGiftQueue(gift, supportCombo);
        }
    }

    private void addGiftQueue(GiftModel gift, boolean supportCombo) {
        if (mGiftQueue != null) {
            if (mGiftQueue.size() == 0) {
                mGiftQueue.add(gift);
                showGift();
                return;
            }
        }
        Log.i(TAG, "addGiftQueue: " + mGiftQueue.size());
        if (supportCombo){
            for (GiftModel model : mGiftQueue) {
                if (model.getGiftId().equals(gift.getGiftId()) && model.getSendUserId().equals(gift.getSendUserId())) {
                    Log.i(TAG, "addGiftQueue: ===============");
                    model.setGiftCuont(model.getGiftCuont() + gift.getGiftCuont());
                } else {
                    Log.i(TAG, "addGiftQueue: ---------------");
                    mGiftQueue.add(gift);
                }
            }
        }else {
            mGiftQueue.add(gift);
        }
        showGift();
    }

    /**
     * 显示礼物
     */
    public synchronized void showGift() {
        if (isEmpty()) {
            return;
        }
        Log.i(TAG, "showGift: begin->" + mGiftQueue.size());
        if (!mFirstItemGift.isShowing()) {
            mFirstItemGift.setGift(getGift());
            mFirstItemGift.startAnimation();
        } else if (!mSecondItemGift.isShowing()) {
            mSecondItemGift.setGift(getGift());
            mSecondItemGift.startAnimation();
        }
        Log.i(TAG, "showGift: end->" + mGiftQueue.size());
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
            Log.i(TAG, "getGift: " + mGiftQueue.size());
        }
        return gift;
    }

    public int getCurGiftCount(String giftId, String userName) {
        int curGiftCount = -1;
        GiftModel firstGift = mFirstItemGift.mGift;
        GiftModel secondGift = mSecondItemGift.mGift;
        if (firstGift != null && firstGift.getGiftId().equals(giftId) && firstGift.getSendUserName().equals(userName)) {
            curGiftCount = mFirstItemGift.getGiftCount();
        } else if (secondGift != null && secondGift.getGiftId().equals(giftId) && secondGift.getSendUserName().equals(userName)) {
            curGiftCount = mSecondItemGift.getGiftCount();
        }
        return curGiftCount;
    }

    @Override
    public void dismiss(int index) {
        Log.i(TAG, "dismiss: index = " + index);
        if (index == 0) {
            reStartAnimation(mFirstItemGift.endAnmation(), mFirstItemGift);
        } else if (index == 1) {
            reStartAnimation(mSecondItemGift.endAnmation(), mSecondItemGift);
        }
    }

    private void reStartAnimation(AnimatorSet animatorSet, final GiftFrameLayout giftFrameLayout) {
        if (animatorSet != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    giftFrameLayout.setCurrentShowStatus(false);
                    showGift();
                }
            });
        }
    }

    /**
     * 清除所有礼物
     */
    public synchronized void cleanAll() {
        if (mGiftQueue != null) {
            mGiftQueue.clear();
        }
        if (mFirstItemGift != null){
            mFirstItemGift.clearHandler();
        }
        if (mSecondItemGift != null){
            mSecondItemGift.clearHandler();
        }
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
