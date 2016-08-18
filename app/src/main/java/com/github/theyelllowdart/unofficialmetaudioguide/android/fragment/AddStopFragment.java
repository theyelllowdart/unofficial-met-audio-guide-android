package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.theyelllowdart.unofficialmetaudioguide.android.R;

public class AddStopFragment extends Fragment {

  private OnAddStopDestroyListener addStopDestroyCallback;
  private OnAddStopPinRotationChangeListener onAddStopPinRotationChangeCallback;
  private OnAddStopAcceptListener onStopAcceptCallback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    addStopDestroyCallback = (OnAddStopDestroyListener) context;
    onAddStopPinRotationChangeCallback = (OnAddStopPinRotationChangeListener) context;
    onStopAcceptCallback = (OnAddStopAcceptListener) context;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    addStopDestroyCallback = (OnAddStopDestroyListener) activity;
    onAddStopPinRotationChangeCallback = (OnAddStopPinRotationChangeListener) activity;
    onStopAcceptCallback = (OnAddStopAcceptListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_add_stop, container, false);

    final int galleryId = getArguments().getInt("galleryId");

    TextView heading = (TextView) view.findViewById(R.id.movePinHeading);
    heading.setText("Gallery " + String.valueOf(galleryId));

    TextView subHeading = (TextView) view.findViewById(R.id.movePinSubheading);
    subHeading.setText("Add Missing Stop");

    final SeekBar rotationSlider = (SeekBar) view.findViewById(R.id.seek_bar_rotation);
    final TextView degrees = (TextView) view.findViewById(R.id.degrees);
    rotationSlider.setMax(359);
    rotationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int rotation = progress - 180;
        onAddStopPinRotationChangeCallback.onAddStopPinRotationChange(rotation);
        degrees.setText(String.valueOf(rotation) + "\u00B0");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    rotationSlider.setProgress(180);

    final EditText stopTextView = (EditText) view.findViewById(R.id.text_stop_id);
    final Button accept = (Button) view.findViewById(R.id.btn_accept_move);

    stopTextView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
          accept.setEnabled(true);
        }
      }
    });

    accept.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int stopId = Integer.parseInt(stopTextView.getText().toString());
        int rotation = rotationSlider.getProgress() - 180;
        onStopAcceptCallback.onAddStopAcceptListener(galleryId, stopId, rotation);
      }
    });

    return view;
  }

  public static AddStopFragment create(int galleryId) {
    Bundle bundle = new Bundle();
    bundle.putInt("galleryId", galleryId);
    AddStopFragment fragment = new AddStopFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    addStopDestroyCallback.onAddStopDestroy();
  }

  public interface OnAddStopDestroyListener {
    void onAddStopDestroy();
  }

  public interface OnAddStopPinRotationChangeListener {
    void onAddStopPinRotationChange(int rotation);
  }

  public interface OnAddStopAcceptListener {
    void onAddStopAcceptListener(int gallery, int stopId, int rotation);
  }
}
