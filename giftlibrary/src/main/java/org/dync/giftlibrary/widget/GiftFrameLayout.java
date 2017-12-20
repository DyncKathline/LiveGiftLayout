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

import org.dync.giftlibrary.GiftControl;
import org.dync.giftlibrary.R;
import org.dync.giftlibrary.util.GiftAnimationUtil;
import org.dync.giftlibrary.util.glide.GlideCircleTransform;
import org.dync.giftlibrary.util.glide.GlideLoader;

import java.io.IOException;


/**
 * Created by KathLine on 2017/1/8.
 */
public class GiftFrameLayout extends FrameLayout implements Handler.Callback {

    private static final String TAG = "GiftFrameLayout";
    private LayoutInflater mInflater;
    private Context mContext;
    private Handler mHandler = new Handler(this);//连击handler
    private Handler comboHandler = new Handler(this);//检查连击handler
    private Runnable runnable;
    /**
     * 实时监测礼物数量
     */
//    private Subscription mSubscribe;
//    private Timer mTimer;
    private static final int RESTART_GIFT_ANIMATION_CODE = 1002;
    /**
     * 礼物展示时间
     */
    public static final int GIFT_DISMISS_TIME = 3000;
    private static final int INTERVAL = 299;
    /**
     * 当前动画runnable
     */
    private Runnable mCurrentAnimRunnable;

    RelativeLayout anim_rl;
    ImageView anim_gift, anim_light, anim_header;
    TextView anim_nickname, anim_sign;
    StrokeTextView anim_num;

    private GiftModel mGift;
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
    private int mCombo = 0;
    /**
     * 跳到指定连击数，例如：从1直接显示3，这里的值就是2
     */
    private int mJumpCombo = 0;
    /**
     * 礼物动画正在显示，在这期间可触发连击效果
     */
    private boolean isShowing = false;
    /**
     * 礼物动画结束
     */
    private boolean isEnd = true;
    /**
     * 自定义动画的接口
     */
    private ICustormAnim anim;
    /**
     * 是否开启礼物动画隐藏模式（如果两个礼物动画同时显示，并且第一个优先结束，第二个礼物的位置会移动到第一个位置上去）
     */
    private boolean isHideMode = false;

    private LeftGiftAnimationStatusListener mGiftAnimationListener;
    private View rootView;

    public GiftFrameLayout(Context context) {
        this(context, null);
    }

    public GiftFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        initView();
    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        initView();
//    }

