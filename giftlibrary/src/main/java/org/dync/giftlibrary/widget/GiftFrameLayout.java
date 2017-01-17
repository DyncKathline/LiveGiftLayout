package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dync.giftlibrary.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by KathLine on 2017/1/8.
 */
public class GiftFrameLayout extends FrameLayout implements Handler.Callback {

    private static final String TAG = "GiftFrameLayout";
    private LayoutInflater mInflater;
    private Handler mHandler = new Handler(this);
    private static final int RESTART_GIFT_ANIMATION_CODE = 1002;
    /**
     * 礼物展示时间
     */
    public static final int GIFT_DISMISS_TIME = 3000;
    /**
     * 当前动画runnable
     */
    private Runnable mCurrentAnimRunnable;

    RelativeLayout anim_rl;
    ImageView anim_gift, anim_light, anim_header;
    TextView anim_nickname, anim_sign;
    StrokeTextView anim_num;

    GiftModel mGift;
    /**
     * item 显示位置
     */
    private int mIndex = 1;
    /**
     * 礼物连击数
     */
    private int mGiftCount;
    /**
     * 当前播放连击数
     */
    private int mCombo = 1;
    /**
     * 实时监测礼物数量
     */
    private Subscription mSubscribe;
    private boolean isShowing = false;
    private LeftGiftAnimationStatusListener mGiftAnimationListener;

    public GiftFrameLayout(Context context) {
        this(context, null);
    }

