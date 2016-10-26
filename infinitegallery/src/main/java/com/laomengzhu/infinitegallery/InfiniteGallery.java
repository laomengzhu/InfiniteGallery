package com.laomengzhu.infinitegallery;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaolifan on 2016/9/21.
 */
public class InfiniteGallery extends FrameLayout implements AnimatorUpdateListener {

    private static final int PREDICT_CHILD_COUNT = 5;

    private int itemWidth;
    private int itemHeight;
    private int itemCount = 0;
    private int itemMargin;
    private int mCenterViewPosition = 0;
    private int mAnimatorDuration = 600;
    private long mAutoSwitchInterval = 5000;
    private boolean mAutoSwitch = false;

    private SignedValueAnimator mScrollAnimator = SignedValueAnimator.ofFloat(0, 1.0f);
    private View exitView;
    private View enterView;
    private GalleryTransformer mTransformer;

    private GalleryViewFactory factory;
    private List<GalleryChangeListener> changeListeners = new ArrayList<InfiniteGallery.GalleryChangeListener>();

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
        if (attrs == null) {
            itemWidth = 0;
            itemHeight = 0;
            return;
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.InfiniteGallery);
        int resId = -1;

        // item width
        resId = ta.getResourceId(R.styleable.InfiniteGallery_item_width, -1);
        if (resId == -1) {
            itemWidth = ta.getDimensionPixelSize(R.styleable.InfiniteGallery_item_width, 0);
        } else {
            itemWidth = getResources().getDimensionPixelSize(resId);
        }
        // item height
        resId = ta.getResourceId(R.styleable.InfiniteGallery_item_height, -1);
        if (resId == -1) {
            itemHeight = ta.getDimensionPixelSize(R.styleable.InfiniteGallery_item_height, 0);
        } else {
            itemHeight = getResources().getDimensionPixelSize(resId);
        }
        // item margin
        resId = ta.getResourceId(R.styleable.InfiniteGallery_itemMargin, -1);
        if (resId == -1) {
            itemMargin = ta.getDimensionPixelSize(R.styleable.InfiniteGallery_itemMargin, 0);
        } else {
            itemMargin = getResources().getDimensionPixelSize(resId);
        }
        // animator duration
        mAnimatorDuration = ta.getInteger(R.styleable.InfiniteGallery_animatorDuration, 600);
        // auto switch interval
        mAutoSwitchInterval = ta.getInteger(R.styleable.InfiniteGallery_autoSwitchInterval, 5000);

        ta.recycle();

        // if (isInEditMode()) {
        // factory = new PreViewFactory(context);
        // setItemCount(3);
        // }

