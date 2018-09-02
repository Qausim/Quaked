package com.example.android.quaked;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HP on 6/18/2018.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    public ArrayList<Earthquake> earthquakes;
    private final ListItemClickListener mOnListItemClickListener;


    public ContentAdapter(ArrayList<Earthquake> earthquakes, ListItemClickListener listener) {
        this.earthquakes = earthquakes;
        mOnListItemClickListener = listener;
    }


    // Create a custom ViewHolder class that extends RecyclerView.ViewHolder
    public class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Initialize the view objects in the layout for each item
        TextView magnitudeTextView, locationTextView;

        // Declare a public constructor of the custom ViewHolder class taking a view object as
        // a parameter.
        public ContentViewHolder(View itemView) {
            super(itemView);
            // Bind each view object in the layout using findViewById()
            magnitudeTextView = (TextView) itemView.findViewById(R.id.tv_magnitude);
            locationTextView = (TextView) itemView.findViewById(R.id.tv_location);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnListItemClickListener.onListItemClick(clickedPosition);
        }
    }


    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get context from parent
        Context context = parent.getContext();
        int layoutForItemsId = R.layout.content_list_item;
        // Initialize a LayoutInflater object
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachToParent = false;


        View view = inflater.inflate(layoutForItemsId, parent, attachToParent);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, int position) {
        Earthquake earthquake = earthquakes.get(position);
        holder.locationTextView.setText(earthquake.getLocation());
        holder.magnitudeTextView.setText(earthquake.getMagnitude());
    }

    @Override
    public int getItemCount() {
        return earthquakes.size();
    }


    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }
}
