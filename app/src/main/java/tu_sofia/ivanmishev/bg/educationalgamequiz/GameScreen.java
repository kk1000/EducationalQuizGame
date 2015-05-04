package tu_sofia.ivanmishev.bg.educationalgamequiz;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.ListIterator;


public class GameScreen extends Activity {

    //session points
    private int sessionPoints = 0;

    Button fiftyButton, friendButton, crowdButton;
    TextView sessionPointsText, randomPointsForCurrentAnswerText, sessionQuestionCounterText, questionText;
    Button aButton, bButton, cButton, dButton;

    private MyData db;
    private Cursor questionsCursor;
    private LinkedList<Question> questionsInList;
    ListIterator iterator;
    private int questionNumber = 1;

    int start = Color.rgb(0x00, 0x99, 0x00);
    int end = Color.rgb(0x00, 0xff, 0x00);

    private ValueAnimator va;

    private final MyHandler mHandler = new MyHandler(this);

    private Button mButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        //Initializing app widgets for further use.
        fiftyButton = (Button) findViewById(R.id.fiftyButton);
        friendButton = (Button) findViewById(R.id.friendButton);
        crowdButton = (Button) findViewById(R.id.crowdButton);

        sessionPointsText = (TextView) findViewById(R.id.sessionPointsText);
        randomPointsForCurrentAnswerText = (TextView) findViewById(R.id.randomPointsForCurrentAnswerText);
        sessionQuestionCounterText = (TextView) findViewById(R.id.sessionQuestionCounterText);
        questionText = (TextView) findViewById(R.id.questionText);

        aButton = (Button) findViewById(R.id.aButton);
        bButton = (Button) findViewById(R.id.bButton);
        cButton = (Button) findViewById(R.id.cButton);
        dButton = (Button) findViewById(R.id.dButton);


        questionsInList = new LinkedList<>();

        va = ObjectAnimator.ofInt(mButton ,"backgroundColor", start, end);

        //open db
        db = new MyData(this);
        //populate cursor
        questionsCursor = db.getGameQuestions();

        //move cursor results to questionsList
        questionsCursor.moveToFirst();

        do {

            questionsInList.add(new Question(
                    questionsCursor.getString(questionsCursor.getColumnIndexOrThrow("Question")),
                    questionsCursor.getString(questionsCursor.getColumnIndexOrThrow("cAnswer")),
                    questionsCursor.getString(questionsCursor.getColumnIndexOrThrow("wAnswer1")),
                    questionsCursor.getString(questionsCursor.getColumnIndexOrThrow("wAnswer2")),
                    questionsCursor.getString(questionsCursor.getColumnIndexOrThrow("wAnswer3")),
                    questionsCursor.getInt(questionsCursor.getColumnIndexOrThrow("difficulty"))));


        } while (questionsCursor.moveToNext());

        iterator = (ListIterator) questionsInList.iterator();

