package com.laomengzhu.infinitegallery.preview;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.laomengzhu.infinitegallery.InfiniteGallery;


/**
 * simple view factory to create ImageView
 */
public class PreViewFactory implements InfiniteGallery.GalleryViewFactory {

    private Context context;

    public PreViewFactory(Context context) {
        this.context = context;
    }

    @Override
    public View createView() {
        return new View(context);
    }

    @Override
    public void bindView(int position, View view) {
        if (position == 0) {
            view.setBackgroundColor(Color.RED);
        } else if (position == 1) {
            view.setBackgroundColor(Color.GREEN);
        } else {
            view.setBackgroundColor(Color.BLUE);
        }
    }

}
