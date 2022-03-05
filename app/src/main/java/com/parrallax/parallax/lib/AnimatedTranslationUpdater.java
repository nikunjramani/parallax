package com.parrallax.parallax.lib;


import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;


public class AnimatedTranslationUpdater implements ParallaxLayerLayout.TranslationUpdater {

    private final float maxParallax;
    private ParallaxLayerLayout parallax;
    private ValueAnimator animation;

    public AnimatedTranslationUpdater() {
        this(1.0f);
    }

    public AnimatedTranslationUpdater(float maxParallax) {
        this.maxParallax = maxParallax;
    }

    @Override
    public void subscribe(ParallaxLayerLayout parallaxLayerLayout, int speed,boolean isVertical) {
        this.parallax = parallaxLayerLayout;
        ValueAnimator.AnimatorUpdateListener listener = animator -> parallax.updateTranslations(new float[] { (float) animator.getAnimatedValue(), 0.0f },isVertical);

        animation = ValueAnimator.ofFloat(0.0f, maxParallax, 0.0f, -maxParallax, 0.0f);
        animation.setDuration(speed); // speed
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setRepeatMode(ValueAnimator.RESTART);
        animation.addUpdateListener(listener);
        animation.start();
    }

    @Override
    public void unSubscribe() {
        if (animation != null && animation.isStarted()) {
            animation.end();
        }
    }
}
