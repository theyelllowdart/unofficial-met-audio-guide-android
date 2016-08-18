package com.github.theyelllowdart.unofficialmetaudioguide.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.theyelllowdart.unofficialmetaudioguide.android.MyApplication;
import com.github.theyelllowdart.unofficialmetaudioguide.android.R;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.ArtObject;
import com.github.theyelllowdart.unofficialmetaudioguide.android.model.Gallery;
import com.github.theyelllowdart.unofficialmetaudioguide.android.util.CenterTopTranformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GalleryFragment extends Fragment {
  private OnArtObjectSelectListener artObjectSelectCallback;
  private OnAddStopSelectListener addStopSelectCallback;
  private float density;
  private GridLayoutManager gridLayoutManager;
  private List<MiniArtObject> miniArtObjects;

  public void scrollToArtObject(String artObjectId) {
    Integer position = null;
    for (int i = 0; i < miniArtObjects.size(); i++) {
      MiniArtObject artObject = miniArtObjects.get(i);
      if (artObject.getId().equals(artObjectId)) {
        position = i + 1;
      }
    }
    if (position != null) {
      gridLayoutManager.scrollToPosition(position);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    artObjectSelectCallback = (OnArtObjectSelectListener) activity;
    addStopSelectCallback = (OnAddStopSelectListener) activity;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    artObjectSelectCallback = (OnArtObjectSelectListener) context;
    addStopSelectCallback = (OnAddStopSelectListener) context;
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    density = getResources().getDisplayMetrics().density;
    final Float padding = density * 4;

    View view = inflater.inflate(R.layout.fragment_gallery, container, false);


    int galleryId = getArguments().getInt("galleryId");
    GalleryFragmentModelAdapter modelAdapter = new GalleryFragmentModelAdapter(galleryId);
    miniArtObjects = modelAdapter.getMiniArtObjects();
    final MiniGalleryInfo miniGalleryInfo = modelAdapter.getMiniGallery();

    final HashMap<Integer, Integer> positionToSpanSize = new HashMap<>();
    for (int i = 0; i < miniArtObjects.size(); i++) {
      MiniArtObject artObject = miniArtObjects.get(i);
      if (!artObject.isLandscape() && i + 1 < miniArtObjects.size() && !miniArtObjects.get(i + 1).isLandscape()) {
        positionToSpanSize.put(i, 1);
        positionToSpanSize.put(i + 1, 1);
        i++;
      } else {
        positionToSpanSize.put(i, 2);
      }
    }

    final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        // Header
        if (position == 0) {
          return 2;
        } else {
          return positionToSpanSize.get(position - 1);
        }
      }
    };


    RecyclerView listView = (RecyclerView) view.findViewById(android.R.id.list);
    gridLayoutManager = new GridLayoutManager(getActivity(), 2);
    gridLayoutManager.setSpanSizeLookup(spanSizeLookup);

    listView.setLayoutManager(gridLayoutManager);
    listView.setAdapter(new MiniArtObjectAdapter(getActivity(), miniGalleryInfo, miniArtObjects, container.getWidth(), padding, spanSizeLookup, artObjectSelectCallback, density, addStopSelectCallback));
    listView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override
      public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int spanSize = spanSizeLookup.getSpanSize(position);
        int spanIndex = spanSizeLookup.getSpanIndex(position, 2);
        if (spanSize == 2) {
          outRect.left = Math.round(padding);
          outRect.right = Math.round(padding);
        } else {
          boolean even = spanIndex % 2 == 0;
          outRect.left = Math.round(even ? padding : padding / 2);
          outRect.right = Math.round(!even ? padding : padding / 2);
        }
        outRect.bottom = Math.round(padding / 2);
        outRect.top = Math.round(padding / 2);
      }
    });
    return view;
  }

  public static GalleryFragment create(int galleryId) {
    Bundle bundle = new Bundle();
    bundle.putInt("galleryId", galleryId);
    GalleryFragment fragment = new GalleryFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  private static class GalleryFragmentModelAdapter {
    private final int galleryId;

    public GalleryFragmentModelAdapter(int galleryId) {
      this.galleryId = galleryId;
    }

    public List<MiniArtObject> getMiniArtObjects() {
      Gallery gallery = MyApplication.idToGallery.get(galleryId);
      ArrayList<MiniArtObject> minis = new ArrayList<>();
      List<ArtObject> sorted = gallery.getArtObjects();
      Collections.sort(gallery.getArtObjects());
      for (ArtObject object : sorted) {
        MiniArtObject miniObject = new MiniArtObject(
          object.getId(),
          object.getImageURL(),
          object.getImageWidth(),
          object.getImageHeight(),
          object.getTitle(),
          object.getPosition()
        );
        minis.add(miniObject);
      }
      return minis;
    }

    public MiniGalleryInfo getMiniGallery() {
      Gallery gallery = MyApplication.idToGallery.get(galleryId);
      return new MiniGalleryInfo(gallery.getId(), gallery.getTitle());
    }
  }

  public static class MiniArtObjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private final MiniGalleryInfo miniGalleryInfo;
    private final List<MiniArtObject> artObjects;
    private final Context context;
    private final int width;
    private final Float padding;
    private final GridLayoutManager.SpanSizeLookup spanSizeLookup;
    private final OnArtObjectSelectListener selectCallback;
    private final float density;
    private final OnAddStopSelectListener addStopSelectCallback;

    public MiniArtObjectAdapter(Context context, MiniGalleryInfo miniGalleryInfo, List<MiniArtObject> artObjects, int width, Float padding, GridLayoutManager.SpanSizeLookup spanSizeLookup, OnArtObjectSelectListener selectCallback, float density, OnAddStopSelectListener addStopSelectCallback) {
      this.miniGalleryInfo = miniGalleryInfo;
      this.artObjects = artObjects;
      this.context = context;
      this.width = width;
      this.padding = padding;
      this.spanSizeLookup = spanSizeLookup;
      this.selectCallback = selectCallback;
      this.density = density;
      this.addStopSelectCallback = addStopSelectCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      if (viewType == TYPE_HEADER) {
        return new HeadHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_gallery_header, parent, false));
      } else {
        return new MiniArtObjectHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_art_object, parent, false));
      }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof HeadHolder) {
        HeadHolder headHolder = ((HeadHolder) holder);
        headHolder.heading.setText("Gallery " + String.valueOf(miniGalleryInfo.getGalleryId()));
        String galleryTitle = miniGalleryInfo.getTitle().replace('-', '\u2011');
        headHolder.title.setText(galleryTitle);
        headHolder.overflowIcon.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add("Add Missing Stop");
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem item) {
                addStopSelectCallback.onAddStopSelected(miniGalleryInfo.getGalleryId());
                return true;
              }
            });
            popup.show();
          }
        });
      } else {
        MiniArtObjectHolder miniHolder = (MiniArtObjectHolder) holder;
        final MiniArtObject artObject = artObjects.get(position - 1);
        miniHolder.title.setText(artObject.getTitle());
        miniHolder.pinId.setText(String.valueOf(artObject.getPosition() + 1));

        int spanSize = spanSizeLookup.getSpanSize(position);
        float itemPadding = spanSize == 2 ? (padding * 2) : (padding * 1.5f);
        int targetWidth = Math.round(((artObject.isLandscape()) ? width : width / 2.0f) - itemPadding);
        int targetHeight = Math.round((200 * density));

        miniHolder.imageView.getLayoutParams().width = targetWidth;
        miniHolder.imageView.getLayoutParams().height = targetHeight;

        Glide.with(context)
          .load(artObject.getImageURL())
          .asBitmap()
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .transform(new CenterTopTranformation(context))
          .override(targetWidth, targetHeight)
          .into(miniHolder.imageView);

        miniHolder.imageView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            selectCallback.onArtObjectSelected(artObject.id);
          }
        });
      }
    }

    @Override
    public int getItemCount() {
      int headerCount = 1;
      return headerCount + artObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
      if (position == 0) {
        return TYPE_HEADER;
      } else {
        return TYPE_ITEM;
      }
    }

    class HeadHolder extends RecyclerView.ViewHolder {
      private final TextView heading;
      private final TextView title;
      private final ImageView overflowIcon;

      public HeadHolder(View itemView) {
        super(itemView);
        heading = (TextView) itemView.findViewById(R.id.galleryListHeader);
        title = (TextView) itemView.findViewById(R.id.galleryTitle);
        overflowIcon = (ImageView) itemView.findViewById(R.id.overflowIcon);
      }
    }

    class MiniArtObjectHolder extends RecyclerView.ViewHolder {
      private final ImageView imageView;
      private final TextView pinId;
      private final TextView title;

      public MiniArtObjectHolder(View convertView) {
        super(convertView);
        imageView = (ImageView) convertView.findViewById(R.id.objectImage1);
        pinId = (TextView) convertView.findViewById(R.id.objectImage1PinId);
        title = (TextView) convertView.findViewById(R.id.objectImage1Title);
      }
    }
  }

  private static class MiniArtObject {
    private final String id;
    private final String imageURL;
    private final int imageWidth;
    private final int imageHeight;
    private final String title;
    private final int position;

    public MiniArtObject(String id, String imageURL, int imageWidth, int imageHeight, String title, int position) {
      this.id = id;
      this.imageURL = imageURL;
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
      this.title = title;
      this.position = position;
    }

    public String getId() {
      return id;
    }

    public String getImageURL() {
      return imageURL;
    }

    public int getImageWidth() {
      return imageWidth;
    }

    public int getImageHeight() {
      return imageHeight;
    }

    public String getTitle() {
      return title;
    }

    public int getPosition() {
      return position;
    }

    public boolean isLandscape() {
      return imageWidth / ((double) imageHeight) > 1.3;
    }
  }

  private static class MiniGalleryInfo {
    private final int galleryId;
    private final String title;

    public MiniGalleryInfo(int galleryId, String title) {
      this.galleryId = galleryId;
      this.title = title;
    }

    public int getGalleryId() {
      return galleryId;
    }

    public String getTitle() {
      return title;
    }
  }

  public interface OnArtObjectSelectListener {
    void onArtObjectSelected(String artObjectId);
  }

  public interface OnAddStopSelectListener {
    void onAddStopSelected(int galleryId);
  }
}