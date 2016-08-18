package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public class ArtObject implements Comparable<ArtObject> {
  private final String id;
  private final String title;
  private final int position;
  private final int galleryId;
  private Float locationX;
  private Float locationY;
  private float rotation;
  private final String imageURL;
  private final int imageWidth;
  private final int imageHeight;
  private final ArrayList<Media> medias = new ArrayList<>();

  public ArtObject(String id, String title, int position, int galleryId, Float locationX, Float locationY, float rotation, String imageURL, int imageWidth, int imageHeight) {
    this.id = id;
    this.title = title;
    this.position = position;
    this.galleryId = galleryId;
    this.locationX = locationX;
    this.locationY = locationY;
    this.rotation = rotation;
    this.imageURL = imageURL;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public int getPosition() {
    return position;
  }

  public int getGalleryId() {
    return galleryId;
  }

  @Nullable
  public Float getLocationX() {
    return locationX;
  }

  @Nullable
  public Float getLocationY() {
    return locationY;
  }

  public String getImageURL() {
    return imageURL;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  public void addMedia(Media media) {
    medias.add(media);
  }

  public ArrayList<Media> getMedias() {
    return medias;
  }

  public float getRotation() {
    return rotation;
  }

  @Override
  public int compareTo(ArtObject another) {
    return Integer.compare(this.getPosition(), another.getPosition());
  }

  public void setLocationX(Float locationX) {
    this.locationX = locationX;
  }

  public void setLocationY(Float locationY) {
    this.locationY = locationY;
  }

  public void setRotation(Float rotation) {
    this.rotation = rotation;
  }
}
