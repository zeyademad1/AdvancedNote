package com.zeyad.advancednote.DataBase.NotesDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.zeyad.advancednote.DataBase.Entities.Note;

import java.util.List;

@Dao()
public interface NoteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertNote(Note note);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getNotes();

    @Delete()
    void RemoveNote(Note note);
}
