package com.zeyad.advancednote.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.zeyad.advancednote.Adapters.NotesAdapter;
import com.zeyad.advancednote.DataBase.Entities.Note;
import com.zeyad.advancednote.DataBase.Room.NoteDB;
import com.zeyad.advancednote.Linstners.OnNoteClicked;
import com.zeyad.advancednote.R;
import com.zeyad.advancednote.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnNoteClicked {

    public static final String UPDATE_BUNDLE_NAME = "isUpdateOrView";
    public static final String PASS_NOTE_BUNDLE_NAME = "Selected_note";
    public static final String IS_FROM_QUICK_ACTIONS = "isFromQuickAdd";
    public static final String QUICK_TYPE = "quickType";
    public static final String QUICK_IMAGE_PATH = "imagePath";
    private static final int ADD_NOTE_REQ_CODE = 101;
    private static final int UPDATE_NOTE_REQ_CODE = 102;
    private static final int SHOW_NOTE_REQ_CODE = 103;
    private static final int READ_EXTERNAL_STORAGE_STATE = 60001;
    private static final int QUICK_ADD_IMAGE_REQ_CODE = 201;
    public static final String QUICK_URL = "quick_url";
    List<Note> noteList;
    NotesAdapter adapter;
    AlertDialog dialog;
    private ActivityMainBinding binding;
    private int notePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        binding.addNotes.setOnClickListener(c -> startActivityForResult(
                new Intent(MainActivity.this,
                        CreateNote.class), ADD_NOTE_REQ_CODE));

        AttachDataToRecyclerView();
        searchNote();
        HandleQuickActions();


        getNotes(SHOW_NOTE_REQ_CODE, false);
    }


    private void HandleQuickActions() {
        binding.quickAddNote.setOnClickListener(c -> startActivityForResult(
                new Intent(MainActivity.this,
                        CreateNote.class), ADD_NOTE_REQ_CODE));

        binding.quickAddImage.setOnClickListener(c -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_STATE);
            } else {
                addImageFromGallery();
            }
        });
        binding.quickAddUrl.setOnClickListener(c -> {
                initAddUrlDialouge();
        });


    }

    private void initAddUrlDialouge() {
        AlertDialog.Builder urlAlert = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_url_layout,
                (ViewGroup) findViewById(R.id.urlDialougeLayout));
        urlAlert.setView(view);
        dialog = urlAlert.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputUrl = view.findViewById(R.id.enterUrl);
        inputUrl.requestFocus();

        final ImageView imageError = view.findViewById(R.id.imgError);

        view.findViewById(R.id.addUrl).setOnClickListener(c -> {
            if (inputUrl.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "The Url Can't Be Empty", Toast.LENGTH_SHORT)
                        .show();

            } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                imageError.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Wrong Url Format , Please A valid input"
                        , Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                Intent quickUrl = new Intent(MainActivity.this , CreateNote.class);
                quickUrl.putExtra(IS_FROM_QUICK_ACTIONS , true);
                quickUrl.putExtra(QUICK_TYPE , "url");
                quickUrl.putExtra(QUICK_URL ,inputUrl.getText().toString());
                startActivityForResult(quickUrl , ADD_NOTE_REQ_CODE);

            }
        });
        view.findViewById(R.id.Cancel).setOnClickListener(c ->
                dialog.dismiss()
        );

        dialog.show();

    }

    @SuppressLint("QueryPermissionsNeeded")
    private void addImageFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (gallery.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(gallery, QUICK_ADD_IMAGE_REQ_CODE);
        }
    }

    private void searchNote() {
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.CancelTimer();
                if (count == 3) {
                    Toast.makeText(getBaseContext(), "You Searched More Than 2 Times " +
                            "Without matter", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    adapter.SearchNotes(s.toString());

                }
            }
        });
    }


    private void AttachDataToRecyclerView() {
        noteList = new ArrayList<>();
        adapter = new NotesAdapter(noteList, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2
                , StaggeredGridLayoutManager.VERTICAL));
        binding.recyclerView.setHasFixedSize(true);
    }


    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class getNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDB.getInstance(getApplicationContext())
                        .noteDAO().getNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == SHOW_NOTE_REQ_CODE) {
                    noteList.addAll(notes);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_NOTE_REQ_CODE) {
                    noteList.add(notePosition, notes.get(notePosition));
                    adapter.notifyItemInserted(notePosition);
                } else if (requestCode == UPDATE_NOTE_REQ_CODE) {
                    noteList.remove(notePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(notePosition);
                    } else {
                        noteList.add(notePosition, notes.get(notePosition));
                        adapter.notifyItemChanged(notePosition);
                    }
                }

            }
        }
        new getNotesTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addImageFromGallery();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                getNotes(ADD_NOTE_REQ_CODE, false);
            }
        } else if (requestCode == UPDATE_NOTE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    getNotes(UPDATE_NOTE_REQ_CODE, data.getBooleanExtra(
                            CreateNote.IS_NOTE_DELETED_BUNDLE_NAME, false));
                }
            }
        } else if (requestCode == QUICK_ADD_IMAGE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    Intent quickImage = new Intent(MainActivity.this
                            , CreateNote.class);
                    quickImage.putExtra(IS_FROM_QUICK_ACTIONS, true);
                    quickImage.putExtra(QUICK_TYPE, "image");
                    quickImage.putExtra(QUICK_IMAGE_PATH, getImagePath(uri));
                    startActivityForResult(quickImage, ADD_NOTE_REQ_CODE);

                }
            }
        }
    }

    private String getImagePath(Uri uri) {
        String imagePath;
        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(uri, null, null
                , null, null);
        if (cursor == null) {
            imagePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            imagePath = cursor.getString(index);
        }
        return imagePath;
    }

    @Override
    public void onClick(Note note, int notePosition) {
        this.notePosition = notePosition;
        Intent update = new Intent(MainActivity.this, CreateNote.class);
        update.putExtra(UPDATE_BUNDLE_NAME, true);
        update.putExtra(PASS_NOTE_BUNDLE_NAME, note);
        startActivityForResult(update, UPDATE_NOTE_REQ_CODE);

    }
}