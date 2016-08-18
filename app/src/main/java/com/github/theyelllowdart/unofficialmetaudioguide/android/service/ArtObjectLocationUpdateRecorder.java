package com.github.theyelllowdart.unofficialmetaudioguide.android.service;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;

import org.json.JSONException;
import org.json.JSONObject;


public class ArtObjectLocationUpdateRecorder {
  private final SQLiteDatabase db;
  private final RequestQueue requestQueue;
  private final String androidId;

  public ArtObjectLocationUpdateRecorder(SQLiteDatabase db, RequestQueue requestQueue, String androidId) {
    this.db = db;
    this.requestQueue = requestQueue;
    this.androidId = androidId;
  }

  public void record(ArtObject artObject, PointF location, float rotation) {
    artObject.setLocationX(location.x);
    artObject.setLocationY(location.y);
    artObject.setRotation(rotation);

    ContentValues contentValues = new ContentValues();
    contentValues.put("art_object_id", artObject.getId());
    contentValues.put("x", location.x);
    contentValues.put("y", location.y);
    contentValues.put("rotation", rotation);
    contentValues.put("uploaded", false);
    db.insert("user_art_object_location", null, contentValues);

    JSONObject json = new JSONObject();
    try {
      json.put("user", androidId);
      json.put("created", System.currentTimeMillis());
      json.put("x", location.x);
      json.put("y", location.y);
      json.put("rotation", rotation);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    JsonObjectRequest missingObjectRequest = new JsonObjectRequest(
      Request.Method.POST,
      "https://glacial-everglades-23026.herokuapp.com/art-object/" + artObject.getId() + "/location-report",
      json,
      new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

        }
      },
      new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
      });
    missingObjectRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 10, 2));
    requestQueue.add(missingObjectRequest);
  }
}
