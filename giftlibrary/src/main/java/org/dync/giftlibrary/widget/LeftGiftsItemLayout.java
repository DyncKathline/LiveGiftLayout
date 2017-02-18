package org.dync.giftlibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dync.giftlibrary.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KathLine on 2017/1/8.
 */
public class LeftGiftsItemLayout extends LinearLayout implements View.OnClickListener, Handler.Callback {

    private static final String TAG = "LeftGiftsItem";

    private ImageView mHeadIv;

    private TextView mNickNameTv;

    private TextView mInfoTv;

    private ImageView mGiftsIv;

    private volatile GiftModel mGift;

    private NumberTextView mNumberImgTv;

    /**
     * 等待中
     */
    public static final int WAITING = 0;

    /**
     * 正在显示
     */
    public static final int SHOWING = 1;

    /**
     * 显示结束或者未显示
     */
    public static final int SHOWEND = 2;

    /**
     * 礼物展示时间
     */
    public static final int GIFT_DISMISS_TIME = 3000;
    private static final int INTERVAL = 299;

    /**
     * 当前该layout显示状态
     */
    private int mCurrentShowStatus = WAITING;

    /**
     * item 显示位置
     */
    private int mIndex = 1;

    /**
     * 礼物平移动画
     */
    private TranslateAnimation mGiftAnimation;

    /**
     * 连击动画
     */
    private ScaleAnimation mGiftNumAnim;//修改礼物数量的动画

    private LeftGiftAnimationStatusListener mGiftAnimationListener;

    private Handler mHandler = new Handler(this);

    /**
     * 礼物连击数
     */
    private int mGiftCount;

    /**
     * 当前动画runnable
     */
    private Runnable mCurrentAnimRunnable;

    /**
     * 当前播放连击数
     */
    private int mCombo = 0;

    /**
     * 实时监测礼物数量
     */
//    private Subscription mSubscribe;
    private Timer mTimer;

    private RelativeLayout mInfoLl;

    public LeftGiftsItemLayout(Context context) {
        super(context);
        initLayout(context, null);
    }

    public LeftGiftsItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context, attrs);
    }

    private void initLayout(Context context, AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.left_gift_item_layout, this);
        mInfoLl = (RelativeLayout) findViewById(R.id.infoRl);
        mHeadIv = (ImageView) findViewById(R.id.headIv);
        mNickNameTv = (TextView) findViewById(R.id.nickNameTv);
        mInfoTv = (TextView) findViewById(R.id.infoTv);
        mGiftsIv = (ImageView) findViewById(R.id.giftIv);
        mNumberImgTv = (NumberTextView) findViewById(R.id.numberImgTv);

        if (attrs != null) {
            final TypedArray typedArray = context.obtainStyledAttributes(
                    attrs, R.styleable.LeftGiftsItemLayout, 0, 0);
            mIndex = typedArray.getInteger(R.styleable.LeftGiftsItemLayout_left_gift_layout_index, 1);
        }

        mHeadIv.setOnClickListener(this);

        mGiftAnimation = new TranslateAnimation(-200, 0, 0, 0);
        mGiftAnimation.setDuration(800);
        mGiftAnimation.setFillAfter(true);
        mGiftAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNumberImgTv.startAnimation(mGiftNumAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mGiftNumAnim = (ScaleAnimation) AnimationUtils.loadAnimation(context, R.anim.gift_num);
        mGiftNumAnim.setAnimationListener(new GiftAnimationListener());
    }

    /**
     * 设置数据
     *
     * @param data
     */
    public void setData(GiftModel data) {
        this.mGift = data;
        mGiftCount = this.mGift.getGiftCuont();
        mCombo = mGiftCount;
        mNickNameTv.setText(this.mGift.getSendUserName());
        mInfoTv.setText("送了一个" + this.mGift.getGiftName());

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContext().getAssets().open(mGift.getGiftId()));

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        mGiftsIv.setImageDrawable(new BitmapDrawable(bitmap));
        replaceNumberText(mCombo);
    }

    /**
     * 开始动画
     */
    public void startGiftAnimation() {
        mGiftsIv.startAnimation(mGiftAnimation);
        mCurrentShowStatus = SHOWING;
    }

    private class GiftNumAnimaRunnable implements Runnable {

        @Override
        public void run() {
            dismissGiftLayout();
        }
    }

    /**
     * 显示完连击数与动画时,关闭此Item Layout,并通知外部隐藏自身(供内部调用)
     */
    private void dismissGiftLayout() {
        mCurrentShowStatus = SHOWEND;
        mGiftsIv.setVisibility(View.INVISIBLE);
        if (mGiftAnimationListener != null) {
            mGiftAnimationListener.dismiss(mIndex);
        }
        removeDismissGiftCallback();
    }

    private void removeDismissGiftCallback() {
        if (mCurrentAnimRunnable != null) {
            mHandler.removeCallbacks(mCurrentAnimRunnable);
            mCurrentAnimRunnable = null;
            stopCheckGiftCount();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case RESTART_GIFT_ANIMATION_CODE:
                mCombo++;
                replaceNumberText(mCombo);
                mNumberImgTv.startAnimation(mGiftNumAnim);
                stopCheckGiftCount();
                removeDismissGiftCallback();
                break;
            default:
                break;
        }
        return true;
    }

    private void replaceNumberText(int num) {
        mNumberImgTv.updataNumber(String.valueOf(num));
    }

    /**
     * 连击数动画监听
     */
    private class GiftAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            mGiftsIv.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mGiftCount > mCombo) {
                mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
            } else {
                checkGiftCountSubscribe();
                mCurrentAnimRunnable = new GiftNumAnimaRunnable();
                mHandler.postDelayed(mCurrentAnimRunnable, GIFT_DISMISS_TIME);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    public interface LeftGiftAnimationStatusListener {
        void dismiss(int index);
    }

    private static final int RESTART_GIFT_ANIMATION_CODE = 1002;

    private void checkGiftCountSubscribe() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mGiftCount > mCombo) {
                    mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
                }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(task, 0, INTERVAL);

//        if (mSubscribe == null || mSubscribe.isUnsubscribed()) {
//            mSubscribe = Observable.interval(INTERVAL, TimeUnit.MILLISECONDS).map(new Func1<Long, Void>() {
//                @Override
//                public Void call(Long aLong) {
//                    return null;
//                }
//            }).subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Action1<Void>() {
//                        @Override
//                        public void call(Void aVoid) {
//                            if (mGiftCount > mCombo) {
//                                mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
//                            }
//                        }
//                    });
//
//        }
    }

    public void stopCheckGiftCount() {
        if (mTimer != null) {
            mTimer.cancel();
        }
//        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
//            mSubscribe.unsubscribe();
//        }
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

    /**
     * 获取该layout当前显示状态
     *
     * @return
     */
    public int getCurrentShowStatus() {
        return mCurrentShowStatus;
    }

    public void setCurrentShowStatus(int status) {
        this.mCurrentShowStatus = status;
    }

    public void setGiftAnimationListener(LeftGiftAnimationStatusListener giftAnimationListener) {
        this.mGiftAnimationListener = giftAnimationListener;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "点击", Toast.LENGTH_SHORT).show();
    }
}