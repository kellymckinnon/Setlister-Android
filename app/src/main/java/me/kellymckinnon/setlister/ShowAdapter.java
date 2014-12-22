package me.kellymckinnon.setlister;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.*;

/**
 * Created by kelly on 12/21/14.
 */
public class ShowAdapter extends Adapter {
    private ArrayList<Show> data;

    public ShowAdapter(ArrayList<Show> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
        return new ShowHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        /* Get element from dataset at this position and
        replace the contents of the view with that element */
        ((ShowHolder) viewHolder).band.setText(data.get(i).band);
        ((ShowHolder) viewHolder).venue.setText(data.get(i).venue);
        ((ShowHolder) viewHolder).tour.setText(data.get(i).tour);
        ((ShowHolder) viewHolder).date.setText(data.get(i).date);
        ((ShowHolder) viewHolder).numSongs.setText(data.get(i).setlist.length + " songs");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ShowHolder extends RecyclerView.ViewHolder {
        public TextView band;
        public TextView venue;
        public TextView tour;
        public TextView date;
        public TextView numSongs;

        public ShowHolder(View itemView) {
            super(itemView);
            band = (TextView) itemView.findViewById(R.id.band);
            venue = (TextView) itemView.findViewById(R.id.venue);
            tour = (TextView) itemView.findViewById(R.id.tour);
            date = (TextView) itemView.findViewById(R.id.date);
            numSongs = (TextView) itemView.findViewById(R.id.num_songs);
        }
    }
}
