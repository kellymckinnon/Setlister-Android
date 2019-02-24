package me.kellymckinnon.setlister.views;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import me.kellymckinnon.setlister.ListingActivity;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.SetlistActivity;
import me.kellymckinnon.setlister.models.Show;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.OnClickListener;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Adapter for ListingFragment to display show information in RecyclerView
 */
public class ShowAdapter extends Adapter {

    private ArrayList<Show> data;
    private Context context;

    public ShowAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.show_list_row, viewGroup, false);
        return new ShowHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        /* Get element from data set at this position and
        replace the contents of the view with that element */
        ((ShowHolder) viewHolder).band.setText(data.get(i).band);
        ((ShowHolder) viewHolder).venue.setText(data.get(i).venue);
        ((ShowHolder) viewHolder).tour.setText(data.get(i).tour);
        ((ShowHolder) viewHolder).date.setText(data.get(i).date);
        ((ShowHolder) viewHolder).numSongs.setText(data.get(i).setlist.length + " songs");
        ((ShowHolder) viewHolder).show = data.get(i);

        if (data.get(i).setlist.length == 0) {
            ((ShowHolder) viewHolder).itemView.setOnClickListener(null);
            ((ShowHolder) viewHolder).itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            ((ShowHolder) viewHolder).itemView.setOnClickListener(((ShowHolder) viewHolder));
            ((ShowHolder) viewHolder).itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Show show) {
        data.add(show);
        notifyItemInserted(getItemCount());
    }

    public class ShowHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public TextView band;
        public TextView venue;
        public TextView tour;
        public TextView date;
        View itemView;
        TextView numSongs;
        public Show show;

        ShowHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            band = itemView.findViewById(R.id.band);
            venue = itemView.findViewById(R.id.venue);
            tour = itemView.findViewById(R.id.tour);
            date = itemView.findViewById(R.id.date);
            numSongs = itemView.findViewById(R.id.num_songs);
        }

        @Override
        public void onClick(View view) {
            if (((ListingActivity) context).listClicked) {
                return;
            }

            ((ListingActivity) context).listClicked = true;
            Intent intent = new Intent(context, SetlistActivity.class);
            intent.putExtra("SONGS", show.setlist);
            intent.putExtra("ARTIST", show.band);
            intent.putExtra("DATE", show.date);
            intent.putExtra("VENUE", show.venue);
            intent.putExtra("TOUR", show.tour);
            context.startActivity(intent);
        }
    }
}
