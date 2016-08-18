package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.theyelllowdart.unofficialmetaudioguide.android.MyApplication;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;

public class MovePinFragment extends Fragment {

  private OnExitMoveModeListener exitMoveStateCallback;
  private OnPinRotationChangeListener onRotationCallback;
  private OnPinMoveAcceptListener onPinMoveAcceptCallback;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    exitMoveStateCallback = (OnExitMoveModeListener) context;
    onRotationCallback = (OnPinRotationChangeListener) context;
    onPinMoveAcceptCallback = (OnPinMoveAcceptListener) context;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    exitMoveStateCallback = (OnExitMoveModeListener) activity;
    onRotationCallback = (OnPinRotationChangeListener) activity;
    onPinMoveAcceptCallback = (OnPinMoveAcceptListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_move_pin, container, false);
    final String artObjectId = getArguments().getString("artObjectId");
    ArtObject artObject = MyApplication.idToArtObject.get(artObjectId);

    TextView heading = (TextView) view.findViewById(R.id.movePinHeading);
    heading.setText("Pin " + String.valueOf(artObject.getPosition() + 1));

    TextView subheading = (TextView) view.findViewById(R.id.movePinSubheading);
    subheading.setText(artObject.getTitle());

    final SeekBar rotation = (SeekBar) view.findViewById(R.id.seek_bar_rotation);
    final TextView degrees = (TextView) view.findViewById(R.id.degrees);
    rotation.setMax(359);
    rotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int rotation = progress - 180;
        onRotationCallback.onPinRotationChange(rotation);
        degrees.setText(String.valueOf(rotation) + "\u00B0");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    rotation.setProgress(Math.round(artObject.getRotation()) + 180);


    final Button accept = (Button) view.findViewById(R.id.btn_accept_move);
    accept.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onPinMoveAcceptCallback.onPinMoveAccept(artObjectId, rotation.getProgress() - 180);
      }
    });

    return view;
  }

  public static MovePinFragment create(String artObjectId) {
    Bundle bundle = new Bundle();
    bundle.putString("artObjectId", artObjectId);
    MovePinFragment fragment = new MovePinFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    exitMoveStateCallback.onExitMoveMode();
  }

  public interface OnExitMoveModeListener {
    void onExitMoveMode();
  }

  public interface OnPinRotationChangeListener {
    void onPinRotationChange(int rotation);
  }

  public interface OnPinMoveAcceptListener {
    void onPinMoveAccept(String artObjectId, int rotation);
  }
}
