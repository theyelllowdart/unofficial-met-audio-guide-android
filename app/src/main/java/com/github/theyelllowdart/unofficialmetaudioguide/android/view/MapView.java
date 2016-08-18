package com.github.theyelllowdart.unofficialmetaudioguide.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.github.theyelllowdart.unofficialmetaudioguide.android.MyApplication;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObjectLocation;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryLabel;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.GalleryViewRect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapView extends ImageView {
  private final float density;
  private final ArrayList<PaintGallery> paintRecs = new ArrayList<>();
  private final Paint textPaint;
  private final float[] imageMatrixValues = new float[9];
  private final Rect clipBounds = new Rect();
  private final RectF clipBoundsF = new RectF();
  private final float[] galleryTextDest = new float[2];
  private final RectF galleryBoundsDest = new RectF();
  private final RectF pinBounds = new RectF();
  private final Rect finalPinBounds = new Rect();
  private final Matrix verticalTextMatrix = new Matrix();
  private final Matrix horizontalTextMatrix = new Matrix();
  private final VectorDrawable pinDrawable;
  private ArrayList<DrawPin> pins = new ArrayList<>();
  private Integer pinToPlace = null;
  private PointF pinLocation = new PointF();
  private ArrayList<ArtObjectLocation> locations = new ArrayList<>();
  private RectF initialLayoutRectF = new RectF();
  private float[] initialLayoutMatrixValues = new float[9];

  private ScaleGestureDetector mScaleDetector;
  private GestureDetector gestureListener;
  private Matrix drawMatrix = new Matrix();

  private OnPinSelectListener pinSelectListener;
  private OnMapSelectListener mapSelectListener;

  private final Float pinWidth;
  private final Float pinHeight;
  private float pinPlaceRotation;
  private Float pinTextVerticalRatioOffset;
  private TextPaint pinTextPaint;


  public MapView(Context context, AttributeSet attrs) throws IOException {
    super(context, attrs);

    pinSelectListener = (OnPinSelectListener) context;
    mapSelectListener = (OnMapSelectListener) context;

    density = getResources().getDisplayMetrics().density;
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    gestureListener = new GestureDetector(context, new GestureListener());

    textPaint = new Paint();
    textPaint.setARGB(255, 88, 126, 146);
    textPaint.setStyle(Paint.Style.FILL);
    textPaint.setTextAlign(Paint.Align.CENTER);
    // textPaint.setAntiAlias(true);
    pinTextPaint = new TextPaint(textPaint);


    final Random random = new Random(0);
    for (GalleryViewRect rect : MyApplication.galleryRectById.values()) {
      final Paint paint = new Paint();
      paint.setColor(random.nextInt());
      paint.setStyle(Paint.Style.FILL);
      paint.setAlpha(90);
      GalleryLabel label = MyApplication.galleryLabels.get(rect.getId());
      if (label == null)
        throw new RuntimeException(String.valueOf(rect.getId()));
      paintRecs.add(new PaintGallery(rect, paint, MyApplication.galleryLabels.get(rect.getId())));
    }

    this.pinDrawable = (VectorDrawable) getResources().getDrawable(R.drawable.pin, null);
    float pinScalar = .022f;
    pinTextVerticalRatioOffset = .05f;
    pinWidth = pinDrawable.getIntrinsicWidth() * pinScalar;
    pinHeight = pinDrawable.getIntrinsicHeight() * pinScalar;
  }

  public interface OnPinSelectListener {
    void onPinSelected(String artObjectId);
  }

  public interface OnMapSelectListener {
    void onMapSelected(float x, float y);
  }

  private static class DrawPin {
    private final String artObjectId;
    private final int position;
    private final RectF rect;
    private final float rotation;
    private final boolean userPlaced;

    public DrawPin(String artObjectId, int position, RectF rect, float rotation, boolean userPlaced) {
      this.artObjectId = artObjectId;
      this.position = position;
      this.rect = rect;
      this.rotation = rotation;
      this.userPlaced = userPlaced;
    }

    public String getArtObjectId() {
      return artObjectId;
    }

    public int getPosition() {
      return position;
    }

    public RectF getRect() {
      return rect;
    }

    public float getRotation() {
      return rotation;
    }

    public boolean isUserPlaced() {
      return userPlaced;
    }
  }

  public ArrayList<ArtObjectLocation> getLocations() {
    return locations;
  }

  public void setPins(List<ArtObjectLocation> locations) {
    this.pins.clear();
    for (ArtObjectLocation location : locations) {
      RectF rect = new RectF(
        location.getX() - pinWidth / 2.0f,
        location.getY() - pinHeight,
        location.getX() + pinWidth / 2.0f,
        location.getY()
      );
      DrawPin pin = new DrawPin(
        location.getArtObjectId(),
        location.getPosition(),
        rect,
        location.getRotation(),
        location.isUserPlaced()
      );
      this.pins.add(pin);
    }
    this.locations.clear();
    this.locations.addAll(locations);
    this.invalidate();
  }

  public void clearPins() {
    this.pins.clear();
    this.invalidate();
  }

  public void clearPinToPlace() {
    this.pinToPlace = null;
    setPinToPlaceRotation(0);
    this.invalidate();
  }


  public void setPinToPlace(int pinToPlace) {
    this.pinToPlace = pinToPlace;
    this.invalidate();
  }

  public void setPinToPlaceRotation(float rotation) {
    this.pinPlaceRotation = rotation;
    invalidate();
  }

  public Integer getPinToPlace() {
    return this.pinToPlace;
  }

  public PointF getPinLocation() {
    Matrix inverted = new Matrix();
    drawMatrix.invert(inverted);
    float[] pts = new float[]{pinLocation.x, pinLocation.y};
    inverted.mapPoints(pts);
    return new PointF(pts[0] / density, pts[1] / density);
  }

  public Integer getPin(float x, float y) {
    for (int i = 0; i < pins.size(); i++) {
      RectF bound = pins.get(i).getRect();
      if (bound.contains(x, y)) {
        return i;
      }
    }
    return null;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (changed) {
      Drawable drawable = getDrawable();
      initialLayoutRectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
      zoomToRect(initialLayoutRectF);
      drawMatrix.getValues(initialLayoutMatrixValues);
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    setImageMatrix(drawMatrix);

    super.onDraw(canvas);


    drawMatrix.getValues(imageMatrixValues);
    textPaint.setTextSize(8 * density * imageMatrixValues[Matrix.MSCALE_X]);

    verticalTextMatrix.set(drawMatrix);
    verticalTextMatrix.postTranslate(imageMatrixValues[Matrix.MSCALE_X] * 10, 0);
    verticalTextMatrix.postRotate(90);

    horizontalTextMatrix.set(drawMatrix);
    horizontalTextMatrix.postTranslate(0, imageMatrixValues[Matrix.MSCALE_X] * 10);

    canvas.getClipBounds(clipBounds);
    clipBoundsF.set(clipBounds);
    for (PaintGallery paintGallery : paintRecs) {
      final GalleryViewRect rect = paintGallery.getRect();
      final Paint paint = paintGallery.getPaint();
      final GalleryLabel label = paintGallery.getLabel();

      final RectF scaled = rect.getScaled();
      drawMatrix.mapRect(galleryBoundsDest, scaled);

      if (RectF.intersects(galleryBoundsDest, clipBoundsF)) {
//        canvas.drawRect(galleryBoundsDest, paint);

        if (label.isHorizontal()) {
          horizontalTextMatrix.mapPoints(galleryTextDest, label.getCoord());
          canvas.drawText(label.getText(), galleryTextDest[0], galleryTextDest[1], textPaint);
        } else {
          canvas.save();
          canvas.rotate(-90);
          verticalTextMatrix.mapPoints(galleryTextDest, label.getCoord());
          canvas.drawText(label.getText(), galleryTextDest[0], galleryTextDest[1], textPaint);
          canvas.restore();
        }
      }
    }

    for (DrawPin pin : pins) {
      int alpha;
      if (pinToPlace != null) {
        if (pinToPlace == pin.getPosition()) {
          alpha = 255;
        } else {
          alpha = 128;
        }
      } else {
        alpha = 255;
      }

      float rotation;
      if (pinToPlace != null && pinToPlace == pin.getPosition()) {
        float scale = imageMatrixValues[Matrix.MSCALE_X];
        pinBounds.set(
          (getWidth() / 2f) - (scale * (pinWidth / 2f)),
          (getHeight() / 2f) - (scale * (pinHeight)),
          (getWidth() / 2f) + (scale * (pinWidth / 2f)),
          (getHeight() / 2f)
        );
        pinLocation.set(pinBounds.centerX(), pinBounds.bottom);
        rotation = pinPlaceRotation;
      } else {
        rotation = pin.getRotation();
        pinBounds.set(pin.getRect());
        drawMatrix.mapRect(pinBounds);
      }
      pinBounds.round(finalPinBounds);
      pinDrawable.setBounds(finalPinBounds);
      pinDrawable.setAlpha(alpha);


      if (rotation != 0) {
        canvas.save();
        canvas.rotate(rotation, finalPinBounds.centerX(), finalPinBounds.bottom);
      }
      pinDrawable.draw(canvas);

      if (pin.userPlaced) {
        pinTextPaint.setColor(Color.RED);
      } else {
        pinTextPaint.setColor(Color.BLUE);
      }
      pinTextPaint.setAlpha(alpha);
      pinTextPaint.setShadowLayer(4f, 1.5f, 1.3f, Color.argb(120, 0, 0, 0));
      pinTextPaint.setTextSize(4 * density * imageMatrixValues[Matrix.MSCALE_X]);
      pinTextPaint.setAntiAlias(true);

      float textX = finalPinBounds.centerX();
      float textY = finalPinBounds.centerY() - (finalPinBounds.height() * pinTextVerticalRatioOffset);
      canvas.drawText(
        String.valueOf(pin.getPosition()),
        textX,
        textY,
        pinTextPaint
      );

      if (rotation != 0) {
        canvas.restore();
      }
    }
  }

  private static class PaintGallery {
    private final GalleryViewRect rect;
    private final Paint paint;
    private final GalleryLabel label;

    public PaintGallery(GalleryViewRect rect, Paint paint, GalleryLabel label) {
      this.rect = rect;
      this.paint = paint;
      this.label = label;
    }

    public GalleryViewRect getRect() {
      return rect;
    }

    public Paint getPaint() {
      return paint;
    }

    public GalleryLabel getLabel() {
      return label;
    }
  }


  // Borrowed from
  // http://stackoverflow.com/questions/19418878/implementing-pinch-zoom-and-drag-using-androids-build-in-gesture-listener-and-s
  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    float lastFocusX;
    float lastFocusY;

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      lastFocusX = detector.getFocusX();
      lastFocusY = detector.getFocusY();
      return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      Matrix transformationMatrix = new Matrix();
      float focusX = detector.getFocusX();
      float focusY = detector.getFocusY();

      transformationMatrix.postTranslate(-focusX, -focusY);
      transformationMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());

      float focusShiftX = focusX - lastFocusX;
      float focusShiftY = focusY - lastFocusY;
      transformationMatrix.postTranslate((focusX + focusShiftX), focusY + focusShiftY);
      drawMatrix.postConcat(transformationMatrix);
      lastFocusX = focusX;
      lastFocusY = focusY;
      invalidate();
      return true;
    }

  }

  public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent,
                            float distanceX, float distanceY) {
      float speedMultiplier = 1f;
      drawMatrix.postTranslate(-distanceX * speedMultiplier, -distanceY * speedMultiplier);
      invalidate();
      return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (pinToPlace == null) {
        Matrix inverse = new Matrix();
        getImageMatrix().invert(inverse);
        float[] absolutePoints = new float[]{e.getX(), e.getY()};
        inverse.mapPoints(absolutePoints);
        float x = absolutePoints[0];
        float y = absolutePoints[1];
        String artObjectId = null;
        for (DrawPin pin : pins) {
          if (pin.getRect().contains(x, y)) {
            artObjectId = pin.artObjectId;
          }
        }
        if (artObjectId != null) {
          pinSelectListener.onPinSelected(artObjectId);
        } else {
          mapSelectListener.onMapSelected(x, y);
        }
      }
      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      float[] drawMatrixValues = new float[9];
      drawMatrix.getValues(drawMatrixValues);
      float scale = drawMatrixValues[Matrix.MSCALE_X];

      float low = initialLayoutMatrixValues[Matrix.MSCALE_X];
      float med = low * 2.8f;
      float high = med * 2.8f;

      float targetScale;
      if (scale < low) {
        targetScale = low;
      } else if (scale < med) {
        targetScale = med;
      } else if (scale < high) {
        targetScale = high;
      } else {
        targetScale = low;
      }

      float postScaleRatio = targetScale / scale;
      drawMatrix.postScale(postScaleRatio, postScaleRatio, e.getX(), e.getY());
      invalidate();
      return true;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mScaleDetector.onTouchEvent(event);
    gestureListener.onTouchEvent(event);
    return true;
  }

  public void zoomToRect(RectF zoomCover) {
    RectF imageSize = new RectF(0, 0, getWidth(), getHeight());
    Matrix newMatrix = new Matrix();
    float minSize = 90 * density;
    float padding = 10 * density;
    float sizeX = Math.max(minSize, zoomCover.width() + padding);
    float sizeY = Math.max(minSize, zoomCover.height() + padding);
    RectF newViewPort = new RectF(
      (zoomCover.centerX() - sizeX / 2),
      (zoomCover.centerY() - sizeY / 2),
      (zoomCover.centerX() + sizeX / 2),
      (zoomCover.centerY() + sizeY / 2)
    );
    newMatrix.setRectToRect(newViewPort, imageSize, Matrix.ScaleToFit.CENTER);
    drawMatrix = newMatrix;
    invalidate();
  }

  public void moveToArtObject(String artObjectId) {
    for (DrawPin pin : pins) {
      if (pin.getArtObjectId().equals(artObjectId)) {
        float targetX = pin.getRect().centerX();
        float targetY = pin.getRect().bottom;
        zoomToRect(new RectF(targetX, targetY, targetX, targetY));
      }
    }
  }
}

