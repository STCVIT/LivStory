package com.yashkasera.livstoryuploaddata.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.yashkasera.livstoryuploaddata.R;
import com.yashkasera.livstoryuploaddata.UploadDataActivity;
import com.yashkasera.livstoryuploaddata.model.WordModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MissingWordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    Context context;
    ArrayList<WordModel> list;

    public MissingWordRecyclerAdapter(Context context, ArrayList<WordModel> map) {
        this.context = context;
        this.list = map;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM)
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false));
        else
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder)
            populateItemRows((ItemViewHolder) holder, position);
    }

    private void populateItemRows(ItemViewHolder holder, int position) {
        holder.word.setText(list.get(position).getWord());
        holder.count.setText(String.valueOf(list.get(position).getCount()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(ArrayList<WordModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView word;
        public Chip count;
        public View mView;

        public ItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
            word = itemView.findViewById(R.id.word);
            count = itemView.findViewById(R.id.count);
        }
    }
}
