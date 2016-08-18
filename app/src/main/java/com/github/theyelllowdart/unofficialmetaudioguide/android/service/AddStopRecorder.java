package com.github.theyelllowdart.unofficialmetaudioguide.android.service;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class AddStopRecorder {
  private final RequestQueue requestQueue;
  private final String androidId;

  public AddStopRecorder(RequestQueue requestQueue, String androidId) {
    this.requestQueue = requestQueue;
    this.androidId = androidId;
  }

  public void record(int galleryId, int stopId, float x, float y, float rotation) {
    JSONObject json = new JSONObject();
    try {
      json.put("user", androidId);
      json.put("created", System.currentTimeMillis());
      json.put("galleryId", galleryId);
      json.put("stopId", stopId);
      json.put("x", x);
      json.put("y", y);
      json.put("rotation", rotation);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    JsonObjectRequest missingObjectRequest = new JsonObjectRequest(
      Request.Method.POST,
      "https://glacial-everglades-23026.herokuapp.com/stops",
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
