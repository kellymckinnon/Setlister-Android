package me.kellymckinnon.setlister.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.kellymckinnon.setlister.ListingActivity;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.SetlistActivity;
import me.kellymckinnon.setlister.models.Show;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.OnClickListener;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

/** Adapter for ListingFragment to display show information in RecyclerView */
public class ShowAdapter extends Adapter {

  private final ArrayList<Show> mShowList;
  private final Context mContext;

  public ShowAdapter(Context context) {
    mShowList = new ArrayList<>();
    this.mContext = context;
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
      band.setText(show.band);
      venue.setText(show.venue);
      tour.setText(show.tour);
      date.setText(show.date);
      numSongs.setText(
          numSongs
              .getContext()
              .getResources()
              .getQuantityString(
                  R.plurals.setlist_row_num_songs, show.setlist.length, show.setlist.length));

      if (show.setlist.length == 0) {
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
        venue.setTextColor(ContextCompat.getColor(context,  R.color.primaryText));
        tour.setTextColor(ContextCompat.getColor(context,  R.color.secondaryText));
        date.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        numSongs.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));
      }
    }

    @Override
    public void onClick(View view) {
      if (((ListingActivity) mContext).listClicked) {
        return;
      }

      ((ListingActivity) mContext).listClicked = true;
      Intent intent = new Intent(mContext, SetlistActivity.class);
      intent.putExtra("SONGS", show.setlist);
      intent.putExtra("ARTIST", show.band);
      intent.putExtra("DATE", show.date);
      intent.putExtra("VENUE", show.venue);
      intent.putExtra("TOUR", show.tour);
      mContext.startActivity(intent);
    }
  }
}
