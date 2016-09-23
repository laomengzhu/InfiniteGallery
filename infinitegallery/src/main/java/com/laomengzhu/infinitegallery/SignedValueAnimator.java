package com.laomengzhu.infinitegallery;

import android.animation.ValueAnimator;
import android.support.annotation.IntDef;

public class SignedValueAnimator extends ValueAnimator {

    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_RIGHT = 1;

    private int direction;

    @IntDef({ DIRECTION_LEFT, DIRECTION_RIGHT })
    public @interface AnimatorDirection {
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(@AnimatorDirection int direction) {
        this.direction = direction;
    }

    public static SignedValueAnimator ofFloat(float... values) {
        SignedValueAnimator anim = new SignedValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }
}
