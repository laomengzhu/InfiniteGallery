package com.laomengzhu.infinitegallerysample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.laomengzhu.infinitegallery.InfiniteGallery;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private InfiniteGallery posterGallery;
    private static ArrayList<String> urls = new ArrayList<String>();

    static {
        urls.add("http://odub97kv7.bkt.clouddn.com/p1.jpg");
        urls.add("http://odub97kv7.bkt.clouddn.com/p2.jpg");
        urls.add("http://odub97kv7.bkt.clouddn.com/p3.jpg");
        urls.add("http://odub97kv7.bkt.clouddn.com/p4.jpg");
        urls.add("http://odub97kv7.bkt.clouddn.com/p5.jpg");
        urls.add("http://odub97kv7.bkt.clouddn.com/p6.jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initImageLoader();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        posterGallery = (InfiniteGallery) findViewById(R.id.posterGallery1);
        posterGallery.setGalleryViewFactory(new InfiniteGallery.GalleryViewFactory() {

            @Override
            public View makeView() {
                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setBackgroundColor(Color.GRAY);
                return imageView;
            }

            @Override
            public void bindView(int position, View view) {
                ImageView imageView = (ImageView) view;
                ImageLoader.getInstance().displayImage(urls.get(position), imageView);
            }
        });
        posterGallery.setPosterCount(1);
        posterGallery.postDelayed(new Runnable() {

            @Override
            public void run() {
                posterGallery.setPosterCount(6);
            }
        }, 5000);
    }

    private void initImageLoader() throws IOException {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.place_holder)
                .showImageOnFail(R.drawable.place_holder)
                .showImageOnLoading(R.drawable.place_holder)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .resetViewBeforeLoading(false)
                .build();
        File cacheDir = StorageUtils.getCacheDirectory(this);
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(1)
                .memoryCacheSize(5 * 1024 * 2014)
                .diskCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 10 * 1024 * 1024))
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(configuration);
    }
}
