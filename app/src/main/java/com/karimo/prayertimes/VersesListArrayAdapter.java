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

public class VersesListArrayAdapter extends ArrayAdapter<QuranObject.Verse> {

    private static final String TAG = "VersesArrayAdapter";
    private List<QuranObject.Verse> versesList = new ArrayList<>();

    static class VersesListViewHolder {
        TextView verseId;
        TextView arabicText;
        TextView transliteration;
    }
    public VersesListArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void add(QuranObject.Verse object) {
        versesList.add(object);
        super.add(object);
    }
    public void addAll(List<QuranObject.Verse> objects) {
        versesList.addAll(objects);
    }
    @Override
    public int getCount() {
        return this.versesList.size();
    }
    @Override
    public QuranObject.Verse getItem(int index) {
        return this.versesList.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        VersesListViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.verses_list_view_item, parent, false);
            viewHolder = new VersesListViewHolder();
            viewHolder.verseId = (TextView) row.findViewById(R.id.verseId);
            viewHolder.arabicText = (TextView) row.findViewById(R.id.verseText);
            viewHolder.transliteration = (TextView) row.findViewById(R.id.verseTransliteration);
            row.setTag(viewHolder);
        } else {
            viewHolder = (VersesListViewHolder) row.getTag();
        }

        QuranObject.Verse verse = getItem(position);
        viewHolder.verseId.setText(Integer.toString(verse.id));
        viewHolder.arabicText.setText(verse.text);
        viewHolder.transliteration.setText(verse.transliteration);
        return row;
    }
}
