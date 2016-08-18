package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.theyelllowdart.unofficialmetaudioguide.android.MyApplication;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Media;

import java.util.List;

public class ArtObjectFragment extends ListFragment {

  private OnMediaSelectListener mediaSelectCallback;
  private OnMovePinSelectListener movePinSelectCallback;
  private OnReportArtObjectMissingSelectListener reportArtObjectMissingCallback;
  private ArtObject artObject;
  private View detailView;
  private View header;
  private View overflowIcon;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mediaSelectCallback = (OnMediaSelectListener) context;
    movePinSelectCallback = (OnMovePinSelectListener) context;
    reportArtObjectMissingCallback = (OnReportArtObjectMissingSelectListener) context;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mediaSelectCallback = (OnMediaSelectListener) activity;
    movePinSelectCallback = (OnMovePinSelectListener) activity;
    reportArtObjectMissingCallback = (OnReportArtObjectMissingSelectListener) activity;
  }

  public void init(final String artObjectId) {
    artObject = MyApplication.idToArtObject.get(artObjectId);

    overflowIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenu().add("Move Pin");
        popup.getMenu().add("Report Missing");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            if (item.getTitle().equals("Move Pin")) {
              movePinSelectCallback.onMovePinSelected(artObject.getId());
            } else {
              reportArtObjectMissingCallback.onReportArtObjectMissingSelected(artObject.getId());
            }
            return true;
          }
        });
        popup.show();
      }
    });

    TextView pinNumber = (TextView) header.findViewById(R.id.objectImage1PinId);
    pinNumber.setText(String.valueOf(artObject.getPosition() + 1));

    TextView title = (TextView) header.findViewById(R.id.objectImage1Title);
    title.setText(artObject.getTitle());

    final ImageView imageView = (ImageView) header.findViewById(R.id.detailImage);
    Glide.with(getActivity())
      .load(artObject.getImageURL())
      .asBitmap()
      .diskCacheStrategy(DiskCacheStrategy.ALL)
      .fitCenter()
      .into(new BitmapImageViewTarget(imageView) {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
          super.onResourceReady(bitmap, anim);
          Palette
            .from(bitmap)
            .generate(new Palette.PaletteAsyncListener() {
              @Override
              public void onGenerated(Palette palette) {
                Palette.Swatch swatch = palette.getLightMutedSwatch();
                if (swatch != null) {
                  imageView.setBackgroundColor(swatch.getRgb());
                }
              }
            });
        }
      });

    MediaAdapter mediaAdapter = new MediaAdapter(
      this.getActivity().getApplicationContext(),
      android.R.layout.simple_list_item_1,
      artObject.getMedias()
    );
    setListAdapter(mediaAdapter);
    detailView.invalidate();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    detailView = inflater.inflate(R.layout.fragment_art_object, container, false);
    header = inflater.inflate(R.layout.fragment_art_object_header, null, false);
    View footer = inflater.inflate(R.layout.fragment_art_object_footer, null, false);
    overflowIcon = header.findViewById(R.id.overflowIcon);

    ListView listView = (ListView) detailView.findViewById(android.R.id.list);
    listView.setFooterDividersEnabled(true);
    listView.setHeaderDividersEnabled(true);
    listView.addHeaderView(header, null, false);
    listView.addFooterView(footer, null, false);

    init(getArguments().getString("artObjectId"));

    return detailView;
  }

  public static ArtObjectFragment create(String artObjectId) {
    Bundle bundle = new Bundle();
    bundle.putString("artObjectId", artObjectId);
    ArtObjectFragment fragment = new ArtObjectFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  public class MediaAdapter extends ArrayAdapter<Media> {

    public MediaAdapter(Context context, int resource, List<Media> objects) {
      super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_media, parent, false);
      }
      Media media = getItem(position);
      TextView mediaTitleView = (TextView) convertView.findViewById(R.id.mediaTitle);
      String brokenLinkText = media.getUrl() == null ? " (Broken Link) " : "";
      mediaTitleView.setText(media.getTitle() + brokenLinkText);
      TextView mediaStopView = (TextView) convertView.findViewById(R.id.mediaStop);
      mediaStopView.setText(String.valueOf(media.getStopId()));
      return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
      return artObject.getMedias().get(position).getUrl() != null;
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    // this is weird, header is position 0 unlike the adapter which doesn't include the header.
    mediaSelectCallback.onMediaSelected(artObject.getMedias().get(position - 1).getId());
  }

  public interface OnMediaSelectListener {
    void onMediaSelected(int mediaId);
  }

  public interface OnMovePinSelectListener {
    void onMovePinSelected(String artObjectId);
  }

  public interface OnReportArtObjectMissingSelectListener {
    void onReportArtObjectMissingSelected(String artObjectId);
  }
}