    public GiftFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        View view = mInflater.inflate(R.layout.item_gift, null);
        anim_rl = (RelativeLayout) view.findViewById(R.id.infoRl);
        anim_gift = (ImageView) view.findViewById(R.id.giftIv);
        anim_light = (ImageView) view.findViewById(R.id.light);
        anim_num = (StrokeTextView) view.findViewById(R.id.animation_num);
        anim_header = (ImageView) view.findViewById(R.id.headIv);
        anim_nickname = (TextView) view.findViewById(R.id.nickNameTv);
        anim_sign = (TextView) view.findViewById(R.id.infoTv);
        this.addView(view);
    }

    public ImageView getAnimGift() {
        return anim_gift;
    }

    public void firstHideLayout() {
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(GiftFrameLayout.this, alpha);
        animator.setStartDelay(0);
        animator.setDuration(0);
        animator.start();
    }

    public void hideView() {
        anim_gift.setVisibility(INVISIBLE);
        anim_light.setVisibility(INVISIBLE);
        anim_num.setVisibility(INVISIBLE);
    }

    public void setGift(GiftModel gift) {
        if (gift == null) {
            return;
        }
        mGift = gift;
        if (0 != gift.getGiftCuont()) {
            this.mGiftCount = gift.getGiftCuont();
        }
        if (!TextUtils.isEmpty(gift.getSendUserName())) {
            anim_nickname.setText(gift.getSendUserName());
        }
        if (!TextUtils.isEmpty(gift.getGiftId())) {
            anim_sign.setText(gift.getGiftId());
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case RESTART_GIFT_ANIMATION_CODE:
                mCombo++;
                anim_num.setText("x " + (mCombo));
                comboAnimation();
                removeDismissGiftCallback();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 显示完连击数与动画时,关闭此Item Layout,并通知外部隐藏自身(供内部调用)
     */
    private void dismissGiftLayout() {
        if (mGiftAnimationListener != null) {
            mGiftAnimationListener.dismiss(mIndex);
        }
        removeDismissGiftCallback();
    }

    private void removeDismissGiftCallback() {
        stopCheckGiftCount();
        if (mCurrentAnimRunnable != null) {
            mHandler.removeCallbacks(mCurrentAnimRunnable);
            mCurrentAnimRunnable = null;
        }
    }

    private class GiftNumAnimaRunnable implements Runnable {

        @Override
        public void run() {
            dismissGiftLayout();
        }
    }

    /**
     * 设置item显示位置
     *
     * @param mIndex
     */
    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    /**
     * 获取ite显示位置
     *
     * @return
     */
    public int getIndex() {
        Log.i(TAG, "index : " + mIndex);
        return mIndex;
    }

    public interface LeftGiftAnimationStatusListener {
        void dismiss(int index);
    }

    public void setGiftAnimationListener(LeftGiftAnimationStatusListener giftAnimationListener) {
        this.mGiftAnimationListener = giftAnimationListener;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setCurrentShowStatus(boolean status) {
        mCombo = 1;
        isShowing = status;
    }

    /**
     * 获取当前显示礼物发送人id
     *
     * @return
     */
    public String getCurrentSendUserId() {
        if (mGift != null) {
            return mGift.getSendUserId();
        }
        return null;
    }

    /**
     * 获取当前显示礼物id
     *
     * @return
     */
    public String getCurrentGiftId() {
        if (mGift != null) {
            return mGift.getGiftId();
        }
        return null;
    }

    /**
     * 增加礼物数量,用于连击效果
     *
     * @param count
     */
    public synchronized void setGiftCount(int count) {
        mGiftCount += count;
    }

    public int getGiftCount(){
        return mGiftCount;
    }

    private void checkGiftCountSubscribe() {
        if (mSubscribe == null || mSubscribe.isUnsubscribed()) {
            mSubscribe = Observable.interval(300, TimeUnit.MILLISECONDS).map(new Func1<Long, Void>() {
                @Override
                public Void call(Long aLong) {
                    return null;
                }
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            if (mGiftCount > mCombo) {
                                mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
                            }
                        }
                    });

        }
    }

    private void stopCheckGiftCount() {
        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
            mSubscribe.unsubscribe();
        }
    }

    public void clearHandler(){
        mHandler.removeCallbacksAndMessages(null);
    }

    public AnimatorSet startAnimation() {
        hideView();
        //布局飞入
        ObjectAnimator flyFromLtoR = GiftAnimationUtil.createFlyFromLtoR(anim_rl, -getWidth(), 0, 400, new OvershootInterpolator());
        flyFromLtoR.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                GiftFrameLayout.this.setVisibility(View.VISIBLE);
                GiftFrameLayout.this.setAlpha(1f);
                isShowing = true;
                anim_num.setText("x " + mCombo);
            }
        });
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContext().getAssets().open(mGift.getGiftId()));

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        anim_gift.setImageDrawable(new BitmapDrawable(bitmap));
//        anim_gift.setImageBitmap(bitmap);

        //礼物飞入
        ObjectAnimator flyFromLtoR2 = GiftAnimationUtil.createFlyFromLtoR(anim_gift, -getWidth(), 0, 400, new DecelerateInterpolator());
        flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                anim_gift.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                GiftAnimationUtil.startAnimationDrawable(anim_light);
                anim_num.setVisibility(View.VISIBLE);
                comboAnimation();
            }
        });
        AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(flyFromLtoR, flyFromLtoR2);

        return animatorSet;
    }

    public void comboAnimation() {
        //数量增加
        ObjectAnimator scaleGiftNum = GiftAnimationUtil.scaleGiftNum(anim_num);
        scaleGiftNum.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                anim_num.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mGiftCount > mCombo) {//连击
                    mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
                } else {
                    checkGiftCountSubscribe();
                    mCurrentAnimRunnable = new GiftNumAnimaRunnable();
                    mHandler.postDelayed(mCurrentAnimRunnable, GIFT_DISMISS_TIME);
                }
            }
        });
        scaleGiftNum.start();
    }

    public AnimatorSet endAnmation() {
        //向上渐变消失
        ObjectAnimator fadeAnimator = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 0, -100, 300, 400);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                anim_num.setVisibility(View.INVISIBLE);
                GiftFrameLayout.this.setVisibility(View.INVISIBLE);
            }
        });
        // 复原
        ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 100, 0, 20, 0);

        AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
        return animatorSet;
    }

}
