package com.example.omkar.guesswhat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.example.omkar.guesswhat.R.id.answer;

public class AddQuestion extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER =  2;

    // Database reference objects
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mQnADatabaseReference;
    private ChildEventListener mChildEventListener;

    // Firebase Storage Object
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mQuestionPhotosStorageReference;

    // variables
    private String question = null;
    private ArrayList<String> ansList;
    private String photoUrl = null;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        // Database objects instantiated
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");

        // Storage objects initialized
        mFirebaseStorage = FirebaseStorage.getInstance();
        mQuestionPhotosStorageReference = mFirebaseStorage.getReference().child("question_images");

        // Instantiate answer list
        ansList = new ArrayList<>();

        // Layout references
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        final Button addAns = findViewById(R.id.addAns);
        Button upload = findViewById(R.id.upload);
        final EditText questionField = findViewById(R.id.question);
        final EditText ansField = findViewById(answer);
        final TextView ansListField = findViewById(R.id.ansListTView);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });


        // add answer to answer list
        addAns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get answer and add to list
                String ans = ansField.getText().toString().toLowerCase();
                ansList.add(ans);
                String ansListTxt = ansListField.getText().toString();

                // display answer list
                if (ansListTxt.equals("")){
                    ansListTxt = ans;
                }
                else{
                    ansListTxt += (", " + ans);
                }
                ansListField.setText(ansListTxt);
                ansField.setText("");
            }
        });



        // Upload QnA object to firebase
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // disable touch on entire activity
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                // get question from Question field
                question = questionField.getText().toString();


                // check parameters are filled
                if (photoUri != null && question != null && ansList.size() != 0){

                    // Upload picture to firebase storage
                    StorageReference photoRef = mQuestionPhotosStorageReference.child(photoUri.getLastPathSegment());
                    photoRef.putFile(photoUri).addOnSuccessListener(AddQuestion.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get uri from from firebase storage
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            photoUrl = downloadUrl.toString();

                            // upload to firebase real time database
                            QnA qAndA = new QnA(question, photoUrl, ansList, ansList.size());
                            mQnADatabaseReference.push().setValue(qAndA).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    finish();
                                }
                            });
                        }
                    });

                }
                else{
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(AddQuestion.this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // Listener for keyboard enter
        ansField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addAns.performClick();
                }
                return false;
            }
        });
    }

    // Overriding onActivityResult for handling photo picker
    // this method gets called before the onResume method of the activity lifecycle
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            // get image uri
            Uri selectImageUri = data.getData();
            photoUri = selectImageUri;
            // display image
            ImageView photoImageView = findViewById(R.id.imgViewer);
            Glide.with(photoImageView.getContext())
                    .load(selectImageUri.toString())
                    .into(photoImageView);
        }
    }
}
