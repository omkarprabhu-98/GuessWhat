package com.example.omkar.guesswhat;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    HashMap<String, ArrayList<String>> qAndA;
    HashMap<String, Integer> qAndI;


    private String question;
    private int score;
    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // object creates
        qAndA = new HashMap<>();
        qAndI = new HashMap<>();

        // loading game data
        loadQAndA();
        loadNewQandI();

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
                checkAnswer(ans, qAndA);
                ans.setText("");
            }
        });


        // button click handler for next question
        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView scoreTotal = (TextView) findViewById(R.id.scoreTotal);
                String out = "Score: " + Integer.toString(score) + "/Total: " + Integer.toString(total);
                scoreTotal.setText(out);
                loadNewQandI();
            }
        });
    }

    /**
     * Checks answer entered by the user
     */
    private void checkAnswer(EditText editText, HashMap qAndA){
        TextView scoreTotal = (TextView) findViewById(R.id.scoreTotal);
        String ans = editText.getText().toString().trim().toLowerCase();
        String out;

        if(((ArrayList)qAndA.get(question)).contains(ans))
        {
            ((ArrayList)qAndA.get(question)).remove(((ArrayList)qAndA.get(question)).indexOf(ans));
            score++;
            out = "Score: " + Integer.toString(score) + "/Total: " + Integer.toString(total);
            scoreTotal.setText(out);
        }


    }


    /**
     * Loads questions, its answers and corresponding images to two Hash Maps
     */
    private void loadQAndA() {

        int noOfQuestions = -1;

        // get assetManager instance
        AssetManager assetManager = getAssets();
        try {
            // open input stream to questions and answers files
            InputStream inputStreamQ = assetManager.open("questions.txt");
            InputStream inputStreamA = assetManager.open("answers.txt");
            BufferedReader inQ = new BufferedReader(new InputStreamReader(inputStreamQ));
            BufferedReader inA = new BufferedReader(new InputStreamReader(inputStreamA));
            String lineQ = null;
            String lineA = null;


            // read questions
            while ((lineQ = inQ.readLine()) != null) {

                // question
                String question = lineQ.trim();

                noOfQuestions++;

                // get no of answers for that questions
                lineQ = inQ.readLine();
                int noOfAnswers = Integer.parseInt(lineQ.trim());

                // store answers in a list
                ArrayList<String> answerList = new ArrayList<>();
                for (int i = 0; i < noOfAnswers; i++) {

                    lineA = inA.readLine();
                    String answer = lineA.trim();
                    answerList.add(answer);
                }
                // add to hash map
                qAndA.put(question, answerList);
                // select image and add to hash map
                qAndI.put(question, (getResources().getIdentifier("picture" + noOfQuestions, "drawable", "com.example.omkar.guesswhat")));

            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load Questions", Toast.LENGTH_LONG);
            toast.show();
        }


//        // print hash Map for debugging
//        for (String name: qAndA.keySet()){
//            System.out.println("" + name);
//            ArrayList<String> answerList = new ArrayList<>();
//            answerList = qAndA.get(name);
//            int i = 0;
//            while (i < answerList.size()){
//                System.out.println(":  " + answerList.get(i));
//                i++;
//            }
//
//        }


        // update total score
        total = noOfQuestions;

    }


    /**
     * Creates equal Bitmaps of the given Bitmap image
     *
     * @param img
     * @return
     */
    private ArrayList<Bitmap> splitBitmap(Bitmap img) {

        Bitmap picture = Bitmap.createScaledBitmap(img, 650, 500, true);

        //Number of rows
        int xCount = 3;

        //Number of columns
        int yCount = 3;

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

        // pick a question from set
        ArrayList<String> keys = new ArrayList<>(qAndI.keySet());
        Random random = new Random();
        question = keys.get(random.nextInt(keys.size()));
        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);

        // get image for a random question
        Bitmap btmp = BitmapFactory.decodeResource(getResources(), qAndI.get(question));

        // get segmented images
        ArrayList<Bitmap> imgs = splitBitmap(btmp);

        // add segmented images to the image grid
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new ImageAdapter(this, imgs));
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
