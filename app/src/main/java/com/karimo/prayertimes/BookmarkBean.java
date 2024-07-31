package com.karimo.prayertimes;

public class BookmarkBean {
    public int id;
    public int surahId;
    public String surahNameTranslit;
    public String surahArName;
    public int verseId;
    public BookmarkBean(int surahId, String surahNameTranslit, String surahArName, int verseId) {
        this.surahId = surahId;
        this.surahNameTranslit = surahNameTranslit;
        this.surahArName = surahArName;
        this.verseId = verseId;
    }
}
