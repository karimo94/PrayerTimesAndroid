package com.karimo.prayertimes;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class QuranObject {
    public int id;
    public String name;
    public String transliteration;
    public String translation;
    public String type;
    public int total_verses;
    public ArrayList<Verse> verses;

    @Override
    public String toString() {
        //we need to create a custom list view
        //5 columns
        return id + " " + transliteration + " " + translation + " " + name;
    }
    public String toStringSimple() {
        return transliteration + " " + name;
    }

    public class Verse{
        public int id;
        public String text;
        //public String translation;
        public String transliteration;

    }

}