        mScrollAnimator.addUpdateListener(this);
        mScrollAnimator.addListener(animatorListener);
        mScrollAnimator.setDuration(mAnimatorDuration);
    }

    private void setupChildViews() {
        removeAllViews();
        int childCount = itemCount > 1 ? PREDICT_CHILD_COUNT : itemCount;
        if (childCount < 1) {
            return;
        }
        mCenterViewPosition = childCount > 1 ? 2 : 0;
        // add child views
        View childView;
        View centerChildView = null;
        LayoutParams lp;
        for (int i = 0; i < childCount; i++) {
            childView = factory.createView();
            childView.setTag(R.id.poster_info_tag, new ItemInfo(i, getDataPosition(i)));
            lp = new LayoutParams(itemWidth, itemHeight);
            lp.gravity = Gravity.CENTER;
            addView(childView, lp);
            factory.bindView(getDataPosition(i), childView);

            if (i == mCenterViewPosition) {
                centerChildView = childView;
            } else {
                if (i < mCenterViewPosition) {
                    childView.setTranslationX(-(1 + Math.abs(Math.abs(i - mCenterViewPosition) - 1)) * (itemWidth
                            + itemMargin));
                } else {
                    childView.setTranslationX((1 + Math.abs(Math.abs(i - mCenterViewPosition) - 1)) * (itemWidth
                            + itemMargin));
                }
            }

            if (Math.abs(i - mCenterViewPosition) > 1) {
                childView.setVisibility(INVISIBLE);
            } else {
                childView.setVisibility(VISIBLE);
            }
        }
        bringVisibleViewToFront(mCenterViewPosition);
        if (mTransformer == null) {
            return;
        }
        enterView = centerChildView;
        for (GalleryChangeListener listener : changeListeners) {
            listener.onBeginSelectGallery(centerChildView);
        }
        ItemInfo info;
        for (int i = 0; i < childCount; i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            if (info.getViewPosition() < mCenterViewPosition) {
                mTransformer.process(enterView, childView, -1);
            } else if (info.getViewPosition() > mCenterViewPosition) {
                mTransformer.process(enterView, childView, 1);
            } else {
                mTransformer.process(enterView, null, 1);
            }
        }
        for (GalleryChangeListener listener : changeListeners) {
            listener.onEndSelectGallery(centerChildView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int realWidthMeasureSpec = 0;
        int realHeightMeasureSpec = 0;
        if (itemWidth <= 0) {
            itemWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - 2 * itemMargin) / 3;
        } else if (getMeasuredWidth() < (itemWidth * 3) + getPaddingLeft() + getPaddingRight() + 2 * itemMargin) {
            realWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (itemWidth * 3) + getPaddingLeft()
                    + getPaddingRight() + 2 * itemMargin, MeasureSpec.EXACTLY);
        }
        if (itemHeight <= 0) {
            itemHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        } else if (getMeasuredHeight() < itemHeight + getPaddingTop() + getPaddingBottom()) {
            realHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemHeight + getPaddingTop() + getPaddingBottom(),
                    MeasureSpec.EXACTLY);
        }

        if (realWidthMeasureSpec != 0 || realHeightMeasureSpec != 0) {
            super.onMeasure(realWidthMeasureSpec == 0 ? widthMeasureSpec : realWidthMeasureSpec,
                    realHeightMeasureSpec == 0 ? heightMeasureSpec : realHeightMeasureSpec);
        }
    }

    private int getDataPosition(int position) {
        int realPosition = position - 2;
        if (realPosition < 0) {
            do {
                realPosition += itemCount;
            } while (realPosition < 0);
        } else if (realPosition > (itemCount - 1)) {
            do {
                realPosition -= itemCount;
            } while (realPosition > (itemCount - 1));
        }
        return realPosition;
    }

    public void setGalleryViewFactory(GalleryViewFactory factory) {
        this.factory = factory;
    }

    public void addGalleryChangeListener(GalleryChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeGalleryChangeListener(GalleryChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void setTransformer(GalleryTransformer transformer) {
        this.mTransformer = transformer;
    }

    /**
     * @return true means children views has reset and will invoke bind view auto; false means do nothing, you should
     * invoke notifyDataSetChange to refresh data if you need
     */
    public boolean setItemCount(int itemCount) {
        if (factory == null) {
            throw new IllegalStateException("GalleryViewFactory has not set");
        }
        int oldItemCount = this.itemCount;
        this.itemCount = itemCount;
        if (needReSetupChildViews(oldItemCount, itemCount)) {
            if (itemWidth > 0 && itemHeight > 0) {
                setupChildViews();
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        setupChildViews();
                    }
                });
            }
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

    private boolean needReSetupChildViews(int oldItemCount, int newItemCount) {
        int oldChildCount = oldItemCount > 1 ? PREDICT_CHILD_COUNT : oldItemCount;
        int newChildCount = newItemCount > 1 ? PREDICT_CHILD_COUNT : newItemCount;
        return oldChildCount != newChildCount;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && itemCount > 1) {
            moveRight();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && itemCount > 1) {
            moveLeft();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveRight() {
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.end();
        }
        mScrollAnimator.setDirection(SignedValueAnimator.DIRECTION_RIGHT);
        int newCenterViewPosition = getCorrectViewPosition(mCenterViewPosition - 1);
        View childView;
        ItemInfo info;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            if (info.getViewPosition() == newCenterViewPosition) {
                enterView = childView;
            } else if (info.getViewPosition() == mCenterViewPosition) {
                exitView = childView;
            }
            info.setAnimatorStartValue(childView.getTranslationX());
            info.setAnimatorEndValue(childView.getTranslationX() + itemWidth + itemMargin);
        }
        bringVisibleViewToFront(newCenterViewPosition);
        mScrollAnimator.start();
        mCenterViewPosition = newCenterViewPosition;
        for (GalleryChangeListener listener : changeListeners) {
            listener.onBeginSelectGallery(enterView);
        }
    }

    private void moveLeft() {
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.end();
        }
        mScrollAnimator.setDirection(SignedValueAnimator.DIRECTION_LEFT);
        int newCenterViewPosition = getCorrectViewPosition(mCenterViewPosition + 1);
        View childView;
        ItemInfo info;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            if (info.getViewPosition() == newCenterViewPosition) {
                enterView = childView;
            } else if (info.getViewPosition() == mCenterViewPosition) {
                exitView = childView;
            }
            info.setAnimatorStartValue(childView.getTranslationX());
            info.setAnimatorEndValue(childView.getTranslationX() - (itemWidth + itemMargin));
        }
        bringVisibleViewToFront(newCenterViewPosition);
        mScrollAnimator.start();
        mCenterViewPosition = newCenterViewPosition;
        for (GalleryChangeListener listener : changeListeners) {
            listener.onBeginSelectGallery(enterView);
        }
    }

    private SimpleAnimatorListener animatorListener = new SimpleAnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
            removeCallbacks(switchPagerRunnable);
        }

        ;

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mTransformer != null) {
                mTransformer.process(enterView, exitView, ((SignedValueAnimator) animation).getDirection());
            }
            movePreloadView();
            hidePreview(((SignedValueAnimator) animation).getDirection());
            for (GalleryChangeListener listener : changeListeners) {
                listener.onEndSelectGallery(enterView);
            }
            if (mAutoSwitch) {
                postDelayed(switchPagerRunnable, mAutoSwitchInterval);
            }
        }
    };

    private void movePreloadView() {
        View childView;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            if (childView.getTranslationX() < -2 * (itemWidth + itemMargin)) {
                childView.setTranslationX(2 * (itemWidth + itemMargin));
                childView.setVisibility(INVISIBLE);
                ItemInfo info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
                int dataPosition = info.getDataPosition() - 1;
                if (dataPosition < 0) {
                    dataPosition += itemCount;
                }
                info.setDataPosition(dataPosition);
                if (factory != null) {
                    factory.bindView(dataPosition, childView);
                }
                if (mTransformer != null) {
                    mTransformer.process(enterView, childView, 1);
                }
            } else if (childView.getTranslationX() > 2 * (itemWidth + itemMargin)) {
                childView.setTranslationX(-2 * (itemWidth + itemMargin));
                childView.setVisibility(INVISIBLE);
                ItemInfo info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
                int dataPosition = info.getDataPosition() + 1;
                if (dataPosition > (itemCount - 1)) {
                    dataPosition -= itemCount;
                }
                info.setDataPosition(dataPosition);
                if (factory != null) {
                    factory.bindView(dataPosition, childView);
                }
                if (mTransformer != null) {
                    mTransformer.process(enterView, childView, -1);
                }
            }
        }
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        View childView;
        ItemInfo info;
        float fraction = Float.parseFloat(animation.getAnimatedValue().toString());
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            childView.setTranslationX(info.getAnimatorStartValue() +
                    fraction * (info.getAnimatorEndValue() - info.getAnimatorStartValue()));
        }
        if (mTransformer != null) {
            mTransformer.process(enterView, exitView, fraction * mScrollAnimator.getDirection());
        }
    }

    private void bringVisibleViewToFront(int centerPosition) {
        int left = getCorrectViewPosition(centerPosition - 1);
        int right = getCorrectViewPosition(centerPosition + 1);
        View childView;
        ItemInfo info;
        View leftView = null, centerView = null, rightView = null;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            if (info.getViewPosition() == centerPosition) {
                centerView = childView;
            } else if (info.getViewPosition() == right) {
                rightView = childView;
            } else if (info.getViewPosition() == left) {
                leftView = childView;
            }
        }
        if (leftView != null) {
            leftView.setVisibility(VISIBLE);
            leftView.bringToFront();
        }
        if (rightView != null) {
            rightView.setVisibility(VISIBLE);
            rightView.bringToFront();
        }
        if (centerView != null) {
            centerView.bringToFront();
        }
    }

    private void hidePreview(int direction) {
        int hidePosition = -1;
        if (SignedValueAnimator.DIRECTION_LEFT == direction) {
            hidePosition = getCorrectViewPosition(mCenterViewPosition - 2);
        } else if (SignedValueAnimator.DIRECTION_RIGHT == direction) {
            hidePosition = getCorrectViewPosition(mCenterViewPosition + 2);
        } else {
            return;
        }
        View childView;
        ItemInfo info;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            info = (ItemInfo) childView.getTag(R.id.poster_info_tag);
            if (info.getViewPosition() == hidePosition) {
                childView.setVisibility(INVISIBLE);
                return;
            }
        }
    }

    private int getCorrectViewPosition(int position) {
        if (position > (PREDICT_CHILD_COUNT - 1)) {
            position -= PREDICT_CHILD_COUNT;
        } else if (position < 0) {
            position += PREDICT_CHILD_COUNT;
        }
        return position;
    }

    public int getCurrentCenterDataPosition() {
        if (enterView == null) {
            return 0;
            // throw new IllegalStateException("the enterview is null");
        }
        ItemInfo info = (ItemInfo) enterView.getTag(R.id.poster_info_tag);
        return info.getDataPosition();
    }

    public void startAutoSwitch() {
        if (itemCount > 1) {
            removeCallbacks(switchPagerRunnable);
            mAutoSwitch = true;
            postDelayed(switchPagerRunnable, mAutoSwitchInterval);
        }
    }

    public void stopAutoSwith() {
        mAutoSwitch = false;
        removeCallbacks(switchPagerRunnable);
    }

    public int getItemCount() {
        return itemCount;
    }

    private Runnable switchPagerRunnable = new Runnable() {

        @Override
        public void run() {
            moveLeft();
        }
    };

    public interface GalleryViewFactory {
        View createView();

        /**
         * @param dataPosition the data index
         * @param view         view to fill data
         */
        void bindView(int dataPosition, View view);
    }

    public interface GalleryChangeListener {
        void onBeginSelectGallery(View itemView);

        void onEndSelectGallery(View itemView);
    }

    public interface GalleryTransformer {
        /**
         * @param enterItemView view will scroll to center
         * @param exitItemView  view will scroll out from center
         * @param progress      between 0 and 1, scroll left &lt; 0, scroll right &gt; 0
         */
        void process(View enterItemView, View exitItemView, float progress);
    }
}
