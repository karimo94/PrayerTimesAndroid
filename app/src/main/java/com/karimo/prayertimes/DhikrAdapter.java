package com.karimo.prayertimes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DhikrAdapter extends RecyclerView.Adapter<DhikrAdapter.ViewHolder> {
    ArrayList<String> dhikrItems;
    public DhikrAdapter(ArrayList<String> allDhikr) {
        dhikrItems = allDhikr;
    }
    @NonNull
    @Override
    public DhikrAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dhikr_list_view_item, parent, false);
        return new DhikrAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DhikrAdapter.ViewHolder holder, int position) {
        String dhikrPiece = dhikrItems.get(position);
        holder.dhikr_content.setText(dhikrPiece);
    }

    @Override
    public int getItemCount() {
        return dhikrItems.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView dhikr_content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dhikr_content = itemView.findViewById(R.id.dhikrContent);
        }
    }
}

