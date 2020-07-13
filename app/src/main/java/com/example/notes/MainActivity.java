package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotes;
    private final List<Note> notes = new ArrayList<>();
    private NotesAdapter adapter;
    private NotesDBHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        dbHelper = new NotesDBHelper(this);

        database = dbHelper.getWritableDatabase();
//        database.delete(NotesContract.NotesEntry.TABLE_NAME, null, null);

        getData();

//        adapter = new NotesAdapter(notes);
        adapter = new NotesAdapter(notes);
        adapter.setOnNoteClickListener(new NotesAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(int position) {
               Toast.makeText(MainActivity.this, "Номер позиции" + position, Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onLongNoteClick(int position) {

                remove(position);

            }
        });

        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotes.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                remove(viewHolder.getAdapterPosition());

            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerViewNotes);

    }

    private void remove(int position){
        int id = notes.get(position).getId();
        String where = NotesContract.NotesEntry._ID + "=?";
        String[] whereArgs = new String[]{Integer.toString(id)};

        adapter.notifyDataSetChanged();
        database.delete(NotesContract.NotesEntry.TABLE_NAME, where, whereArgs);

        getData();
    }

    public void onClickAddNote(View view) {

        Intent intent = new Intent(this, addNoteActivity.class);
        startActivity(intent);

    }

    private void getData(){

        notes.clear();

        String selection = NotesContract.NotesEntry.COLUMN_PRIORITY + "<=?";
        String[] selectionArgs = new String[]{"2"};

        Cursor cursor = database.query(NotesContract.NotesEntry.TABLE_NAME, null, selection, selectionArgs, null, null, NotesContract.NotesEntry.COLUMN_DAY_OF_WEEK);

        while (cursor.moveToNext()){

            int id = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
            int dayOfWeek = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DAY_OF_WEEK));
            int priority = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_PRIORITY));

            notes.add(new Note(id, title, description, dayOfWeek, priority));

        }
        cursor.close();

    }

}