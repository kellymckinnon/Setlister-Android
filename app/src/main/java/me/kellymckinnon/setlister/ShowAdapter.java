package me.kellymckinnon.setlister;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.OnClickListener;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by kelly on 12/21/14.
 */
public class ShowAdapter extends Adapter {

    private ArrayList<Show> data;
    private Context context;

    public ShowAdapter(Context context) {
        data = new ArrayList<Show>();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_row, viewGroup, false);
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
        ((ShowHolder) viewHolder).show = data.get(i);
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
        public TextView numSongs;
        public Show show;

        public ShowHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            band = (TextView) itemView.findViewById(R.id.band);
            venue = (TextView) itemView.findViewById(R.id.venue);
            tour = (TextView) itemView.findViewById(R.id.tour);
            date = (TextView) itemView.findViewById(R.id.date);
            numSongs = (TextView) itemView.findViewById(R.id.num_songs);
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
