package com.laomengzhu.infinitegallery.transformer;

import android.view.View;

import com.laomengzhu.infinitegallery.InfiniteGallery;

public class ScaleTransformer implements InfiniteGallery.GalleryTransformer {

    private float targetScale;

    public ScaleTransformer(float targetScale) {
        super();
        this.targetScale = Math.abs(targetScale);
    }

    @Override
    public void process(View enterItemView, View exitItemView, float progress) {
        if (Math.abs(progress) == 0) {
            return;
        }
        enterItemView.setScaleX(targetScale + (1.0f - targetScale) * Math.abs(progress));
        enterItemView.setScaleY(targetScale + (1.0f - targetScale) * Math.abs(progress));
        exitItemView.setScaleX(1 + (targetScale - 1) * Math.abs(progress));
        exitItemView.setScaleY(1 + (targetScale - 1) * Math.abs(progress));
    }

}
