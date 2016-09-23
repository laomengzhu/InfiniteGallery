package com.laomengzhu.infinitegallery.transformer;

import android.support.v4.view.ViewCompat;
import android.view.View;

import com.laomengzhu.infinitegallery.InfiniteGallery;

public class DeepthTransformer implements InfiniteGallery.GalleryTransformer {

    private float targetScale;

    public DeepthTransformer(float targetScale) {
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
        ViewCompat.setTranslationZ(enterItemView, 10 * Math.abs(progress));
        ViewCompat.setTranslationZ(exitItemView, 10 + (0 - 10) * Math.abs(progress));
    }
}
