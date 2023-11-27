package com.karimo.prayertimes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class SurahsListArrayAdapter extends ArrayAdapter<QuranObject> {
    private static final String TAG = "SurahsArrayAdapter";
    private List<QuranObject> surahsList = new ArrayList<QuranObject>();

    static class SurahsListViewHolder {
        TextView surahId;
        TextView transliteration;
        TextView translation;
        TextView name;
    }

    public SurahsListArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void add(QuranObject object) {
        surahsList.add(object);
        super.add(object);
    }
    public void addAll(List<QuranObject> objects) {
        surahsList.addAll(objects);
    }
    @Override
    public int getCount() {
        return this.surahsList.size();
    }
    @Override
    public QuranObject getItem(int index) {
        return this.surahsList.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SurahsListViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.surahs_list_view_item, parent, false);
            viewHolder = new SurahsListViewHolder();
            viewHolder.surahId = (TextView) row.findViewById(R.id.surahId);
            viewHolder.transliteration = (TextView) row.findViewById(R.id.surahTransliteration);
            viewHolder.translation = (TextView) row.findViewById(R.id.surahTranslation);
            viewHolder.name = (TextView) row.findViewById(R.id.surahName);
            row.setTag(viewHolder);
        } else {
            viewHolder = (SurahsListViewHolder)row.getTag();
        }

        QuranObject surah = getItem(position);
        viewHolder.surahId.setText(Integer.toString(surah.id));
        viewHolder.transliteration.setText(surah.transliteration);
        viewHolder.translation.setText(surah.translation);
        viewHolder.name.setText(surah.name);
        return row;
    }
}
