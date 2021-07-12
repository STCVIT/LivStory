package com.yashkasera.livstoryuploaddata.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.yashkasera.livstoryuploaddata.R;
import com.yashkasera.livstoryuploaddata.model.SoundModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.yashkasera.livstoryuploaddata.MainActivity.soundModelArrayList;

public class SoundRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public SoundRecyclerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound, parent, false);
            return new ItemViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder)
            populateItemRows((ItemViewHolder) holder, position);
    }

    private void populateItemRows(ItemViewHolder holder, int position) {
        holder.textView.setText(soundModelArrayList.get(position).getType());
        holder.chipGroup.removeAllViews();
        for (String keyword : soundModelArrayList.get(position).getKeywords()) {
            Chip chip = new Chip(context);
            chip.setText(keyword);
            chip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.colorAccent)));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.chipBackgroundColor)));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context,
                    R.color.colorAccent)));
            chip.setTextStartPaddingResource(R.dimen.margin_medium);
            chip.setTextEndPaddingResource(R.dimen.margin_medium);
            chip.setChipStrokeWidthResource(R.dimen.strokeWidth);
            holder.chipGroup.addView(chip);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (soundModelArrayList.get(position) == null)
            return VIEW_TYPE_LOADING;
        else
            return soundModelArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return soundModelArrayList.size();
    }

    public void addItem(SoundModel soundModel) {
        soundModelArrayList.add(soundModel);
        notifyItemInserted(soundModelArrayList.size() - 1);
    }

    public void removeItem(SoundModel soundModel) {
        int position = soundModelArrayList.indexOf(soundModel);
        soundModelArrayList.remove(soundModel);
        notifyItemRemoved(position);
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final ChipGroup chipGroup;
        public final ProgressBar progressBar;
        public final View mView;

        public ItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
            textView = itemView.findViewById(R.id.textView);
            chipGroup = itemView.findViewById(R.id.chipGroup);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

}
