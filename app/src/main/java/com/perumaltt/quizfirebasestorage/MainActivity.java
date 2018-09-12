package com.perumaltt.quizfirebasestorage;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.perumaltt.quizfirebasestorage.adapter.PhotoAdapter;
import com.perumaltt.quizfirebasestorage.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGES = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private PhotoAdapter adapter;
    private List<Photo> photos;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);

        photos = new ArrayList<>();

        storageReference = FirebaseStorage.getInstance().getReference("photos");
        databaseReference = FirebaseDatabase.getInstance().getReference("photos");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                photos.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Photo photo = postSnapshot.getValue(Photo.class);
                    photos.add(photo);
                }

                adapter = new PhotoAdapter(getApplicationContext(), photos);


                recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGES);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGES && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {

                int titleItemSelected = data.getClipData().getItemCount();

                for (int i = 0; i < titleItemSelected; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    final String fileName = getFileName(fileUri);
                    Log.d(TAG, "onActivityResult: " + fileName);

//                    StorageReference fileUpload = storageReference.child(fileName);
                    StorageReference fileUpload = storageReference.child(System.currentTimeMillis()
                            + "." + getFileExtension(fileUri));

                    fileUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            Log.d(TAG, "onSuccess: " + fileName + " upload successfully.");
                            Photo upload = new Photo(taskSnapshot.getDownloadUrl().toString());
                            String uploadId = databaseReference.push().getKey();
                            databaseReference.child(uploadId).setValue(upload);
                        }
                    });


                }

//                Snackbar.make(findViewById(android.R.id.content), "Here's a Multiple", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                final String fileName = getFileName(fileUri);
                Log.d(TAG, "onActivityResult: " + fileName);

//                    StorageReference fileUpload = storageReference.child(fileName);
                StorageReference fileUpload = storageReference.child(System.currentTimeMillis()
                        + "." + getFileExtension(fileUri));

                fileUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content), "Successfully", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.d(TAG, "onSuccess: " + fileName + " upload successfully.");
                        Photo upload = new Photo(taskSnapshot.getDownloadUrl().toString());
                        String uploadId = databaseReference.push().getKey();
                        databaseReference.child(uploadId).setValue(upload);
                    }
                });

//                Snackbar.make(findViewById(android.R.id.content), "Here's a Single", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != 1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
