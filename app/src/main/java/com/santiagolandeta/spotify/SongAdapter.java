package com.santiagolandeta.spotify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private final ArrayList<Song> songList;
    private static OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SongAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBind(songList.get(position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvSongName;
        public ImageView ivSongImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvCancion);
            ivSongImage = itemView.findViewById(R.id.ivListado);
            itemView.setOnClickListener(this);
        }

        public void onBind(Song song) {
            tvSongName.setText(song.getNombre());
            ivSongImage.setImageResource(song.getImagen());
        }


        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }
}

