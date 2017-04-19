package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.Log;

import org.dync.giftlibrary.util.ThreadUtil;

import java.util.ArrayList;

/**
 * Created by KathLine on 2017/1/8.
 */

public class GiftControl implements GiftFrameLayout.LeftGiftAnimationStatusListener {

    private static final String TAG = "GiftControl";
    /**
     * 礼物队列(在多个线程中使用此List)
     */
    private ArrayList<GiftModel> mGiftQueue;

    /**
     * 礼物1
     */
    private GiftFrameLayout mFirstItemGift;

    /**
     * 礼物2
     */
    private GiftFrameLayout mSecondItemGift;

    public GiftControl(Context context) {
        mGiftQueue = new ArrayList<>();
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

    public void loadGift(GiftModel gift) {
        loadGift(gift, true);
    }

    /**
     * 加入礼物，具有实时连击效果
     *
     * @param gift
     * @param supportCombo 是否支持实时连击，如果为true：支持，否则不支持
     */
    public void loadGift(GiftModel gift, boolean supportCombo) {
        if (mGiftQueue != null) {
            if (supportCombo) {
                if (mFirstItemGift.isShowing()) {
                    if (mFirstItemGift.getCurrentGiftId().equals(gift.getGiftId()) && mFirstItemGift.getCurrentSendUserId().equals(gift.getSendUserId())) {
                        //连击
                        Log.i(TAG, "addGiftQueue: ========mFirstItemGift连击========礼物：" + gift.getGiftId() + ",连击X" + gift.getGiftCuont());
                        mFirstItemGift.setGiftCount(gift.getGiftCuont());
                        mFirstItemGift.setSendGiftTime(gift.getSendGiftTime());
                        return;
                    }
                }

                if (mSecondItemGift.isShowing()) {
                    if (mSecondItemGift.getCurrentGiftId().equals(gift.getGiftId()) && mSecondItemGift.getCurrentSendUserId().equals(gift.getSendUserId())) {
                        //连击
                        Log.i(TAG, "addGiftQueue: ========mSecondItemGift连击========礼物：" + gift.getGiftId() + ",连击X" + gift.getGiftCuont());
                        mSecondItemGift.setGiftCount(gift.getGiftCuont());
                        mSecondItemGift.setSendGiftTime(gift.getSendGiftTime());
                        return;
                    }
                }
            }

            addGiftQueue(gift, supportCombo);
        }
    }

    private void addGiftQueue(final GiftModel gift, final boolean supportCombo) {
        if (mGiftQueue != null) {
            if (mGiftQueue.size() == 0) {
                Log.d(TAG, "addGiftQueue---集合个数：" + mGiftQueue.size() + ",礼物：" + gift.getGiftId());
                mGiftQueue.add(gift);
                showGift();
                return;
            }
        }
        Log.d(TAG, "addGiftQueue---集合个数：" + mGiftQueue.size() + ",礼物：" + gift.getGiftId());
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                if (supportCombo) {
                    boolean addflag = false;
                    for (GiftModel model : mGiftQueue) {
                        if (model.getGiftId().equals(gift.getGiftId()) && model.getSendUserId().equals(gift.getSendUserId())) {
                            Log.d(TAG, "addGiftQueue: ========已有集合========" + gift.getGiftId() + ",礼物数：" + gift.getGiftCuont());
                            model.setGiftCuont(model.getGiftCuont() + gift.getGiftCuont());
                            addflag = true;
                            break;
                        }
                    }
                    //如果在现有的集合中不存在同一人发的礼物就加入到现有集合中
                    if (!addflag) {
                        Log.d(TAG, "addGiftQueue: --------新的集合--------" + gift.getGiftId() + ",礼物数：" + gift.getGiftCuont());
                        mGiftQueue.add(gift);
                    }
                } else {
                    mGiftQueue.add(gift);
                }
            }
        });

    }

    /**
     * 显示礼物
     */
    public synchronized void showGift() {
        if (isEmpty()) {
            return;
        }
        Log.d(TAG, "showGift: begin->集合个数：" + mGiftQueue.size());
        if (!mFirstItemGift.isShowing() && mFirstItemGift.isEnd()) {
            boolean hasGift = mFirstItemGift.setGift(getGift());
            if (hasGift) {
                mFirstItemGift.startAnimation();
            }
        }
        if (!mSecondItemGift.isShowing() && mSecondItemGift.isEnd()) {
            boolean hasGift = mSecondItemGift.setGift(getGift());
            if (hasGift) {
                mSecondItemGift.startAnimation();
            }
        }
        Log.d(TAG, "showGift: end->集合个数：" + mGiftQueue.size());
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
            Log.i(TAG, "getGift---集合个数：" + mGiftQueue.size() + ",送出礼物---" + gift.getGiftId() + ",礼物数X" + gift.getGiftCuont());
        }
        return gift;
    }

    public int getCurGiftCount(String giftId, String userName) {
        int curGiftCount = -1;
        GiftModel firstGift = mFirstItemGift.getGift();
        GiftModel secondGift = mSecondItemGift.getGift();
        if (firstGift != null && firstGift.getGiftId().equals(giftId) && firstGift.getSendUserName().equals(userName)) {
            curGiftCount = mFirstItemGift.getGiftCount();
        } else if (secondGift != null && secondGift.getGiftId().equals(giftId) && secondGift.getSendUserName().equals(userName)) {
            curGiftCount = mSecondItemGift.getGiftCount();
        }
        return curGiftCount;
    }

    @Override
    public void dismiss(int index) {
        if (index == 0) {
            reStartAnimation(mFirstItemGift, index);
        } else if (index == 1) {
            reStartAnimation(mSecondItemGift, index);
        }
    }

    private void reStartAnimation(final GiftFrameLayout giftFrameLayout, final int index) {
        //动画结束，这时不能触发连击动画
        giftFrameLayout.setCurrentShowStatus(false);
        Log.d(TAG, "reStartAnimation: 动画结束");
        AnimatorSet animatorSet = giftFrameLayout.endAnmation();
        if (animatorSet != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.i(TAG, "礼物动画dismiss: index = " + index);
                    //动画完全结束
                    giftFrameLayout.CurrentEndStatus(true);
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
        if (mFirstItemGift != null) {
            mFirstItemGift.clearHandler();
            mFirstItemGift.stopCheckGiftCount();
        }
        if (mSecondItemGift != null) {
            mSecondItemGift.clearHandler();
            mSecondItemGift.stopCheckGiftCount();
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
