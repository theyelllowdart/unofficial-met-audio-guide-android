package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class Gallery {
  private final int id;
  private final String title;
  private final List<ArtObject> artObjects = new ArrayList<ArtObject>();
  private RectF bound;
  private Float labelX;
  private Float labelY;

  public Gallery(int id, String title) {
    this.id = id;
    this.title = title;
  }

  public Gallery(int id, String title, RectF bound, Float labelX, Float labelY) {
    this.id = id;
    this.title = title;
    this.bound = bound;
    this.labelX = labelX;
    this.labelY = labelY;
  }

  public void addArtObject(ArtObject artObject) {
    artObjects.add(artObject);
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public List<ArtObject> getArtObjects() {
    return artObjects;
  }

  public RectF getBound() {
    return bound;
  }

  public Float getLabelX() {
    return labelX;
  }

  public Float getLabelY() {
    return labelY;
  }
}
