package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.dync.giftlibrary.R;

/**
 * Created by KathLine on 2017/7/7.
 */

public class CustormAnim implements ICustormAnim {

    @Override
    public AnimatorSet startAnim(final GiftFrameLayout giftFrameLayout, View rootView) {
        //礼物飞入
        ObjectAnimator flyFromLtoR2 = GiftAnimationUtil.createFlyFromLtoR(giftFrameLayout, 1500, 0, 2000, new DecelerateInterpolator());
        flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                giftFrameLayout.initLayoutState();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                giftFrameLayout.comboAnimation();
            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(flyFromLtoR2);
        animSet.start();
        return animSet;
    }

    @Override
    public AnimatorSet comboAnim(final GiftFrameLayout giftFrameLayout, View rootView) {
        final StrokeTextView anim_num = (StrokeTextView) rootView.findViewById(R.id.animation_num);
        //数量增加
        ObjectAnimator scaleGiftNum = GiftAnimationUtil.scaleGiftNum(anim_num);
        scaleGiftNum.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                anim_num.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                giftFrameLayout.comboEndAnim();//这里一定要回调该方法，不然连击不会生效
            }
        });
        scaleGiftNum.start();
        return null;
    }

    @Override
    public AnimatorSet endAnim(final GiftFrameLayout giftFrameLayout, View rootView) {
        //向上渐变消失
        ObjectAnimator fadeAnimator = GiftAnimationUtil.createFadeAnimator(giftFrameLayout, 0, 0, 1500, 0);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                giftFrameLayout.setVisibility(View.INVISIBLE);
            }
        });
        // 复原
        ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(giftFrameLayout, 0, 0, 0, 0);

        AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
        return animatorSet;
    }
}
