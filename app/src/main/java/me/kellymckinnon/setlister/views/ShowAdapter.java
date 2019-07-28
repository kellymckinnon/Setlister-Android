package me.kellymckinnon.setlister.views;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.OnClickListener;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.fragments.ListingFragment.OnSetlistSelectedListener;
import me.kellymckinnon.setlister.models.Show;

/** Adapter for ListingFragment to display show information in RecyclerView */
public class ShowAdapter extends Adapter {

  private final ArrayList<Show> mShowList;
  private final OnSetlistSelectedListener mOnSetlistSelectedListener;

  public ShowAdapter(OnSetlistSelectedListener onSetlistSelectedListener) {
    mShowList = new ArrayList<>();
    mOnSetlistSelectedListener = onSetlistSelectedListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View v =
        LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.show_list_row, viewGroup, false);
    return new ShowHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    ((ShowHolder) viewHolder).bindShow(mShowList.get(i));
  }

  @Override
  public int getItemCount() {
    return mShowList.size();
  }

  public void add(Show show) {
    mShowList.add(show);
    notifyItemInserted(getItemCount());
  }

  class ShowHolder extends RecyclerView.ViewHolder implements OnClickListener {

    final TextView band;
    final TextView venue;
    final TextView tour;
    final TextView date;
    Show show;
    final View itemView;
    final TextView numSongs;

    ShowHolder(View itemView) {
      super(itemView);
      this.itemView = itemView;
      band = itemView.findViewById(R.id.band);
      venue = itemView.findViewById(R.id.venue);
      tour = itemView.findViewById(R.id.tour);
      date = itemView.findViewById(R.id.date);
      numSongs = itemView.findViewById(R.id.num_songs);
    }

    void bindShow(Show show) {
      this.show = show;
      band.setText(show.getBand());
      venue.setText(show.getVenue());
      tour.setText(show.getTour());
      date.setText(show.getDate());
      numSongs.setText(
          numSongs
              .getContext()
              .getResources()
              .getQuantityString(
                  R.plurals.setlist_row_num_songs, show.getSongs().length, show.getSongs().length));

      if (show.getSongs().length == 0) {
        itemView.setOnClickListener(null);
        itemView.setBackgroundColor(Color.LTGRAY);
        band.setTextColor(Color.GRAY);
        venue.setTextColor(Color.GRAY);
        tour.setTextColor(Color.GRAY);
        date.setTextColor(Color.GRAY);
        numSongs.setTextColor(Color.GRAY);
      } else {
        Context context = itemView.getContext();
        itemView.setOnClickListener(this);
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.appBackground));
        band.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
        venue.setTextColor(ContextCompat.getColor(context, R.color.primaryText));
        tour.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));
        date.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        numSongs.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));
      }
    }

    @Override
    public void onClick(View view) {
      mOnSetlistSelectedListener.onSetlistSelected(show);
    }
  }
}
