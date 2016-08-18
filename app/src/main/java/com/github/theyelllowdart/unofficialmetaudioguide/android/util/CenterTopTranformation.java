package com.github.theyelllowdart.unofficialmetaudioguide.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

public class CenterTopTranformation extends BitmapTransformation {

  public CenterTopTranformation(Context context) {
    super(context);
  }


  private Bitmap transform(Bitmap recycled, Bitmap toCrop, int width, int height) {
    if (toCrop == null) {
      return null;
    } else if (toCrop.getWidth() == width && toCrop.getHeight() == height) {
      return toCrop;
    }
    // From ImageView/Bitmap.createScaledBitmap.
    final float scale;
    float dx = 0;
    Matrix m = new Matrix();
    if (toCrop.getWidth() * height > width * toCrop.getHeight()) {
      scale = (float) height / (float) toCrop.getHeight();
      dx = (width - toCrop.getWidth() * scale) * 0.5f;
    } else {
      scale = (float) width / (float) toCrop.getWidth();
    }

    m.setScale(scale, scale);
    m.postTranslate((int) (dx + 0.5f), 0);
    final Bitmap result;
    if (recycled != null) {
      result = recycled;
    } else {
      result = Bitmap.createBitmap(width, height, toCrop.getConfig() != null ? toCrop.getConfig() : Bitmap.Config.ARGB_8888);
    }

    // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
    TransformationUtils.setAlpha(toCrop, result);

    Canvas canvas = new Canvas(result);
    Paint paint = new Paint(TransformationUtils.PAINT_FLAGS);
    canvas.drawBitmap(toCrop, m, paint);
    return result;
  }

  @Override
  protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
    final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null
      ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);

    Bitmap transformed = transform(toReuse, toTransform, outWidth, outHeight);
    if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
      toReuse.recycle();
    }
    return transformed;
  }

  @Override
  public String getId() {
    return "yo";
  }
}