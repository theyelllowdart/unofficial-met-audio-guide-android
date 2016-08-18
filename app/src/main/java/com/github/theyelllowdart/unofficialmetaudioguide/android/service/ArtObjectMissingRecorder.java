package com.github.theyelllowdart.unofficialmetaudioguide.android.service;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ArtObjectMissingRecorder {
  private final RequestQueue requestQueue;
  private final String androidId;

  public ArtObjectMissingRecorder(RequestQueue requestQueue, String androidId) {
    this.requestQueue = requestQueue;
    this.androidId = androidId;
  }

  public void record(String artObjectId) {
    JSONObject json = new JSONObject();
    try {
      json.put("user", androidId);
      json.put("created", System.currentTimeMillis());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    JsonObjectRequest missingObjectRequest = new JsonObjectRequest(
      Request.Method.POST,
      "https://glacial-everglades-23026.herokuapp.com/art-object/" + artObjectId + "/missing-report",
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
