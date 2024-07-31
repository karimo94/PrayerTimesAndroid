package com.karimo.prayertimes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {
    Context myContext;
    ArrayList<BookmarkBean> bookmarkedVerses;
    BookmarkDb bookmarkDb;
    RecyclerItemClickListener myClickListener;
    public BookmarksAdapter(Context context, ArrayList allBookmarks) {
        myContext = context;
        bookmarkedVerses = allBookmarks;
        bookmarkDb = new BookmarkDb(myContext);
    }
    @NonNull
    @Override
    public BookmarksAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_list_view_item, parent, false);
        return new BookmarksAdapter.ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull BookmarksAdapter.ViewHolder holder, int position) {
        BookmarkBean current = bookmarkedVerses.get(position);
        holder.surah_name.setText(current.surahNameTranslit);
        holder.surah_ar_name.setText(current.surahArName);
        holder.verse_number.setText(Integer.toString(current.verseId));

        holder.delete_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open a modal to delete bookmark (are you sure)
                removeBookmarkDialog(current, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkedVerses.size();
    }

    private void removeBookmarkDialog(BookmarkBean bookmark, int position) {
        final TextView tx = new TextView(myContext.getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setTitle("Remove bookmark?");
        builder.setView(tx);
        builder.create();
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bookmarkDb.deleteBookmark(bookmark.surahId, bookmark.verseId);
                bookmarkedVerses.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(myContext, "No bookmark removed",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView surah_name, surah_ar_name, verse_number;
        ImageView delete_bookmark;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            surah_name = itemView.findViewById(R.id.bookmark_surah_name);
            surah_ar_name = itemView.findViewById(R.id.bookmark_surah_arabic);
            verse_number = itemView.findViewById(R.id.bookmark_surah_verse_id);
            delete_bookmark = itemView.findViewById(R.id.deleteBookmark);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (myClickListener != null) myClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }
    public void setRecyclerClickListener(RecyclerItemClickListener iListener) {
        myClickListener = iListener;
    }
    public BookmarkBean getItem(int id) {
        return bookmarkedVerses.get(id);
    }
    public interface RecyclerItemClickListener{
        void onItemClick(View view, int position);
    }
}
