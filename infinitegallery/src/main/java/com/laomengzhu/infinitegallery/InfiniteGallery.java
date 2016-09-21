package com.laomengzhu.infinitegallery;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by xiaolifan on 2016/9/21.
 */

public class InfiniteGallery extends FrameLayout {

    private static final int DEFAULT_POSTER_WIDTH = 160;
    private static final int DEFAULT_POSTER_HEIGHT = 90;
    private static final int PREDICT_CHILD_COUNT = 5;

    private float mRatio = 0.84f;
    private GalleryViewFactory factory;
    private GalleryChangeListener changeListener;
    private int posterWidth;
    private int posterHeight;
    private int posterCount = 0;
    private int mCenterViewPosition = 0;

    // animators
    private AnimatorSet mAnimatorSet;
    private ArrayList<ObjectAnimator> animators = new ArrayList<ObjectAnimator>(PREDICT_CHILD_COUNT);
    private ObjectAnimator enterScaleXAnimator, enterScaleYAnimator;
    private ObjectAnimator exitScaleXAnimator, exitScaleYAnimator;

    @SuppressLint("NewApi")
    public InfiniteGallery(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);
    }

    public InfiniteGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs);
    }

    public InfiniteGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public InfiniteGallery(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public void initAttrs(Context context, AttributeSet attrs) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        if (attrs == null || isInEditMode()) {
            posterWidth = DEFAULT_POSTER_WIDTH;
            posterHeight = DEFAULT_POSTER_HEIGHT;
            return;
        }
        if (isInEditMode()) {
            factory = new PreViewFactory(context);
            setPosterCount(3);
            return;
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InfiniteGallery);
        int resId = -1;

        // poster width
        resId = ta.getResourceId(R.styleable.InfiniteGallery_poster_width, -1);
        if (resId == -1) {
            posterWidth = ta.getDimensionPixelSize(R.styleable.InfiniteGallery_poster_width, DEFAULT_POSTER_WIDTH);
        } else {
            posterWidth = getResources().getDimensionPixelSize(resId);
        }
        // poster height
        resId = ta.getResourceId(R.styleable.InfiniteGallery_poster_height, -1);
        if (resId == -1) {
            posterHeight = ta.getDimensionPixelSize(R.styleable.InfiniteGallery_poster_height, DEFAULT_POSTER_HEIGHT);
        } else {
            posterHeight = getResources().getDimensionPixelSize(resId);
        }

        ta.recycle();
    }

    private void setupChildViews() {
        removeAllViews();
        int childCount = posterCount > 1 ? PREDICT_CHILD_COUNT : posterCount;
        if (childCount < 1) {
            return;
        }
        mCenterViewPosition = childCount > 1 ? 2 : 0;
        // add child views
        View childView;
        View centerChildView = null;
        LayoutParams lp;
        for (int i = 0; i < childCount; i++) {
            childView = factory.makeView();
            childView.setTag(R.id.poster_info_tag, new ItemInfo(i, getDataPosition(i)));
            lp = new LayoutParams(posterWidth, posterHeight);
            lp.gravity = Gravity.CENTER;
            addView(childView, lp);
            factory.bindView(getDataPosition(i), childView);

            if (i == mCenterViewPosition) {
                centerChildView = childView;
                childView.setScaleX(1.0f);
                childView.setScaleY(1.0f);
            } else {
                if (i < mCenterViewPosition) {
                    // childView.setRotationY(30);
                    childView.setTranslationX(-(1 + Math.abs(Math.abs(i - mCenterViewPosition) - 1)) * posterWidth
                            * mRatio);
                } else {
                    // childView.setRotationY(-30);
                    childView.setTranslationX((1 + Math.abs(Math.abs(i - mCenterViewPosition) - 1)) * posterWidth
                            * mRatio);
                }
                childView.setScaleX(mRatio);
                childView.setScaleY(mRatio);
            }
        }
        if (centerChildView != null) {
            centerChildView.bringToFront();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec((int) (posterWidth * mRatio * 3), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(posterHeight, MeasureSpec.EXACTLY));
    }

    private int getDataPosition(int position) {
        int realPosition = position - 2;
        if (realPosition < 0) {
            do {
                realPosition += posterCount;
            } while (realPosition < 0);
        } else if (realPosition > (posterCount - 1)) {
            do {
                realPosition -= posterCount;
            } while (realPosition > (posterCount - 1));
        }
        return realPosition;
    }

    public void setGalleryViewFactory(GalleryViewFactory factory) {
        this.factory = factory;
    }

    public void setGalleryChangeListener(GalleryChangeListener listener) {
        changeListener = listener;
    }

    public interface GalleryViewFactory {
        View makeView();

        /**
         * @param dataPosition the data index
         * @param view         view to fill data
         */
        void bindView(int dataPosition, View view);
    }

    public interface GalleryChangeListener {
        void onBeginSelectGallery(View itemView);

        void onEndSelectGallery(View itemView);

        /**
         * @param enterItemView view will scroll to center
         * @param exitItemView  view will scroll out from center
         * @param progress      between 0 and 1, scroll left &lt; 0, scroll right &gt; 0
         */
        void onSelecting(View enterItemView, View exitItemView, float progress);
    }

    /**
     * @return true means children views has reset and will invoke bind view auto; false means do nothing, you should
     * invoke notifyDataSetChange to refresh data if you need
     */
    public boolean setPosterCount(int posterCount) {
        if (factory == null) {
            throw new IllegalStateException("GalleryViewFactory has not set");
        }
        int oldPosterCount = this.posterCount;
        this.posterCount = posterCount;
        if (needReSetupChildViews(oldPosterCount, posterCount)) {
            setupChildViews();
            return true;
        } else {
            return false;
        }
    }

    /**
     * notify data has changed, the bindView will be invoked
     */
    public void notifyDataSetChange() {
        if (factory == null) {
            throw new IllegalStateException("GalleryViewFactory has not set");
        }
        int childCount = getChildCount();
        View childView;
        ItemInfo info;
        for (int i = 0; i < childCount; i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            factory.bindView(info.getDataPosition(), childView);
        }
    }

    private boolean needReSetupChildViews(int oldPosterCount, int newPosterCount) {
        int oldChildCount = oldPosterCount > 1 ? PREDICT_CHILD_COUNT : oldPosterCount;
        int newChildCount = newPosterCount > 1 ? PREDICT_CHILD_COUNT : newPosterCount;
        return oldChildCount != newChildCount;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && posterCount > 1) {
            moveLeft();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && posterCount > 1) {
            moveRight();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveRight() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
        int newCenterViewPosition = mCenterViewPosition - 1;
        if (newCenterViewPosition > (PREDICT_CHILD_COUNT - 1)) {
            newCenterViewPosition -= PREDICT_CHILD_COUNT;
        } else if (newCenterViewPosition < 0) {
            newCenterViewPosition += PREDICT_CHILD_COUNT;
        }
        ObjectAnimator animator;
        View childView;
        View targetView = null;
        for (int i = 0; i < PREDICT_CHILD_COUNT; i++) {
            childView = getChildAt(i);
            if (getItemViewPosition(childView) == newCenterViewPosition) {
                targetView = childView;
                setupEnterScaleAnimators(childView);
            } else if (getItemViewPosition(childView) == mCenterViewPosition) {
                setupExitScaleAnimator(childView);
            }
            animator = null;
            if (animators.size() > i) {
                animator = animators.get(i);
            }
            if (animator == null) {
                animator = ObjectAnimator.ofFloat(childView, View.TRANSLATION_X, childView.getTranslationX(),
                        childView.getTranslationX() + posterWidth * mRatio);
                animators.add(i, animator);
            } else {
                animator.setTarget(childView);
                animator.setFloatValues(childView.getTranslationX(),
                        childView.getTranslationX() + posterWidth * mRatio);
            }
        }
        setupAnimatorSet();
        if (targetView != null) {
            targetView.bringToFront();
        }
        mAnimatorSet.start();
        mCenterViewPosition = newCenterViewPosition;
    }

    private void moveLeft() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
        int newCenterViewPosition = mCenterViewPosition + 1;
        if (newCenterViewPosition > (PREDICT_CHILD_COUNT - 1)) {
            newCenterViewPosition -= PREDICT_CHILD_COUNT;
        } else if (newCenterViewPosition < 0) {
            newCenterViewPosition += PREDICT_CHILD_COUNT;
        }
        ObjectAnimator animator;
        View childView;
        View targetView = null;
        for (int i = 0; i < PREDICT_CHILD_COUNT; i++) {
            childView = getChildAt(i);
            if (getItemViewPosition(childView) == newCenterViewPosition) {
                targetView = childView;
                setupEnterScaleAnimators(childView);
            } else if (getItemViewPosition(childView) == mCenterViewPosition) {
                setupExitScaleAnimator(childView);
            }
            animator = null;
            if (animators.size() > i) {
                animator = animators.get(i);
            }
            if (animator == null) {
                animator = ObjectAnimator.ofFloat(childView, View.TRANSLATION_X, childView.getTranslationX(),
                        childView.getTranslationX() - posterWidth * mRatio);
                animators.add(i, animator);
            } else {
                animator.setTarget(childView);
                animator.setFloatValues(childView.getTranslationX(),
                        childView.getTranslationX() - posterWidth * mRatio);
            }
        }
        setupAnimatorSet();
        if (targetView != null) {
            targetView.bringToFront();
        }
        mAnimatorSet.start();
        ValueAnimator animator2 = ValueAnimator.ofFloat(0, 1.0f);
        mCenterViewPosition = newCenterViewPosition;
    }

    private int getItemViewPosition(View itemView) {
        ItemInfo info = (ItemInfo) itemView.getTag(R.id.poster_info_tag);
        return info.getViewPosition();
    }

    private void setupExitScaleAnimator(View childView) {
        if (exitScaleXAnimator == null) {
            exitScaleXAnimator = ObjectAnimator.ofFloat(childView, View.SCALE_X, childView.getScaleX(), mRatio);
        } else {
            exitScaleXAnimator.setTarget(childView);
            exitScaleXAnimator.setFloatValues(childView.getScaleX(), mRatio);
        }
        if (exitScaleYAnimator == null) {
            exitScaleYAnimator = ObjectAnimator.ofFloat(childView, View.SCALE_Y, childView.getScaleY(), mRatio);
        } else {
            exitScaleYAnimator.setTarget(childView);
            exitScaleYAnimator.setFloatValues(childView.getScaleY(), mRatio);
        }
    }

    private void setupEnterScaleAnimators(View childView) {
        if (enterScaleXAnimator == null) {
            enterScaleXAnimator = ObjectAnimator.ofFloat(childView, View.SCALE_X, childView.getScaleX(), 1.0f);
        } else {
            enterScaleXAnimator.setTarget(childView);
            enterScaleXAnimator.setFloatValues(childView.getScaleX(), 1.0f);
        }
        if (enterScaleYAnimator == null) {
            enterScaleYAnimator = ObjectAnimator.ofFloat(childView, View.SCALE_Y, childView.getScaleY(), 1.0f);
        } else {
            enterScaleYAnimator.setTarget(childView);
            enterScaleYAnimator.setFloatValues(childView.getScaleY(), 1.0f);
        }
    }

    private void setupAnimatorSet() {
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            ArrayList<Animator> tempAnimators = new ArrayList<Animator>();
            tempAnimators.addAll(animators);
            tempAnimators.add(enterScaleXAnimator);
            tempAnimators.add(enterScaleYAnimator);
            tempAnimators.add(exitScaleXAnimator);
            tempAnimators.add(exitScaleYAnimator);
            mAnimatorSet.playTogether(tempAnimators);
            mAnimatorSet.setDuration(1000);
            mAnimatorSet.addListener(animatorListener);
        }
    }

    private SimpleAnimatorListener animatorListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            movePreloadView();
        }
    };

    private void movePreloadView() {
        View childView;
        for (int i = 0; i < PREDICT_CHILD_COUNT; i++) {
            childView = getChildAt(i);
            if (childView.getTranslationX() < -2 * posterWidth * mRatio) {
                childView.setTranslationX(2 * posterWidth * mRatio);
                ItemInfo info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
                int dataPosition = info.getDataPosition() - 1;
                if (dataPosition < 0) {
                    dataPosition += posterCount;
                }
                info.setDataPosition(dataPosition);
                if (factory != null) {
                    factory.bindView(dataPosition, childView);
                }
            } else if (childView.getTranslationX() > 2 * posterWidth * mRatio) {
                childView.setTranslationX(-2 * posterWidth * mRatio);
                ItemInfo info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
                int dataPosition = info.getDataPosition() + 1;
                if (dataPosition > (posterCount - 1)) {
                    dataPosition -= posterCount;
                }
                info.setDataPosition(dataPosition);
                if (factory != null) {
                    factory.bindView(dataPosition, childView);
                }
            }
        }
    }
}
