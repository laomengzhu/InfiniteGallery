package com.laomengzhu.infinitegallery.transformer;

import android.view.View;

import com.laomengzhu.infinitegallery.InfiniteGallery;


public class OuterRotationTransformer implements InfiniteGallery.GalleryTransformer {

    private float targetRotationDegrees;

    public OuterRotationTransformer(float targetRotationDegrees) {
        super();
        this.targetRotationDegrees = Math.abs(targetRotationDegrees);
    }

    @Override
    public void process(View enterItemView, View exitItemView, float progress) {
        exitItemView.setRotationY(-targetRotationDegrees * progress);
        if (progress > 0) {// to right
            enterItemView.setRotationY(targetRotationDegrees * (1 - Math.abs(progress)));
        } else if (progress < 0) {// to left
            enterItemView.setRotationY(-targetRotationDegrees * (1 - Math.abs(progress)));
        }
    }

}
