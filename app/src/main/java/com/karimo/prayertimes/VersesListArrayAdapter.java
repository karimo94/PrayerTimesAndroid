package com.karimo.prayertimes;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class VersesListArrayAdapter extends ArrayAdapter<QuranObject.Verse> {

    private static final String TAG = "VersesArrayAdapter";
    private List<QuranObject.Verse> versesList = new ArrayList<>();

    boolean ar_only = false;
    boolean translateOnly = false;
    boolean translitOnly = false;
    boolean displayAll = false;

    static class VersesListViewHolder {
        TextView verseId;
        TextView arabicText;
        TextView transliteration;
        TextView translation;
    }
    public VersesListArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        loadQuranDisplayPref(context);
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
            viewHolder.translation = row.findViewById(R.id.verseTranslation);
            row.setTag(viewHolder);
        } else {
            viewHolder = (VersesListViewHolder) row.getTag();
        }

        QuranObject.Verse verse = getItem(position);
        viewHolder.verseId.setText(Integer.toString(verse.id));
        viewHolder.arabicText.setText(verse.text);
        viewHolder.transliteration.setText(verse.transliteration);
        viewHolder.translation.setText(verse.translation);
        //conditional rendering
        if(ar_only) {
            viewHolder.translation.setVisibility(View.INVISIBLE);
            viewHolder.transliteration.setVisibility(View.INVISIBLE);
        }
        if(translateOnly) {
            viewHolder.transliteration.setVisibility(View.INVISIBLE);
        }
        if(translitOnly) {
            viewHolder.translation.setVisibility(View.INVISIBLE);
        }
        return row;
    }
    private void loadQuranDisplayPref(Context ctx) {
        String quranDisplayMode = PreferenceManager.getDefaultSharedPreferences(ctx).getString("myQuranDisplayPref", "display_all");
        quranDisplayMode = new Gson().fromJson(quranDisplayMode, String.class);
        switch (quranDisplayMode) {
            case "arabic":
                ar_only = true;
                break;
            case "translate":
                translateOnly = true;
                break;
            case "transliteration":
                translitOnly = true;
                break;
            default:
                displayAll = true;
                break;
        }
    }
}
