package com.laomengzhu.infinitegallery;

public class ItemInfo {
    private int viewPosition;
    private int dataPosition;

    public ItemInfo(int viewPosition, int dataPosition) {
        super();
        this.viewPosition = viewPosition;
        this.dataPosition = dataPosition;
    }

    public int getViewPosition() {
        return viewPosition;
    }

    public void setViewPosition(int viewPosition) {
        this.viewPosition = viewPosition;
    }

    public int getDataPosition() {
        return dataPosition;
    }

    public void setDataPosition(int dataPosition) {
        this.dataPosition = dataPosition;
    }
}
