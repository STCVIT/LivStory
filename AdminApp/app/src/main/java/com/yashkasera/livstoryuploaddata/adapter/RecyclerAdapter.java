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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    private final Context context;
    private final ArrayList<SoundModel> list;

    public RecyclerAdapter(Context context, ArrayList<SoundModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerAdapter.RecyclerViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getType());
        holder.chipGroup.removeAllViews();
        for (String keyword : list.get(position).getKeywords()) {
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
    public int getItemCount() {
        return list.size();
    }

    public void addItem(SoundModel soundModel) {
        list.add(soundModel);
        notifyItemInserted(list.size() - 1);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final ChipGroup chipGroup;
        public final ProgressBar progressBar;
        public final View mView;

        public RecyclerViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mView = itemView;
            textView = itemView.findViewById(R.id.textView);
            chipGroup = itemView.findViewById(R.id.chipGroup);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
