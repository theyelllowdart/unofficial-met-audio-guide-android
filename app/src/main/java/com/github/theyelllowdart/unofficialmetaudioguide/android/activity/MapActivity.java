package com.github.theyelllowdart.unofficialmetaudioguide.android.activity;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.theyelllowdart.unofficialmetaudioguide.android.MyApplication;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.fragment.AddStopFragment;
import com.github.theyelllowdart.unofficialmetaudioguide.android.fragment.ArtObjectFragment;
import com.github.theyelllowdart.unofficialmetaudioguide.android.fragment.GalleryFragment;
import com.github.theyelllowdart.unofficialmetaudioguide.android.fragment.MovePinFragment;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObjectLocation;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Gallery;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryViewRect;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Media;
import com.github.theyelllowdart.unofficialmetaudioguide.android.view.MapView;
import com.github.theyelllowdart.unofficialmetaudioguide.android.view.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class MapActivity extends Activity implements ArtObjectFragment.OnMediaSelectListener,
  ArtObjectFragment.OnMovePinSelectListener,
  MapView.OnPinSelectListener,
  MapView.OnMapSelectListener,
  GalleryFragment.OnArtObjectSelectListener,
  MovePinFragment.OnExitMoveModeListener,
  MovePinFragment.OnPinRotationChangeListener,
  MovePinFragment.OnPinMoveAcceptListener,
  ArtObjectFragment.OnReportArtObjectMissingSelectListener,
  GalleryFragment.OnAddStopSelectListener,
  AddStopFragment.OnAddStopDestroyListener,
  AddStopFragment.OnAddStopPinRotationChangeListener,
  AddStopFragment.OnAddStopAcceptListener {

  private Float density;
  private Player player;
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    density = getResources().getDisplayMetrics().density;

    player = new Player(
      getBaseContext(),
      (SeekBar) findViewById(R.id.seek),
      (Button) findViewById(R.id.play),
      (TextView) findViewById(R.id.time),
      (TextView) findViewById(R.id.audioTitle)
    );

    mapView = (MapView) findViewById(R.id.map);
  }

  @Override
  public void onPinSelected(String artObjectId) {
    Object fragment = getFragmentManager()
      .findFragmentById(R.id.fragment_container);
    if (fragment instanceof GalleryFragment) {
      ((GalleryFragment) fragment).scrollToArtObject(artObjectId);
    } else if (fragment instanceof ArtObjectFragment) {
      ((ArtObjectFragment) fragment).init(artObjectId);
    }
  }

  @Override
  public void onMapSelected(float x, float y) {
    float[] coordinates = new float[]{x, y};
    Log.i("tag", x / density + " " + y / density);

    for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
      if (rect.contains(coordinates[0], coordinates[1])) {
        mapView.clearPins();
        final int galleryId = rect.getId();

        getFragmentManager()
          .beginTransaction()
          .replace(R.id.fragment_container, GalleryFragment.create(galleryId))
          .commit();

        Gallery gallery = MyApplication.idToGallery.get(galleryId);
        int unsetLocationCount = 0;
        ArrayList<ArtObjectLocation> pins = new ArrayList<>();
        for (ArtObject artObject : gallery.getArtObjects()) {
          float pinX;
          float pinY;
          boolean userPlaced;
          if (artObject.getLocationX() == null) {
            pinX = rect.getScaled().centerX() + (5 * density * unsetLocationCount);
            pinY = rect.getScaled().centerY();
            userPlaced = false;
            unsetLocationCount++;
          } else {
            pinX = artObject.getLocationX() * density;
            pinY = artObject.getLocationY() * density;
            userPlaced = true;
          }
          float rotation = artObject.getRotation();
          ArtObjectLocation location = new ArtObjectLocation(
            artObject.getId(), artObject.getPosition() + 1, pinX, pinY, rotation,
            userPlaced);
          pins.add(location);
        }
        Collections.sort(pins);
        mapView.setPins(pins);
        mapView.zoomToRect(rect.getScaled());
        break;
      }
    }
  }

  @Override
  public void onArtObjectSelected(String artObjectId) {
    getFragmentManager()
      .beginTransaction()
      .replace(R.id.fragment_container, ArtObjectFragment.create(artObjectId))
      .addToBackStack(null)
      .commit();
  }

  @Override
  public void onMediaSelected(int mediaId) {
    Media media = MyApplication.idToMedia.get(mediaId);
    ArtObject artObject = MyApplication.idToArtObject.get(media.getArtObjectId());
    ArrayList<Media> medias = artObject.getMedias();
    ArrayList<Media> queue = new ArrayList<>();
    for (int i = media.getPosition() + 1; i < medias.size(); i++) {
      Media m = medias.get(i);
      if (m.getUrl() != null) {
        queue.add(m);
      }
    }
    try {
      player.play(media.getUrl(), media.getTitle() + "-" + media.getStopId(), queue);
    } catch (IOException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onMovePinSelected(String artObjectId) {
    ArtObject artObject = MyApplication.idToArtObject.get(artObjectId);
    mapView.setPinToPlace(artObject.getPosition() + 1);
    mapView.setPinToPlaceRotation(artObject.getRotation());
    mapView.moveToArtObject(artObjectId);
    getFragmentManager()
      .beginTransaction()
      .replace(R.id.fragment_container, MovePinFragment.create(artObjectId))
      .addToBackStack(null)
      .commit();
  }

  @Override
  public void onExitMoveMode() {
    mapView.clearPinToPlace();
  }

  @Override
  public void onPinRotationChange(int rotation) {
    mapView.setPinToPlaceRotation(rotation);
  }

  @Override
  public void onPinMoveAccept(String artObjectId, int rotation) {
    ArtObject artObject = MyApplication.idToArtObject.get(artObjectId);
    MyApplication.artObjectLocationUpdateRecorder.record(artObject, mapView.getPinLocation(), rotation);

    ArrayList<ArtObjectLocation> newLocations = new ArrayList<>();
    for (ArtObjectLocation artObjectLocation : mapView.getLocations()) {
      if (artObjectLocation.getArtObjectId().equals(artObjectId)) {
        newLocations.add(new ArtObjectLocation(
          artObjectLocation.getArtObjectId(),
          artObjectLocation.getPosition(),
          artObject.getLocationX() * density,
          artObject.getLocationY() * density,
          artObject.getRotation(),
          true
        ));
      } else {
        newLocations.add(artObjectLocation);
      }
    }

    Collections.sort(newLocations);

    mapView.setPins(newLocations);

    getFragmentManager()
      .popBackStack();
  }

  @Override
  public void onReportArtObjectMissingSelected(String artObjectId) {
    MyApplication.artObjectMissingRecorder.record(artObjectId);
    Toast.makeText(getApplicationContext(), "Thank you for the report!", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onAddStopSelected(int galleryId) {
    ArrayList<ArtObjectLocation> newLocations = new ArrayList<>(mapView.getLocations());
    // NOTE: Add fake pin location to Map for new stop.
    newLocations.add(0, new ArtObjectLocation("", 0, 0, 0, 0, true));
    mapView.setPins(newLocations);
    mapView.setPinToPlace(0);
    mapView.setPinToPlaceRotation(0);
    getFragmentManager()
      .beginTransaction()
      .replace(R.id.fragment_container, AddStopFragment.create(galleryId))
      .addToBackStack(null)
      .commit();
  }

  @Override
  public void onAddStopAcceptListener(int gallery, int stopId, int rotation) {
    PointF location = mapView.getPinLocation();
    MyApplication.addStopRecorder.record(gallery, stopId, location.x, location.y, rotation);
    Toast.makeText(getApplicationContext(), "Stop Added", Toast.LENGTH_LONG).show();
    getFragmentManager()
      .popBackStack();
  }

  @Override
  public void onAddStopDestroy() {
    ArrayList<ArtObjectLocation> newLocations = new ArrayList<>(mapView.getLocations());
    newLocations.remove(0);
    mapView.setPins(newLocations);
    mapView.clearPinToPlace();
    mapView.setPinToPlaceRotation(0);
  }

  @Override
  public void onAddStopPinRotationChange(int rotation) {
    mapView.setPinToPlaceRotation(rotation);
  }
}

