package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

import android.graphics.RectF;


public class GalleryViewRect {
  private final int id;
  private final RectF scaled;

  public GalleryViewRect(int id, float x1, float y1, float x2, float y2) {
    this.id = id;
    scaled = new RectF(x1, y1, x2, y2);
  }

  public boolean contains(float x, float y) {
    return scaled.contains(x, y);
  }

  public int getId() {
    return id;
  }

  public RectF getScaled() {
    return scaled;
  }
}
