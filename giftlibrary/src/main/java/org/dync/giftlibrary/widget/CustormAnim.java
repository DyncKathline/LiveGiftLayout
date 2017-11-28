package org.dync.giftlibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.dync.giftlibrary.R;
import org.dync.giftlibrary.util.GiftAnimationUtil;

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
                giftFrameLayout.comboAnimation(true);
            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(flyFromLtoR2);
        animSet.start();
        return animSet;
    }

    @Override
    public AnimatorSet comboAnim(final GiftFrameLayout giftFrameLayout, View rootView, boolean isFirst) {
        final StrokeTextView anim_num = (StrokeTextView) rootView.findViewById(R.id.animation_num);
        if (isFirst) {
            anim_num.setVisibility(View.VISIBLE);
            anim_num.setText("x " + giftFrameLayout.getCombo());
            giftFrameLayout.comboEndAnim();//这里一定要回调该方法，不然连击不会生效
        } else {
            //数量增加
            ObjectAnimator scaleGiftNum = GiftAnimationUtil.scaleGiftNum(anim_num);
            scaleGiftNum.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    giftFrameLayout.comboEndAnim();//这里一定要回调该方法，不然连击不会生效
                }
            });
            scaleGiftNum.start();
        }
        return null;
    }

    @Override
    public AnimatorSet endAnim(final GiftFrameLayout giftFrameLayout, View rootView) {
//        //向上渐变消失
//        ObjectAnimator fadeAnimator = GiftAnimationUtil.createFadeAnimator(giftFrameLayout, 0, 0, 1500, 0);
//        // 复原
//        ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(giftFrameLayout, 0, 0, 0, 0);
//        AnimatorSet animatorSet = GiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
//        return animatorSet;
        return testAnim(giftFrameLayout);
    }

    @NonNull
    private AnimatorSet testAnim(GiftFrameLayout giftFrameLayout) {
        PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", 0, -50);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.5f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(giftFrameLayout, translationY, alpha);
        animator.setStartDelay(0);
        animator.setDuration(1500);

        translationY = PropertyValuesHolder.ofFloat("translationY", -50, -100);
        alpha = PropertyValuesHolder.ofFloat("alpha", 0.5f, 0f);
        ObjectAnimator animator1 = ObjectAnimator.ofPropertyValuesHolder(giftFrameLayout, translationY, alpha);
        animator1.setStartDelay(0);
        animator1.setDuration(1500);

        // 复原
        ObjectAnimator fadeAnimator2 = GiftAnimationUtil.createFadeAnimator(giftFrameLayout, 0, 0, 0, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator1).after(animator);
        animatorSet.play(fadeAnimator2).after(animator1);
        animatorSet.start();
        return animatorSet;
    }
}