    private void initView() {
        rootView = mInflater.inflate(R.layout.item_gift, null);
        anim_rl = (RelativeLayout) rootView.findViewById(R.id.infoRl);
        anim_gift = (ImageView) rootView.findViewById(R.id.giftIv);
        anim_light = (ImageView) rootView.findViewById(R.id.light);
        anim_num = (StrokeTextView) rootView.findViewById(R.id.animation_num);
        anim_header = (ImageView) rootView.findViewById(R.id.headIv);
        anim_nickname = (TextView) rootView.findViewById(R.id.nickNameTv);
        anim_sign = (TextView) rootView.findViewById(R.id.infoTv);
        this.addView(rootView);
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

    public void setHideMode(boolean isHideMode) {
        this.isHideMode = isHideMode;
    }

    public void hideView() {
        anim_gift.setVisibility(INVISIBLE);
        anim_light.setVisibility(INVISIBLE);
        anim_num.setVisibility(INVISIBLE);
    }

    public void setGiftViewEndVisibility(boolean hasGift) {

        if (isHideMode && hasGift) {
            GiftFrameLayout.this.setVisibility(View.GONE);
        } else {
            GiftFrameLayout.this.setVisibility(View.INVISIBLE);
        }
    }

    public boolean setGift(GiftModel gift) {
        if (gift == null) {
            return false;
        }
        mGift = gift;

        if (mGift.isCurrentStart()) {
            mGiftCount = gift.getGiftCount() + mGift.getHitCombo();
        } else {
            mGiftCount = gift.getGiftCount();
        }
        mJumpCombo = mGift.getJumpCombo();
        if (!TextUtils.isEmpty(gift.getSendUserName())) {
            anim_nickname.setText(gift.getSendUserName());
        }
        if (!TextUtils.isEmpty(gift.getGiftId())) {
            anim_sign.setText(gift.getGiftName());
        }
        return true;
    }

    public GiftModel getGift() {
        return mGift;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case RESTART_GIFT_ANIMATION_CODE:
                if (mJumpCombo > 0) {
                    mCombo += mJumpCombo;
                } else {
                    ++mCombo;
                }
                anim_num.setText("x " + (mCombo));
                comboAnimation(false);
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
        removeDismissGiftCallback();
        if (mGiftAnimationListener != null) {
            mGiftAnimationListener.dismiss(this);
        }
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
        void dismiss(GiftFrameLayout giftFrameLayout);
    }

    public void setGiftAnimationListener(LeftGiftAnimationStatusListener giftAnimationListener) {
        this.mGiftAnimationListener = giftAnimationListener;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setCurrentShowStatus(boolean status) {
        mCombo = 0;
        isShowing = status;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void CurrentEndStatus(boolean isEnd) {
        this.isEnd = isEnd;
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
        Log.d(TAG, "setGiftCount: " + count);
        mGiftCount += count;
        mGift.setGiftCount(mGiftCount);
    }

    public int getGiftCount() {
        return mGiftCount;
    }

    public synchronized void setSendGiftTime(long sendGiftTime) {
        mGift.setSendGiftTime(sendGiftTime);
    }

    public long getSendGiftTime() {
        return mGift.getSendGiftTime();
    }

    public boolean isCurrentStart() {
        return mGift.isCurrentStart();
    }

    public void setCurrentStart(boolean currentStart) {
        mGift.setCurrentStart(currentStart);
    }

    public int getCombo() {
        return mCombo;
    }

    public int getJumpCombo() {
        return mJumpCombo;
    }

    /**
     * <pre>
     * 这里不能GIFT_DISMISS_TIME % INVISIBLE == 0,
     * 因为余数为0的话，说明在这个时刻即执行了{@link GiftControl#reStartAnimation(GiftFrameLayout, int)}移除动画{@link #endAnmation(ICustormAnim)},也执行了检查监听连击动作。
     * 这导致就会出现在礼物动画消失的一瞬间，点击连击会出现如下日志出现的情况（已经触发连击了，但是礼物的动画已经结束了）：
     * 02-18 20:45:57.900 9060-9060/org.dync.livegiftlayout D/GiftControl: addGiftQueue---集合个数：0,礼物：p/000.png
     * 02-18 20:45:57.900 9060-9060/org.dync.livegiftlayout D/GiftControl: showGift: begin->集合个数：1
     * 02-18 20:45:57.900 9060-9060/org.dync.livegiftlayout I/GiftControl: getGift---集合个数：0,送出礼物---p/000.png,礼物数X1
     * 02-18 20:45:57.910 9060-9060/org.dync.livegiftlayout D/GiftControl: showGift: end->集合个数：0
     * 02-18 20:46:01.910 9060-9060/org.dync.livegiftlayout I/GiftControl: addGiftQueue: ========mFirstItemGift连击========礼物：p/000.png,连击X1
     * 02-18 20:46:01.970 9060-9060/org.dync.livegiftlayout D/GiftControl: reStartAnimation: 动画结束
     * 02-18 20:46:02.490 9060-9060/org.dync.livegiftlayout I/GiftControl: 礼物动画dismiss: index = 0
     *
     * </pre>
     */
    private void checkGiftCountSubscribe() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mGiftCount > mCombo) {
                    mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
                }
                comboHandler.postDelayed(runnable, INTERVAL);
            }
        };
        comboHandler.postDelayed(runnable, INTERVAL);

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
        comboHandler.removeCallbacksAndMessages(null);

//        if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
//            mSubscribe.unsubscribe();
//        }
    }

    public void clearHandler() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;//这里要置位null，否则当前页面销毁时，正在执行的礼物动画会造成内存泄漏
        }

        mGiftAnimationListener = null;

        if (comboHandler != null) {
            comboHandler.removeCallbacksAndMessages(null);
            comboHandler = null;//这里要置位null，否则当前页面销毁时，正在执行的礼物动画会造成内存泄漏
        }
        resetGift();
    }

    public void resetGift() {
        runnable = null;
        mCurrentAnimRunnable = null;
        mGift = null;
        mIndex = -1;
        mGiftCount = 0;
        mCombo = 0;
        mJumpCombo = 0;
        isShowing = false;
        isEnd = true;
        isHideMode = false;
    }

    /**
     * 动画开始时回调，使用方法借鉴{@link #startAnimation}
     */
    public void initLayoutState() {
        this.setVisibility(View.VISIBLE);
        this.setAlpha(1f);
        isShowing = true;
        isEnd = false;

        if (mGift.getSendUserPic().equals("")) {
            GlideLoader.init()
                    .load(R.mipmap.icon)
                    .applyDefault()
                    .bitmapTransform(new GlideCircleTransform(mContext))
                    .into(anim_header);
        } else {
            GlideLoader.init()
                    .load(mGift.getSendUserPic())
                    .applyDefault()
                    .bitmapTransform(new GlideCircleTransform(mContext))
                    .into(anim_header);
        }
        if (mGift.isCurrentStart()) {
            mCombo = mGift.getHitCombo();
        }
        anim_num.setVisibility(INVISIBLE);
        anim_num.setText("x " + mCombo);

        if (!mGift.getGiftPic().equals("")) {
            GlideLoader.init()
                    .load(mGift.getGiftPic())
                    .applyDefault()
                    .bitmapTransform(new GlideCircleTransform(mContext))
                    .into(anim_gift);
        } else {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContext().getAssets().open(mGift.getGiftId()));

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            anim_gift.setImageDrawable(new BitmapDrawable(bitmap));
//        anim_gift.setImageBitmap(bitmap);
        }
    }

    /**
     * 连击结束时回调
     */
    public void comboEndAnim() {
        if (mHandler != null) {
            if (mGiftCount > mCombo) {//连击
                mHandler.sendEmptyMessage(RESTART_GIFT_ANIMATION_CODE);
            } else {
                mCurrentAnimRunnable = new GiftNumAnimaRunnable();
                mHandler.postDelayed(mCurrentAnimRunnable, GIFT_DISMISS_TIME);
                checkGiftCountSubscribe();
            }
        }
    }

    public AnimatorSet startAnimation(ICustormAnim anim) {
        this.anim = anim;
        if (anim == null) {
            hideView();
            //布局飞入
            ObjectAnimator flyFromLtoR = GiftAnimationUtil.createFlyFromLtoR(anim_rl, -getWidth(), 0, 400, new OvershootInterpolator());
            flyFromLtoR.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    initLayoutState();
                }
            });

            //礼物飞入
            ObjectAnimator flyFromLtoR2 = GiftAnimationUtil.createFlyFromLtoR(anim_gift, -getWidth(), 0, 400, new DecelerateInterpolator());
            flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    anim_gift.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    comboAnimation(true);
                }
            });
            AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(flyFromLtoR, flyFromLtoR2);

            return animatorSet;
        } else {
            return anim.startAnim(this, rootView);
        }
    }

    public void comboAnimation(boolean isFirst) {
        if (anim == null) {
           if(isFirst){
               GiftAnimationUtil.startAnimationDrawable(anim_light);
               anim_num.setVisibility(View.VISIBLE);
               anim_num.setText("x " + mCombo);
               comboEndAnim();
           }else {
               //数量增加
               ObjectAnimator scaleGiftNum = GiftAnimationUtil.scaleGiftNum(anim_num);
               scaleGiftNum.addListener(new AnimatorListenerAdapter() {
                   @Override
                   public void onAnimationStart(Animator animation) {

                   }

                   @Override
                   public void onAnimationEnd(Animator animation) {
                       comboEndAnim();
                   }
               });
               scaleGiftNum.start();
           }
        } else {
            anim.comboAnim(this, rootView, isFirst);
        }
    }

    public AnimatorSet endAnmation(ICustormAnim anim) {
        if (anim == null) {
            //向上渐变消失
            ObjectAnimator fadeAnimator = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 0, -100, 500, 0);
            fadeAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    anim_num.setVisibility(View.INVISIBLE);
                }
            });
            // 复原
            ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(GiftFrameLayout.this, 100, 0, 0, 0);

            AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
            return animatorSet;
        } else {
            return anim.endAnim(this, rootView);
        }
    }
}
