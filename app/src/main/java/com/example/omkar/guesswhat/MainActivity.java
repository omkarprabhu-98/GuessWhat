package com.example.omkar.guesswhat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static android.os.Build.ID;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_OK =  2;

    // first object load indicator
    private static int FIRST_ENTRY_IN_DATABASE = 0;


    // game database
    private ArrayList<QnA> database;

    // game variables
    private String question;
    private int total;
    private HashMap <String, Integer> score = new HashMap<>();
    private int difficulty = 3;
    private Bitmap currentImage;


    // Current game object
    QnA currentQnA;

    // Database reference objects
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mQnADatabaseReference;
    private ChildEventListener mChildEventListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // preloader
        final LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        // Database objects instantiated
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");

        // loading game data
        loadQAndA();

    }



    @Override
    protected void onResume(){
        super.onResume();

        // button click handler for entering answer
        Button enter = (Button) findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ans=(EditText)findViewById(R.id.edit);
                checkAnswer(ans);
                ans.setText("");
            }
        });


        // button click handler for next question
        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!database.isEmpty()){
                    loadNewQandI();
                    FIRST_ENTRY_IN_DATABASE = 0;
                }
                else{
                    LinearLayout endOfQuestions = findViewById(R.id.endOfQuestions);
                    endOfQuestions.setVisibility(View.VISIBLE);
                }
            }
        });


        // Button click handler for upload after questions are over
        Button upload = findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddQuestion.class);
                startActivityForResult(i, RESULT_OK);
            }
        });
    }




    /**
     * Loads game if database is updated after adding a question
     * Else loads end of questions UI
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_OK) {

            if(!database.isEmpty()){
                loadNewQandI();
                FIRST_ENTRY_IN_DATABASE = 0;
            }
            else{
                LinearLayout endOfQuestions = findViewById(R.id.endOfQuestions);
                endOfQuestions.setVisibility(View.VISIBLE);
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.easy:
                difficulty = 3;
                displayImage(currentImage);
                return true;
            case R.id.medium:
                difficulty = 5;
                displayImage(currentImage);
                return true;
            case R.id.hard:
                difficulty = 7;
                displayImage(currentImage);
                return true;
            case R.id.extreme:
                difficulty = 9;
                displayImage(currentImage);
                return true;
            case R.id.addQ:
                Intent i = new Intent(MainActivity.this, AddQuestion.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**
     * Checks answer entered by the user
     */
    private void checkAnswer(EditText editText){
        TextView scoreTotal = (TextView) findViewById(R.id.scoreTotal);
        String ans = editText.getText().toString().trim().toLowerCase();
        String out;
        Log.d("Ans", ans);
        for(String log : currentQnA.getAnswer())
        {
            Log.d("Content",log);
        }
        if(((ArrayList)currentQnA.getAnswer()).contains(ans))
        {
            ((ArrayList)currentQnA.getAnswer()).remove(((ArrayList)currentQnA.getAnswer()).indexOf(ans));
            score.put(question, score.get(question) + 1);
            out = "Score: " + Integer.toString(score.get(question)) + "/Total: " + Integer.toString(total);
            scoreTotal.setText(out);
            if(currentQnA.getAnswer().isEmpty()){
                database.remove(currentQnA);
                final Button next = (Button) findViewById(R.id.next);
                next.performClick();
            }
        }
    }




    /**
     * Loads questions from firebase database into the
     */
    private void loadQAndA() {

        // Instantiate database
        database = new ArrayList<>();

        // Set childEventListener on mQnADatabaseReference
        // For actively listening to changes in firebase's real time database
        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    // add Messages the the list
                    QnA qAndA = dataSnapshot.getValue(QnA.class);
                    database.add(qAndA);

                    if (FIRST_ENTRY_IN_DATABASE == 0){
                        loadNewQandI();
                    }
                    FIRST_ENTRY_IN_DATABASE = 1;
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            // Event Listener for reading data from real time database
            mQnADatabaseReference.addChildEventListener(mChildEventListener);
        }
    }



    /**
     * Creates equal Bitmaps of the given Bitmap image
     * @param img
     * @return
     */
    private ArrayList<Bitmap> splitBitmap(Bitmap img) {

        GridView grid = (GridView) findViewById(R.id.grid);
        Bitmap picture = Bitmap.createScaledBitmap(img, grid.getWidth(), grid.getHeight(), true);

        //Number of rows
        int xCount = difficulty;

        //Number of columns
        int yCount = difficulty;

        ArrayList<Bitmap> imgs = new ArrayList<>();
        int width, height, k = 0;

        // Divide the original bitmap width by the desired vertical column count
        width = picture.getWidth() / xCount;

        // Divide the original bitmap height by the desired horizontal row count
        height = picture.getHeight() / yCount;

        // Loop the array and create bitmaps for each coordinate
        for (int x = 0; x < xCount; ++x) {
            for (int y = 0; y < yCount; ++y) {
                // Create the sliced bitmap
                imgs.add(Bitmap.createBitmap(picture, x * width, y * height, width, height));
                k++;
            }
        }

        // Randomly shuffle the array
        long seed = System.nanoTime();
        Collections.shuffle(imgs, new Random(seed));

        // Return the array
        return imgs;
    }



    /**
     * Load a new image after selecting a random question
     * The image is segmented and added to Image grid
     */
    private void loadNewQandI() {

        // end of questions is not visible
        LinearLayout endOfQuestions = findViewById(R.id.endOfQuestions);
        endOfQuestions.setVisibility(View.GONE);

        // preloader
        final LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        // initialize turn variables
        total = 0;

        // pick a question from set
        Random random = new Random(System.nanoTime());
        currentQnA = database.get(random.nextInt(database.size()));
        question = currentQnA.getQuestion();

        // display question text
        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);

        //get total answers for the question
        total = currentQnA.getNoOfAns();

        //if the question is accessed for the first time, add it to the array
        if(!score.containsKey(question)){
            score.put(question, 0);
        }

        // display game variables
        TextView scoreTotal = (TextView) findViewById(R.id.scoreTotal);
        String out = "Score: " + Integer.toString(score.get(question)) + "/Total: " + Integer.toString(total);
        scoreTotal.setText(out);


        // Background task for fetching image of a question
        new AsyncTask<Void, Void, Void>() {
            Bitmap btmp;
            @Override
            protected Void doInBackground(Void... params) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                try {

                    btmp = Glide.
                            with(MainActivity.this).
                            load(currentQnA.getPhotoUrl()).
                            asBitmap().
                            into(1000,1000).
                            get();
                    currentImage = btmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void dummy) {
                if (null != btmp) {
                    // Update the image of a question's Image View
//                    Log.d("CHECK", "IN TP");
                    if (btmp != null) {
                       displayImage(btmp);
                    }
                    // remove preloader
                    linlaHeaderProgress.setVisibility(View.GONE);
                }
            }
        }.execute();

//        Log.d("URL", currentQnA.getPhotoUrl());

    }

    private void displayImage(Bitmap btmp){
        // get segmented images
        ArrayList<Bitmap> imgs = splitBitmap(btmp);

        // add segmented images to the image grid
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new ImageAdapter(MainActivity.this, imgs));
        grid.setNumColumns((int) Math.sqrt(imgs.size()));
    }


    /**
     * Class to handle inserting images into GridView
     */
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ArrayList <Bitmap> imgs = new ArrayList<>();

        public ImageAdapter(Context c, ArrayList<Bitmap> imgs) {
            mContext = c;
            this.imgs = imgs;
        }

        public int getCount() {
            return imgs.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(imgs.get(position));
            return imageView;

        }
    }


}
