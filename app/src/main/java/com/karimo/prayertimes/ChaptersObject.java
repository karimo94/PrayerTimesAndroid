package com.karimo.prayertimes;

import java.util.ArrayList;

public class ChaptersObject {
    public ArrayList<Chapter> chapters;
    public class Chapter {
        public int id;
        public String name;
        public String transliteration;
        public String translation;
        public String type;
        public int total_verses;
        public String link;
        @Override
        public String toString() {
            //we need to create a custom list view
            //5 columns
            return id + " " + transliteration + " " + translation + " " + name;
        }
    }

}
