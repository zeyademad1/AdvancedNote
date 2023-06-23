package com.zeyad.advancednote.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zeyad.advancednote.DataBase.Entities.Note;
import com.zeyad.advancednote.DataBase.Room.NoteDB;
import com.zeyad.advancednote.R;
import com.zeyad.advancednote.databinding.ActivityCreateNoteBinding;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNote extends AppCompatActivity {
    public static final String IS_NOTE_DELETED_BUNDLE_NAME = "isNoteDeleted";
    private final int READ_EXTERNAL_STORAGE_STATE = 1;
    private final int ADD_IMAGE_REQ_CODE = 1000;
    ActivityCreateNoteBinding binding;
    private String noteColor = "#333333";
    private String selectedImagePath = "";
    private AlertDialog dialog;


    private Note returnedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        handleReturnedData();
        initMiscellaneous();
        WorkWithQuickActions();

        binding.noteDate.setText(new SimpleDateFormat("EEEE , dd MMMM yyyy HH:mm a"
                , Locale.getDefault()).format(new Date()));

        binding.imgBack.setOnClickListener(b -> onBackPressed());

        binding.imgDone.setOnClickListener(s -> saveNote());

        binding.deleteUrl.setOnClickListener(c -> {
            binding.textUrl.setText(null);
            binding.layoutWebUrl.setVisibility(View.GONE);
            binding.deleteUrl.setVisibility(View.GONE);
        });

        binding.deleteImage.setOnClickListener(c -> {
            binding.noteImage.setImageBitmap(null);
            binding.noteImage.setVisibility(View.GONE);
            selectedImagePath = "";
            binding.deleteImage.setVisibility(View.VISIBLE);
        });


    }

    private void WorkWithQuickActions() {
        String quickActionType;
        if (getIntent().getBooleanExtra(MainActivity.IS_FROM_QUICK_ACTIONS, false)) {
            quickActionType = getIntent().getStringExtra(MainActivity.QUICK_TYPE);
            if (quickActionType.equals("image")) {
                selectedImagePath = getIntent().getStringExtra(MainActivity.QUICK_IMAGE_PATH);
                binding.noteImage.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                binding.noteImage.setVisibility(View.VISIBLE);
            } else if (quickActionType.equals("url")) {
                binding.textUrl.setText(getIntent().getStringExtra(MainActivity.QUICK_URL));
                binding.layoutWebUrl.setVisibility(View.VISIBLE);
            }
        }
    }


    private void handleReturnedData() {
        if (getIntent().getBooleanExtra(MainActivity.UPDATE_BUNDLE_NAME, false)) {
            returnedNote = (Note) getIntent().getSerializableExtra(MainActivity.PASS_NOTE_BUNDLE_NAME);
            initViews(returnedNote);
        }

    }

    void initViews(@NonNull Note note) {
        binding.etTitle.setText(note.getTitle());
        binding.etDescription.setText(note.getDescription());
        binding.etSubtitle.setText(note.getSubtitle());

        if (note.getImagePath() != null && !note.getImagePath().trim().isEmpty()) {
            binding.noteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
            binding.noteImage.setVisibility(View.VISIBLE);
            binding.deleteImage.setVisibility(View.VISIBLE);
            selectedImagePath = note.getImagePath();
        }

        if (note.getWebLink() != null) {
            binding.textUrl.setText(note.getWebLink());
            binding.layoutWebUrl.setVisibility(View.VISIBLE);
            binding.deleteUrl.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote() {
        Note note = new Note();
        if (binding.etSubtitle.toString().trim().isEmpty()) {
            Toast.makeText(getBaseContext(), "Note Subtitle Can't Be Empty",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.etTitle.toString().trim().isEmpty()) {
            Toast.makeText(getBaseContext(), "Note Title Can't Be Empty",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.etDescription.toString().trim().isEmpty()) {
            Toast.makeText(getBaseContext(), "Note Description Can't Be Empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // AS we put in dao Conflict Strategy replace : It will replace the note with the new
        // note if the have the same id
        if (returnedNote != null) {
            note.setId(returnedNote.getId());
        }

        note.setSubtitle(binding.etSubtitle.getText().toString());
        note.setTitle(binding.etTitle.getText().toString());
        note.setDescription(binding.etDescription.getText().toString());
        note.setDateTime(binding.noteDate.getText().toString());
        if (noteColor != null) {
            note.setColor(noteColor);
        }
        if (selectedImagePath != null) {
            note.setImagePath(selectedImagePath);
        }
        if (binding.layoutWebUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(binding.textUrl.getText().toString());
        }


        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDB.getInstance(getApplicationContext()).noteDAO().InsertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }


    private void initMiscellaneous() {
        LinearLayout linearLayout = findViewById(R.id.layout_miscellaneous);
        BottomSheetBehavior<LinearLayout> behavior = BottomSheetBehavior.from(linearLayout);

        linearLayout.findViewById(R.id.txtMiscellaneous).setOnClickListener(c -> {
            if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        ImageView imageColor1 = linearLayout.findViewById(R.id.imgSelectedColor1);
        ImageView imageColor2 = linearLayout.findViewById(R.id.imgSelectedColor2);
        ImageView imageColor3 = linearLayout.findViewById(R.id.imgSelectedColor3);
        ImageView imageColor4 = linearLayout.findViewById(R.id.imgSelectedColor4);
        ImageView imageColor5 = linearLayout.findViewById(R.id.imgSelectedColor5);
        getViewColor(noteColor);

        linearLayout.findViewById(R.id.noteColor1).setOnClickListener(v -> {
            noteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            getViewColor(noteColor);
        });

        linearLayout.findViewById(R.id.noteColor2).setOnClickListener(v -> {
            noteColor = "#FBDE3B";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            getViewColor(noteColor);
        });

        linearLayout.findViewById(R.id.noteColor3).setOnClickListener(v -> {
            noteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            getViewColor(noteColor);
        });

        linearLayout.findViewById(R.id.noteColor4).setOnClickListener(v -> {
            noteColor = "#3A52FC";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done);
            imageColor5.setImageResource(0);
            getViewColor(noteColor);
        });

        linearLayout.findViewById(R.id.noteColor5).setOnClickListener(v -> {
            noteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done);
            getViewColor(noteColor);
        });

        if (returnedNote != null && !returnedNote.getColor().trim().isEmpty()
                && returnedNote.getColor() != null) {
            switch (returnedNote.getColor()) {
                case "#333333":
                    linearLayout.findViewById(R.id.noteColor1).performClick();
                    break;
                case "#FBDE3B":
                    linearLayout.findViewById(R.id.noteColor2).performClick();
                    break;
                case "#FF4842":
                    linearLayout.findViewById(R.id.noteColor3).performClick();
                    break;

                case "#3A52FC":
                    linearLayout.findViewById(R.id.noteColor4).performClick();
                    break;
                case "#000000":
                    linearLayout.findViewById(R.id.noteColor5).performClick();
                    break;
            }
        }

        linearLayout.findViewById(R.id.layout_addImage).setOnClickListener(c -> {
            if (ContextCompat.checkSelfPermission(CreateNote.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateNote.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_STATE);
            } else {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                addImageFromGallery();
            }
        });

        linearLayout.findViewById(R.id.layout_addUrl).setOnClickListener(c -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            initAddUrlDialouge();
        });


        linearLayout.findViewById(R.id.layout_deleteNote).setOnClickListener(c -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showDeleteDialouge();
        });

    }

    private void getViewColor(String colorString) {
        GradientDrawable gradientDrawable = (GradientDrawable)
                binding.viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(colorString));

    }

    @SuppressLint("QueryPermissionsNeeded")
    private void addImageFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (gallery.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(gallery, ADD_IMAGE_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addImageFromGallery();
            }
        } else {
            Toast.makeText(CreateNote.this, "Permission Denied!!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_IMAGE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.noteImage.setImageBitmap(bitmap);
                        binding.noteImage.setVisibility(View.VISIBLE);
                        binding.deleteImage.setVisibility(View.VISIBLE);
                        selectedImagePath = getImagePath(uri);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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


    private void initAddUrlDialouge() {
        AlertDialog.Builder urlAlert = new AlertDialog.Builder(CreateNote.this);
        View view = LayoutInflater.from(CreateNote.this).inflate(R.layout.add_url_layout,
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
                binding.layoutWebUrl.setVisibility(View.VISIBLE);
                binding.textUrl.setText(inputUrl.getText().toString());
                dialog.dismiss();

            }
        });
        view.findViewById(R.id.Cancel).setOnClickListener(c ->
                dialog.dismiss()
        );

        dialog.show();

    }

    void showDeleteDialouge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote.this);
        View view = LayoutInflater.from(CreateNote.this).inflate(R.layout.delete_note_layout,
                (ViewGroup) findViewById(R.id.delete_note_layout));
        builder.setView(view);
        AlertDialog deleteDialog = builder.create();
        if (deleteDialog.getWindow() != null) {
            deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.textDelete).setOnClickListener(c -> {
            new DeleteAsyncTask().execute();
            deleteDialog.dismiss();
        });

        view.findViewById(R.id.textCancel).setOnClickListener(c -> deleteDialog.dismiss());

        deleteDialog.show();
    }

    class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            NoteDB.getInstance(CreateNote.this).noteDAO().RemoveNote(returnedNote);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent delete = new Intent();
            delete.putExtra(IS_NOTE_DELETED_BUNDLE_NAME, true);
            setResult(RESULT_OK);
            finish();
        }
    }
}