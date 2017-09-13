package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by KathLine on 2017/1/8.
 */

public class GiftControl implements GiftFrameLayout.LeftGiftAnimationStatusListener {

    private static final String TAG = "GiftControl";
    protected Context mContext;
    /**
     * 自定义动画
     */
    private ICustormAnim custormAnim;
    /**
     * 礼物队列
     */
    private ArrayList<GiftModel> mGiftQueue;

    /**
     * 礼物数量集合
     */
    private SparseArray<GiftFrameLayout> mGiftLayoutList;
    /**
     * 添加礼物布局的父容器
     */
    private LinearLayout mGiftLayoutParent;

    public GiftControl(Context context) {
        mContext = context;
        mGiftQueue = new ArrayList<>();
    }

    public GiftControl setCustormAnim(ICustormAnim anim){
        custormAnim = anim;

        return this;
    }

    /**
     *
     * @param isHideMode        是否开启隐藏动画
     * @param giftLayoutParent  存放礼物控件的父容器
     * @param giftLayoutNums    礼物控件的数量
     * @return
     */
    public GiftControl setGiftLayout(boolean isHideMode, LinearLayout giftLayoutParent, @NonNull int giftLayoutNums){
        if(giftLayoutNums <= 0){
            throw new IllegalArgumentException("GiftFrameLayout数量必须大于0");
        }
        if(giftLayoutParent.getChildCount() > 0){//如果父容器没有子孩子，就进行添加
            return this;
        }
        mGiftLayoutParent = giftLayoutParent;

        final SparseArray<GiftFrameLayout> giftLayoutList = new SparseArray<>();
        for (int i = 0; i < giftLayoutNums; i++) {
            giftLayoutList.append(i, new GiftFrameLayout(mContext));
        }

        mGiftLayoutList = giftLayoutList;
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);

            giftLayoutParent.addView(giftFrameLayout);

