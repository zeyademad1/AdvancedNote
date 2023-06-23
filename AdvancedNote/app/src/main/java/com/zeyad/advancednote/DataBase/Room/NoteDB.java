package com.zeyad.advancednote.DataBase.Room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.zeyad.advancednote.DataBase.Entities.Note;
import com.zeyad.advancednote.DataBase.NotesDAO.NoteDAO;

@Database(entities = Note.class, exportSchema = false, version = 1)
public abstract class NoteDB extends RoomDatabase {
    private static final String NOTE_DATABASE_NAME = "notes_database";
    private static NoteDB instance;

    public synchronized static NoteDB getInstance(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, NoteDB.class
                    , NOTE_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract NoteDAO noteDAO();
}