        setCurrentQuestion();
    }

    //OnClick listener for game session
    public void checkAnswer(View view) {

        mButton = (Button) view;

        if (mButton.getText().toString().equals(questionsInList.get(0).getRightAnswer())) {
            //Toast.makeText(this, "Correct answer", Toast.LENGTH_SHORT).show();
            questionNumberIncrement();
            checkIfLastQuestion();

            mHandler.postDelayed(r, 1200);

            try {
                questionsInList.removeFirst();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            countSessionPoints();
            setCurrentQuestion();
            iterator = (ListIterator) questionsInList.iterator();
            //TODO check for last 50 question and after answer intent to EndGameScreen with proper message.

        } else {
            Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
            wrongAnswer();

        }


    }
    //TODO this method does not let user to answer to 50-th question and isEmpty never return true maybe because iterator
    public void checkIfLastQuestion(){
        if(questionsInList.isEmpty() || questionNumber == 50){
            endGame();
        }
    }

    public void setCurrentQuestion(){

        sessionPointsText.setText(" "+getSessionPoints());
        randomPointsForCurrentAnswerText.setText("+" + questionsInList.getFirst().getDifficulty() +"pts");
        sessionQuestionCounterText.setText(questionNumber + " question");
        questionText.setText(questionsInList.getFirst().getQuestion());
        aButton.setText(questionsInList.getFirst().getRightAnswer());
        bButton.setText(questionsInList.getFirst().getWrongAnswer1());
        cButton.setText(questionsInList.getFirst().getWrongAnswer2());
        dButton.setText(questionsInList.getFirst().getWrongAnswer3());

    }

    public void questionNumberIncrement(){
        this.questionNumber++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        questionsCursor.close();
        db.close();
        destroyGreenAnimation();
        mHandler.removeCallbacks(r);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public int getSessionPoints(){
        return sessionPoints;
    }

    //TODO maintaining session points from randomPointGeneratorForCurrentAnswer
    public void countSessionPoints() {

        sessionPoints = sessionPoints + questionsInList.getFirst().getDifficulty();
        //Toast.makeText(this, "Current points " + sessionPoints, Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences("gameScore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (getSessionPoints() > (sharedPreferences.getInt("gameScore",HomeScreen.DEFAULT_SCORE))){
            editor.putInt("gameScore", getSessionPoints());
            editor.commit();
            Toast.makeText(this, "Data was saved into shared pref folder", Toast.LENGTH_SHORT).show();
        }
    }

    public void wrongAnswer(){

        Intent intent = new Intent(this, WrongAnswerScreen.class);
        intent.putExtra("key","yey");
        intent.putExtra("currentPoints", getSessionPoints());
        intent.putExtra("questionNumber", this.questionNumber);
        intent.putExtra("currentQuestion", this.questionsInList.getFirst().getQuestion());
        intent.putExtra("answerForCurrentQuestion", this.questionsInList.getFirst().getRightAnswer());
        startActivity(intent);
        finish();

    }

    public void endGame(){
        Intent intent = new Intent(this, EndGameScreen.class);
        intent.putExtra("currentPoints", getSessionPoints());
        startActivity(intent);
        finish();
    }


    public void useMightyBlaster(View view) {

        //TODO Blast question with radiation

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Мощтния бластер");
        alertDialogBuilder
                .setMessage(questionsInList.getFirst().blastQuestion())
                .setCancelable(false)
                .setNegativeButton("ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        fiftyButton.setClickable(false);
        fiftyButton.setBackgroundResource(R.drawable.used);
        fiftyButton.setText("");


    }

    public void useWiseAlienButton(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Отговорът на мъдреца");
        alertDialogBuilder
                .setMessage(questionsInList.getFirst().askAlien())
                .setCancelable(false)
                .setNegativeButton("ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        friendButton.setClickable(false);
        friendButton.setText("X");

    }

    public void useConsortiumButton(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Решението на консорциума");
        alertDialogBuilder
                .setMessage(questionsInList.getFirst().askConsortium())
                .setCancelable(false)
                .setNegativeButton("ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        crowdButton.setClickable(false);
        crowdButton.setText("X");

    }

    private static class MyHandler extends Handler {
        private final WeakReference<GameScreen> mActivity;


        public MyHandler(GameScreen activity) {
            mActivity = new WeakReference<GameScreen>(activity);
        }

        public void handleMessage(Message msg) {
            GameScreen activity = mActivity.get();
            if (activity != null) {

            }
        }


    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 1200);
        }
    };

    public void setGreenAnimation(){
        //int start2 = Color.parseColor("#ff22ff");
        //int end2 = Color.parseColor("#3322ff");
        va.setDuration(75);
        va.setEvaluator(new ArgbEvaluator());
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.REVERSE);
        va.start();

        //va.cancel();
    }

    public void destroyGreenAnimation(){
        va.cancel();
    }

}