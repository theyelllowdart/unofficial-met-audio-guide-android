package com.github.theyelllowdart.unofficialmetaudioguide.android.model;

public class ArtObjectLocation implements Comparable<ArtObjectLocation> {
  private final String artObjectId;
  private final int position;
  private final float x;
  private final float y;
  private final float rotation;
  private final boolean userPlaced;

  public ArtObjectLocation(String artObjectId, int position, float x, float y, float rotation, boolean userPlaced) {
    this.artObjectId = artObjectId;
    this.position = position;
    this.x = x;
    this.y = y;
    this.rotation = rotation;
    this.userPlaced = userPlaced;
  }

  public int getPosition() {
    return position;
  }

  public String getArtObjectId() {
    return artObjectId;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getRotation() {
    return rotation;
  }

  public boolean isUserPlaced() {
    return userPlaced;
  }

  @Override
  public int compareTo(ArtObjectLocation another) {
    return Float.compare(y, another.y);
  }
}
