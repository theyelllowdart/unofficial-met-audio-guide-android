package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

import android.support.annotation.Nullable;

public class Media implements Comparable<Media> {
  private final int id;
  private final String url;
  private final String title;
  private final int position;
  private final String artObjectId;
  private final int stopId;

  public Media(int id, String url, String title, int position, String artObjectId, int stopId) {

    this.id = id;
    this.url = url;
    this.title = title;
    this.position = position;
    this.artObjectId = artObjectId;
    this.stopId = stopId;
  }


  public int getId() {
    return id;
  }

  @Nullable
  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public int getPosition() {
    return position;
  }

  public String getArtObjectId() {
    return artObjectId;
  }

  public int getStopId() {
    return stopId;
  }

  @Override
  public int compareTo(Media another) {
    int artObjectCompare = this.artObjectId.compareTo(another.artObjectId);
    if (artObjectCompare == 0) {
      return Integer.compare(this.position, another.position);
    } else {
      return artObjectCompare;
    }
  }
}
