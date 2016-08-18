package com.github.theyelllowdart.unofficialmetaudioguide.android.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.theyelllowdart.unofficialmetaudioguide.android.R;

import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

public class DatabaseHelper extends SQLiteOpenHelper {
  private final Context context;

  public static final int DATABASE_VERSION = 2;
  public static final String DATABASE_NAME = "MetAudioGuide.db";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE gallery\n" +
      "(\n" +
      "    id INTEGER PRIMARY KEY NOT NULL,\n" +
      "    title VARCHAR NOT NULL,\n" +
      "    bound_x1 DOUBLE PRECISION,\n" +
      "    bound_y1 DOUBLE PRECISION,\n" +
      "    bound_x2 DOUBLE PRECISION,\n" +
      "    bound_y2 DOUBLE PRECISION,\n" +
      "    label_x DOUBLE PRECISION,\n" +
      "    label_y DOUBLE PRECISION\n" +
      ");"
    );

    db.execSQL("CREATE TABLE art_object\n" +
      "(\n" +
      "    id VARCHAR PRIMARY KEY NOT NULL,\n" +
      "    title VARCHAR NOT NULL,\n" +
      "    position INTEGER NOT NULL,\n" +
      "    gallery_id INTEGER NOT NULL,\n" +
      "    position_x REAL,\n" +
      "    position_y REAL,\n" +
      "    rotation REAL,\n" +
      "    image_url VARCHAR NOT NULL,\n" +
      "    image_width INTEGER NOT NULL,\n" +
      "    image_height INTEGER NOT NULL,\n" +
      "    CONSTRAINT gallery_id FOREIGN KEY (gallery_id) REFERENCES finished_gallery (id)\n" +
      ");"
    );

    db.execSQL("CREATE TABLE media\n" +
      "(\n" +
      "    id INTEGER PRIMARY KEY NOT NULL,\n" +
      "    url VARCHAR,\n" +
      "    title VARCHAR NOT NULL,\n" +
      "    position INTEGER NOT NULL,\n" +
      "    art_object_id VARCHAR NOT NULL,\n" +
      "    stop_id INTEGER NOT NULL,\n" +
      "    CONSTRAINT art_object_id FOREIGN KEY (art_object_id) REFERENCES finished_art_object (id)\n" +
      ");"
    );

    db.execSQL("CREATE TABLE user_art_object_location\n" +
      "(\n" +
      "    id INTEGER PRIMARY KEY NOT NULL,\n" +
      "    art_object_id TEXT NOT NULL,\n" +
      "    x REAL NOT NULL,\n" +
      "    y REAL NOT NULL,\n" +
      "    uploaded BOOLEAN NOT NULL,\n" +
      "    rotation REAL NOT NULL\n" +
      ");"
    );

    db.beginTransaction();
    try (CSVReader reader = new CSVReader(
      new InputStreamReader(context.getResources().openRawResource(R.raw.gallery)))) {
      String[] matches;
      while ((matches = reader.readNext()) != null) {
        ContentValues values = new ContentValues();
        values.put("id", Integer.parseInt(matches[0]));
        values.put("title", matches[1]);
        values.put("bound_x1", matches[2].equals("") ? null : Float.parseFloat(matches[2]));
        values.put("bound_y1", matches[3].equals("") ? null : Float.parseFloat(matches[3]));
        values.put("bound_x2", matches[4].equals("") ? null : Float.parseFloat(matches[4]));
        values.put("bound_y2", matches[5].equals("") ? null : Float.parseFloat(matches[5]));
        values.put("label_x", matches[6].equals("") ? null : Float.parseFloat(matches[6]));
        values.put("label_y", matches[7].equals("") ? null : Float.parseFloat(matches[7]));
        db.insert("gallery", null, values);
      }
      db.setTransactionSuccessful();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      db.endTransaction();
    }

    db.beginTransaction();
    try (CSVReader reader = new CSVReader(
      new InputStreamReader(context.getResources().openRawResource(R.raw.art_object)))) {
      String[] matches;
      while ((matches = reader.readNext()) != null) {
        ContentValues values = new ContentValues();
        values.put("id", matches[0]);
        values.put("title", matches[1]);
        values.put("position", Integer.parseInt(matches[2]));
        values.put("gallery_id", Integer.parseInt(matches[3]));
        values.put("position_x", matches[4].equals("") ? null : Float.parseFloat(matches[4]));
        values.put("position_y", matches[5].equals("") ? null : Float.parseFloat(matches[5]));
        values.put("rotation", matches[6].equals("") ? null : Float.parseFloat(matches[6]));
        values.put("image_url", matches[7]);
        values.put("image_width", Integer.parseInt(matches[8]));
        values.put("image_height", Integer.parseInt(matches[9]));
        db.insert("art_object", null, values);
      }
      db.setTransactionSuccessful();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      db.endTransaction();
    }

    db.beginTransaction();
    try (CSVReader reader = new CSVReader(
      new InputStreamReader(context.getResources().openRawResource(R.raw.media)))) {
      String[] matches;
      while ((matches = reader.readNext()) != null) {
        ContentValues values = new ContentValues();
        values.put("id", Integer.parseInt(matches[0]));
        values.put("url", matches[1].equals("") ? null : matches[1]);
        values.put("title", matches[2]);
        values.put("position", Integer.parseInt(matches[3]));
        values.put("art_object_id", matches[4]);
        values.put("stop_id", Integer.parseInt(matches[5]));
        db.insert("media", null, values);
      }
      db.setTransactionSuccessful();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS gallery");
    db.execSQL("DROP TABLE IF EXISTS art_object");
    db.execSQL("DROP TABLE IF EXISTS media");
    db.execSQL("DROP TABLE IF EXISTS user_add_stop");
    db.execSQL("DROP TABLE IF EXISTS user_art_object_location");
    db.execSQL("DROP TABLE IF EXISTS user_missing_art_object");
    db.execSQL("DROP TABLE IF EXISTS version");
    onCreate(db);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}
