package com.example.omkar.guesswhat;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

        // button click handler
        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewQandI();
            }
        });


    }

    /**
     * Loads questions, its answers and corresponding images to two Hash Maps
     */
    private void loadQAndA (){

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
            while((lineQ = inQ.readLine()) != null) {

                // question
                String question = lineQ.trim();

                noOfQuestions++;

                // get no of answers for that questions
                lineQ = inQ.readLine();
                int noOfAnswers = Integer.parseInt(lineQ.trim());

                // store answers in a list
                ArrayList<String> answerList = new ArrayList<>();
                for (int i = 0; i < noOfAnswers; i++){

                    lineA = inA.readLine();
                    String answer = lineA.trim();
                    answerList.add(answer);
                }
                // add to hash map
                qAndA.put(question, answerList);
                // select image and add to hash map
                qAndI.put(question,(getResources().getIdentifier("picture"+ noOfQuestions, "drawable", "com.example.omkar.guesswhat")));

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
     * Creates 9 equal Bitmaps of the given Bitmap image
     * @param picture
     * @return
     */
    private Bitmap[] splitBitmap(Bitmap picture){
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, 240, 240, true);
        Bitmap[] imgs = new Bitmap[9];
        // Selects coordinates and widths of the larger bitmap to create smaller part
        imgs[0] = Bitmap.createBitmap(scaledBitmap, 0, 0, 80 , 80);
        imgs[1] = Bitmap.createBitmap(scaledBitmap, 80, 0, 80, 80);
        imgs[2] = Bitmap.createBitmap(scaledBitmap, 160, 0, 80,80);
        imgs[3] = Bitmap.createBitmap(scaledBitmap, 0, 80, 80, 80);
        imgs[4] = Bitmap.createBitmap(scaledBitmap, 80, 80, 80,80);
        imgs[5] = Bitmap.createBitmap(scaledBitmap, 160, 80,80,80);
        imgs[6] = Bitmap.createBitmap(scaledBitmap, 0, 160, 80,80);
        imgs[7] = Bitmap.createBitmap(scaledBitmap, 80, 160,80,80);
        imgs[8] = Bitmap.createBitmap(scaledBitmap, 160,160,80,80);
        return imgs;

    }

    /**
     * Load a new image after selecting a random question
     * The image is segmented and added to Image grid
     */
    private void loadNewQandI(){

        // pick a question from set
        ArrayList<String> keys = new ArrayList<>(qAndI.keySet());
        Random random = new Random();
        question = keys.get(random.nextInt(keys.size()));
        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);

        // get image for a random question
        Bitmap btmp = BitmapFactory.decodeResource(getResources(), qAndI.get(question));

        // get segmented images
        Bitmap [] imgs = splitBitmap(btmp);

        // add segmented images to the image grid
        ImageView imageView1 = (ImageView) findViewById(R.id.img1);
        imageView1.setImageBitmap(imgs[3]);

        ImageView imageView2 = (ImageView) findViewById(R.id.img2);
        imageView2.setImageBitmap(imgs[8]);

        ImageView imageView3 = (ImageView) findViewById(R.id.img3);
        imageView3.setImageBitmap(imgs[2]);

        ImageView imageView4 = (ImageView) findViewById(R.id.img4);
        imageView4.setImageBitmap(imgs[5]);

        ImageView imageView5 = (ImageView) findViewById(R.id.img5);
        imageView5.setImageBitmap(imgs[0]);

        ImageView imageView6 = (ImageView) findViewById(R.id.img6);
        imageView6.setImageBitmap(imgs[7]);

        ImageView imageView7 = (ImageView) findViewById(R.id.img7);
        imageView7.setImageBitmap(imgs[4]);

        ImageView imageView8 = (ImageView) findViewById(R.id.img8);
        imageView8.setImageBitmap(imgs[1]);

        ImageView imageView9 = (ImageView) findViewById(R.id.img9);
        imageView9.setImageBitmap(imgs[6]);

    }
}
