package com.laomengzhu.infinitegallery.transformer;

import android.view.View;

import com.laomengzhu.infinitegallery.InfiniteGallery;


public class TransScaleTransformer implements InfiniteGallery.GalleryTransformer {

    private float targetScale;

    public TransScaleTransformer(float targetScale) {
        super();
        this.targetScale = Math.abs(targetScale);
    }

    @Override
    public void process(View enterItemView, View exitItemView, float progress) {
        if (Math.abs(progress) == 0) {
            return;
        }
        enterItemView.setScaleX(1.0f + (targetScale - 1.0f) * Math.abs(progress));
        enterItemView.setScaleY(1.0f + (targetScale - 1.0f) * Math.abs(progress));

        exitItemView.setScaleX(targetScale + (1 - targetScale) * Math.abs(progress));
        exitItemView.setScaleY(targetScale + (1 - targetScale) * Math.abs(progress));
    }
}
