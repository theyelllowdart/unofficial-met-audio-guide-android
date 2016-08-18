package com.github.theyelllowdart.unofficialmetaudioguide.android;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;
import android.provider.Settings;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Gallery;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryLabel;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryViewRect;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Media;
import com.github.theyelllowdart.unofficialmetaudioguide.android.service.AddStopRecorder;
import com.github.theyelllowdart.unofficialmetaudioguide.android.service.ArtObjectLocationUpdateRecorder;
import com.github.theyelllowdart.unofficialmetaudioguide.android.service.ArtObjectMissingRecorder;
import com.github.theyelllowdart.unofficialmetaudioguide.android.service.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

  private class UserArtObjectLocation {
    private final int id;
    private final String artObjectId;
    private final float x;
    private final float y;
    private final float rotation;
    private final boolean uploaded;

    public UserArtObjectLocation(int id, String artObjectId, float x, float y, float rotation, boolean uploaded) {
      this.id = id;
      this.artObjectId = artObjectId;
      this.x = x;
      this.y = y;
      this.rotation = rotation;
      this.uploaded = uploaded;
    }
  }

  public static Map<Integer, GalleryViewRect> galleryRectById = new HashMap<>();
  public static Map<Integer, GalleryLabel> galleryLabels = new HashMap<>();
  public static Map<Integer, Gallery> idToGallery = new HashMap<>();
  public static Map<String, ArtObject> idToArtObject = new HashMap<>();
  public static HashMap<Integer, Media> idToMedia = new HashMap<>();
  public static ArtObjectLocationUpdateRecorder artObjectLocationUpdateRecorder;
  public static ArtObjectMissingRecorder artObjectMissingRecorder;
  public static AddStopRecorder addStopRecorder;

  @Override
  public void onCreate() {
    final float density = getResources().getDisplayMetrics().density;
    super.onCreate();
    SQLiteDatabase db = new DatabaseHelper(getApplicationContext()).getWritableDatabase();

    ArrayList<Gallery> galleries = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("SELECT * FROM gallery", null)) {
      while (cursor.moveToNext()) {
        RectF bound = null;
        if (!cursor.isNull(cursor.getColumnIndex("bound_x1"))) {
          bound = new RectF(
            cursor.getFloat(cursor.getColumnIndex("bound_x1")) * density,
            cursor.getFloat(cursor.getColumnIndex("bound_y1")) * density,
            cursor.getFloat(cursor.getColumnIndex("bound_x2")) * density,
            cursor.getFloat(cursor.getColumnIndex("bound_y2")) * density
          );
        }
        Gallery gallery = new Gallery(
          cursor.getInt(cursor.getColumnIndex("id")),
          cursor.getString(cursor.getColumnIndex("title")),
          bound,
          cursor.isNull(cursor.getColumnIndex("label_x")) ? null : cursor.getFloat(cursor.getColumnIndex("label_x")) * density,
          cursor.isNull(cursor.getColumnIndex("label_y")) ? null : cursor.getFloat(cursor.getColumnIndex("label_y")) * density
        );
        galleries.add(gallery);
      }
    }
    for (Gallery gallery : galleries) {
      idToGallery.put(gallery.getId(), gallery);
      RectF bound = gallery.getBound();
      if (bound != null) {
        GalleryViewRect galleryViewRect = new GalleryViewRect(
          gallery.getId(),
          bound.left,
          bound.top,
          bound.right,
          bound.bottom
        );
        galleryRectById.put(gallery.getId(), galleryViewRect);
      }
      if (gallery.getLabelX() != null) {
        GalleryLabel galleryLabel = new GalleryLabel(
          String.valueOf(gallery.getId()),
          new float[]{gallery.getLabelX(), gallery.getLabelY()},
          true
        );
        galleryLabels.put(gallery.getId(), galleryLabel);
      }
    }

    ArrayList<ArtObject> artObjects = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("SELECT * FROM art_object", null)) {
      while (cursor.moveToNext()) {
        ArtObject artObject = new ArtObject(
          cursor.getString(cursor.getColumnIndex("id")),
          cursor.getString(cursor.getColumnIndex("title")),
          cursor.getInt(cursor.getColumnIndex("position")),
          cursor.getInt(cursor.getColumnIndex("gallery_id")),
          cursor.isNull(cursor.getColumnIndex("position_x")) ? null : cursor.getFloat(cursor.getColumnIndex("position_x")),
          cursor.isNull(cursor.getColumnIndex("position_y")) ? null : cursor.getFloat(cursor.getColumnIndex("position_y")),
          cursor.isNull(cursor.getColumnIndex("rotation")) ? 0 : cursor.getFloat(cursor.getColumnIndex("rotation")),
          cursor.getString(cursor.getColumnIndex("image_url")),
          cursor.getInt(cursor.getColumnIndex("image_width")),
          cursor.getInt(cursor.getColumnIndex("image_height"))
        );
        artObjects.add(artObject);
      }
    }
    for (ArtObject artObject : artObjects) {
      idToArtObject.put(artObject.getId(), artObject);
      idToGallery.get(artObject.getGalleryId()).addArtObject(artObject);
    }

    ArrayList<UserArtObjectLocation> locations = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("SELECT * FROM user_art_object_location", null)) {
      while (cursor.moveToNext()) {
        UserArtObjectLocation location = new UserArtObjectLocation(
          cursor.getInt(cursor.getColumnIndex("id")),
          cursor.getString(cursor.getColumnIndex("art_object_id")),
          cursor.getFloat(cursor.getColumnIndex("x")),
          cursor.getFloat(cursor.getColumnIndex("y")),
          cursor.getFloat(cursor.getColumnIndex("rotation")),
          cursor.getInt(cursor.getColumnIndex("uploaded")) == 1
        );
        locations.add(location);
      }
    }
    for (UserArtObjectLocation location : locations) {
      ArtObject artObject = idToArtObject.get(location.artObjectId);
      if (artObject != null) {
        artObject.setLocationX(location.x);
        artObject.setLocationY(location.y);
        artObject.setRotation(location.rotation);
      }
    }

    ArrayList<Media> medias = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("SELECT * FROM media", null)) {
      while (cursor.moveToNext()) {
        Media media = new Media(
          cursor.getInt(cursor.getColumnIndex("id")),
          cursor.getString(cursor.getColumnIndex("url")),
          cursor.getString(cursor.getColumnIndex("title")),
          cursor.getInt(cursor.getColumnIndex("position")),
          cursor.getString(cursor.getColumnIndex("art_object_id")),
          cursor.getInt(cursor.getColumnIndex("stop_id"))
        );
        medias.add(media);
      }
    }
    Collections.sort(medias);
    for (Media media : medias) {
      idToMedia.put(media.getId(), media);
      idToArtObject.get(media.getArtObjectId()).addMedia(media);
    }

    // TODO(aaron): Put this into a data verification test
    for (GalleryViewRect rect1 : galleryRectById.values()) {
      for (GalleryViewRect rect2 : galleryRectById.values()) {
        if (rect2.getScaled().intersect(rect1.getScaled()) && rect1.getId() != rect2.getId()) {
          throw new RuntimeException("intersecting shapes " + rect1.getId() + " " + rect2.getId());
        }
      }
    }

    RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());

    String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(),
      Settings.Secure.ANDROID_ID);
    artObjectLocationUpdateRecorder = new ArtObjectLocationUpdateRecorder(db, requestQueue, androidId);
    artObjectMissingRecorder = new ArtObjectMissingRecorder(requestQueue, androidId);
    addStopRecorder = new AddStopRecorder(requestQueue, androidId);

  }
}