            giftFrameLayout.setIndex(i);
            giftFrameLayout.firstHideLayout();
            giftFrameLayout.setGiftAnimationListener(this);
            giftFrameLayout.setHideMode(isHideMode);
        }
        return this;
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
                GiftFrameLayout giftFrameLayout;
                for (int i = 0; i < mGiftLayoutList.size(); i++) {
                    giftFrameLayout = mGiftLayoutList.get(i);
                    if(giftFrameLayout.isShowing()){
                        if (giftFrameLayout.getCurrentGiftId().equals(gift.getGiftId()) && giftFrameLayout.getCurrentSendUserId().equals(gift.getSendUserId())) {
                            //连击
                            Log.i(TAG, "addGiftQueue: ========giftFrameLayout("+ giftFrameLayout.getIndex()+")连击========礼物：" + gift.getGiftId() + ",连击X" + gift.getGiftCount());
                            if(gift.getJumpCombo() > 0){
                                //触发连击，这里不要在加上giftCount了，因为你要连击的数已经变成jumpCombo了
                                giftFrameLayout.setGiftCount(gift.getJumpCombo());
                            }else {
                                giftFrameLayout.setGiftCount(gift.getGiftCount());
                            }
                            giftFrameLayout.setSendGiftTime(gift.getSendGiftTime());
                            return;
                        }
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
        if (supportCombo) {
            boolean addflag = false;
            for (GiftModel model : mGiftQueue) {
                if (model.getGiftId().equals(gift.getGiftId()) && model.getSendUserId().equals(gift.getSendUserId())) {
                    Log.d(TAG, "addGiftQueue: ========已有集合========" + gift.getGiftId() + ",礼物数：" + gift.getGiftCount());
                    model.setGiftCount(model.getGiftCount() + gift.getGiftCount());
                    addflag = true;
                    break;
                }
            }
            //如果在现有的集合中不存在同一人发的礼物就加入到现有集合中
            if (!addflag) {
                Log.d(TAG, "addGiftQueue: --------新的集合--------" + gift.getGiftId() + ",礼物数：" + gift.getGiftCount());
                mGiftQueue.add(gift);
            }
        } else {
            mGiftQueue.add(gift);
        }

    }

    /**
     * 显示礼物
     */
    public synchronized void showGift() {
        if (isEmpty()) {
            return;
        }
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            Log.d(TAG, "showGift: begin->集合个数：" + mGiftQueue.size());
            if (!giftFrameLayout.isShowing() && giftFrameLayout.isEnd()) {
                boolean hasGift = giftFrameLayout.setGift(getGift());
                if (hasGift) {
                    giftFrameLayout.startAnimation(custormAnim);
                }
            }
            Log.d(TAG, "showGift: end->集合个数：" + mGiftQueue.size());
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
            Log.i(TAG, "getGift---集合个数：" + mGiftQueue.size() + ",送出礼物---" + gift.getGiftId() + ",礼物数X" + gift.getGiftCount());
        }
        return gift;
    }

    /**
     * 通过获取giftId和getSendUserId当前用户giftId礼物总数
     * @param giftId
     * @param userId
     * @return
     */
    public int getCurGiftCountByUserId(String giftId, String userId) {
        int curGiftCount = 0;
        GiftFrameLayout giftFrameLayout;
        GiftModel giftModel;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            giftModel = giftFrameLayout.getGift();
            if (giftModel != null && giftModel.getGiftId().equals(giftId) && giftModel.getSendUserId().equals(userId)) {
                curGiftCount = giftModel.getGiftCount();
            } else {//自己的礼物不正在显示，还在队列中
                Iterator<GiftModel> iterator = mGiftQueue.iterator();
                while (iterator.hasNext()){
                    giftModel = iterator.next();
                    if (giftModel.getGiftId().equals(giftId) && giftModel.getSendUserId().equals(userId)){
                        curGiftCount = giftModel.getGiftCount();
                        break;
                    }
                }
            }
        }
        return curGiftCount;
    }

    /**
     * 获取正在展示礼物的个数（即GiftFragmeLayout展示的个数）
     * @return
     */
    public int getShowingGiftLayoutCount(){
        int count = 0;
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            if(giftFrameLayout.isShowing()){
                count++;
            }
        }
        return count;
    }

    /**
     * 获取正在展示礼物的个数实例（即GiftFragmeLayout展示的个数实例）
     * @return
     */
    public List<GiftFrameLayout> getShowingGiftLayouts(){
        List<GiftFrameLayout> giftLayoutList = new ArrayList<>();
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            if(giftFrameLayout.isShowing()){
                giftLayoutList.add(giftFrameLayout);
            }
        }
        return giftLayoutList;
    }

    @Override
    public void dismiss(int index) {
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            if(giftFrameLayout.getIndex() == index){
                reStartAnimation(giftFrameLayout, giftFrameLayout.getIndex());
            }
        }
    }

    private void reStartAnimation(final GiftFrameLayout giftFrameLayout, final int index) {
        //动画结束，这时不能触发连击动画
        giftFrameLayout.setCurrentShowStatus(false);
        Log.d(TAG, "reStartAnimation: 动画结束");
        AnimatorSet animatorSet = giftFrameLayout.endAnmation(custormAnim);
        if (animatorSet != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.i(TAG, "礼物动画dismiss: index = " + index);
                    //动画完全结束
                    giftFrameLayout.CurrentEndStatus(true);
                    giftFrameLayout.setGiftViewEndVisibility(isEmpty());
                    showGift();
                }
            });
        }
    }

    public GiftControl reSetGiftLayout(boolean isHideMode, LinearLayout giftLayoutParent,  @NonNull int giftLayoutNums){
        return setGiftLayout(isHideMode, giftLayoutParent, giftLayoutNums);
    }

    /**
     * 清除所有礼物
     */
    public synchronized void cleanAll() {
        if (mGiftQueue != null) {
            mGiftQueue.clear();
        }
        mGiftLayoutParent.removeAllViews();
        GiftFrameLayout giftFrameLayout;
        for (int i = 0; i < mGiftLayoutList.size(); i++) {
            giftFrameLayout = mGiftLayoutList.get(i);
            if(giftFrameLayout != null){
                giftFrameLayout.clearHandler();
                giftFrameLayout.firstHideLayout();
            }
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
