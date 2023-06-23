package com.zeyad.advancednote.Adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeyad.advancednote.DataBase.Entities.Note;
import com.zeyad.advancednote.Linstners.OnNoteClicked;
import com.zeyad.advancednote.databinding.NoteItemLayoutBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    List<Note> notes;
    OnNoteClicked listner;
    Timer timer;
    List<Note> resultNote;

    public NotesAdapter(List<Note> notes, OnNoteClicked listner) {
        this.notes = notes;
        this.listner = listner;
        resultNote = notes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteItemLayoutBinding binding = NoteItemLayoutBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.onBind(notes.get(position), position);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void SearchNotes(String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                if (keyword.trim().isEmpty()) {
                    notes = resultNote;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : resultNote) {
                        if (note.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                note.getSubtitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                note.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 200);
    }

    public void CancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    class NotesViewHolder extends RecyclerView.ViewHolder {
        NoteItemLayoutBinding binding;

        public NotesViewHolder(@NonNull NoteItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void onBind(Note note, int position) {

            binding.itemNoteTitle.setText(note.getTitle());
            if (binding.itemNoteDate.toString().isEmpty()) {
                Log.d("ahmed", "onBind: the date is empty");
            }
            binding.itemNoteDate.setText(note.getDateTime());
            if (note.getSubtitle().trim().isEmpty()) {
                binding.itemNoteSubtitle.setVisibility(View.GONE);
            }
            binding.itemNoteSubtitle.setText(note.getSubtitle());
            addColor2Note(note);

            if (note.getImagePath() != null) {
                binding.itemImage.setVisibility(View.VISIBLE);
                binding.itemImage.setImageBitmap(BitmapFactory
                        .decodeFile(note.getImagePath()));
            } else {
                binding.itemImage.setVisibility(View.GONE);
            }


            //OnNote Click Listner
            binding.getRoot().setOnClickListener(c -> listner.onClick(note, position));

        }

        private void addColor2Note(Note note) {
            GradientDrawable gradientDrawable = (GradientDrawable)
                    binding.itemLayout.getBackground();
            if (note.getColor() != null)
                gradientDrawable.setColor(Color.parseColor(note.getColor()));

        }
    }
}
