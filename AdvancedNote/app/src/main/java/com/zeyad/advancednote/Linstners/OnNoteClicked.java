package com.zeyad.advancednote.Linstners;

import com.zeyad.advancednote.DataBase.Entities.Note;

public interface OnNoteClicked {

    void onClick(Note note , int notePosition);
}
