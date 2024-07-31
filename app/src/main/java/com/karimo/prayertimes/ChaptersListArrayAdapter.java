package com.karimo.prayertimes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChaptersListArrayAdapter extends ArrayAdapter<ChaptersObject.Chapter> {
    private static final String TAG = "ChaptersArrayAdapter";
    private List<ChaptersObject.Chapter> chaptersList = new ArrayList<>();
    static class ChaptersListViewHolder {
        TextView surahId;
        TextView transliteration;
        TextView translation;
        TextView name;
    }
    public ChaptersListArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
    @Override
    public void add(ChaptersObject.Chapter chapter) {
        chaptersList.add(chapter);
        super.add(chapter);
    }
    public void addAll(List<ChaptersObject.Chapter> otherList) {
        chaptersList.addAll(otherList);
    }
    @Override
    public int getCount() {
        return this.chaptersList == null ? 0 : this.chaptersList.size();
    }
    @Override
    public ChaptersObject.Chapter getItem(int index) {
        return this.chaptersList.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChaptersListArrayAdapter.ChaptersListViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.surahs_list_view_item, parent, false);
            viewHolder = new ChaptersListViewHolder();
            viewHolder.surahId = (TextView) row.findViewById(R.id.surahId);
            viewHolder.transliteration = (TextView) row.findViewById(R.id.surahTransliteration);
            viewHolder.translation = (TextView) row.findViewById(R.id.surahTranslation);
            viewHolder.name = (TextView) row.findViewById(R.id.surahName);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ChaptersListViewHolder) row.getTag();
        }

        ChaptersObject.Chapter chapter = getItem(position);
        viewHolder.surahId.setText(Integer.toString(chapter.id));
        viewHolder.transliteration.setText(chapter.transliteration);
        viewHolder.translation.setText(chapter.translation);
        viewHolder.name.setText(chapter.name);
        return row;
    }
    @Override
    public Filter getFilter() {
        // https://stackoverflow.com/questions/19122848/custom-getfilter-in-custom-arrayadapter-in-android
        return this.getFilter();
    }

}
