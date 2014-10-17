package quiz.fw.com.quiz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import quiz.fw.com.Database.DatabaseHelper;
import quiz.fw.com.R;
import quiz.fw.com.controller.AppController;
import quiz.fw.com.utils.CommonFunctions;
import quiz.fw.com.utils.Constants;
import quiz.fw.com.utils.TimerService;

/**
 * Created by kaustubh on 9/10/14.
 */
public class QuizActivity extends Activity implements CompoundButton.OnCheckedChangeListener,View.OnClickListener {

  private DatabaseHelper db;
  private TextView txtQuizQuestion,txtQuizQuestionNo,txtQuestionProgress,txtNavLabel,txtTimer;
  private Button btnSubmit,btnSkip;
  private ImageButton btnNavigator;
  private ProgressBar progressbarQuestionStatus;
  private RadioButton radio0,radio1,radio2,radio3;
  private ProgressDialog pDialog;
  private String TAG = QuizActivity.class.getSimpleName();
  String correctAnswer,selectedAnswer="";
  Integer no_of_questions;
  RadioGroup rg;
  private List<Integer> ids = new ArrayList<Integer>();

  private TableLayout tableNavigatorLayout;
  private Animation slideRight, slideLeft,fade_in, fade_out;
  private FrameLayout frameNavigator;
  private RelativeLayout relativeNavigator;
  JSONObject resultJsonObj;
  private JSONArray responseArr,resultArr;
  private Integer questionCnt = 0;
  private Integer userID;
  private Integer qID;
  private Integer [] radioBtnId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.quiz);

    db = new DatabaseHelper(this);

    Intent extras = getIntent();
    if (extras != null) {
      userID = Integer.parseInt(extras.getStringExtra("userID"));
    }

    resultArr = new JSONArray();

    tableNavigatorLayout = (TableLayout) findViewById(R.id.tableNavigatorLayout);
    tableNavigatorLayout.removeAllViews();

    txtQuizQuestion = (TextView) findViewById(R.id.quiz_txtQuestion);
    txtQuizQuestionNo = (TextView) findViewById(R.id.quiz_txtQuestionNo);
    txtQuestionProgress = (TextView) findViewById(R.id.quiz_txtCurrentQuestion);
    txtTimer = (TextView) findViewById(R.id.quiz_txtTimer);
    txtNavLabel = (TextView) findViewById(R.id.quiz_txtNavLabel);
    txtNavLabel.setOnClickListener(this);

    progressbarQuestionStatus = (ProgressBar) findViewById(R.id.questionProgress);
    frameNavigator = (FrameLayout) findViewById(R.id.frameNavigator);
    relativeNavigator = (RelativeLayout) findViewById(R.id.navigatorLayout);

    rg = (RadioGroup) findViewById(R.id.quiz_rgOpinion);

    radio0 = (RadioButton) findViewById(R.id.radio0);
    radio0.setOnCheckedChangeListener(this);
    radio1 = (RadioButton) findViewById(R.id.radio1);
    radio1.setOnCheckedChangeListener(this);
    radio2 = (RadioButton) findViewById(R.id.radio2);
    radio2.setOnCheckedChangeListener(this);
    radio3 = (RadioButton) findViewById(R.id.radio3);
    radio3.setOnCheckedChangeListener(this);

    btnSubmit = (Button) findViewById(R.id.quiz_btnSubmit);
    btnSubmit.setOnClickListener(this);
    btnSkip = (Button) findViewById(R.id.quiz_btnSkip);
    btnSkip.setOnClickListener(this);

    if(findViewById(R.id.quiz_btnNavSwitch) != null){
      btnNavigator = (ImageButton) findViewById(R.id.quiz_btnNavSwitch);
      btnNavigator.setOnClickListener(this);
    }

    pDialog = new ProgressDialog(this);
    pDialog.setMessage("Loading...");
    pDialog.setCancelable(false);

    fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

    slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left);
    slideLeft.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        btnNavigator.setVisibility(View.VISIBLE);
        btnNavigator.startAnimation(fade_in);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });

    slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right);
    slideRight.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        btnNavigator.setVisibility(View.VISIBLE);
        btnNavigator.startAnimation(fade_in);
        frameNavigator.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });

    registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION));
    if(savedInstanceState == null) {
      makeJsonArrayReq();
      if(!CommonFunctions.isServiceRunning(TimerService.class,this)) {
        Intent intentService = new Intent(this, TimerService.class);
        intentService.putExtra("minutes",1);
        startService(intentService);
      }
    }
  }

  public void radioTextColorChange(boolean b, RadioButton radioBtn,String separator){
    if(b) {
      String[] ans = radioBtn.getText().toString().split(separator);
      selectedAnswer= ans[1];
      radioBtn.setTextColor(getResources().getColor(R.color.white));
    }else {
      radioBtn.setTextColor(getResources().getColor(R.color.black));
    }
  }

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      String time = intent.getStringExtra("time");
      if(!time.equals("finish")){
        txtTimer.setText(time);
      }else{
        startCompletionActivity();
      }
    }
  };

  @Override
  public void onClick(View view) {
    try{
      switch (view.getId()) {
        case R.id.quiz_btnSubmit:
          if(!selectedAnswer.isEmpty()) {
            resultJsonObj.put("id",qID);
            radioBtnId[questionCnt] = rg.getCheckedRadioButtonId();
            questionCnt++;
            if (selectedAnswer.trim().equals(correctAnswer)) {
              resultJsonObj.put("answer","correct");
            } else {
              resultJsonObj.put("answer","wrong");
            }
            resultJsonObj.remove("status");
            resultJsonObj.put("status","taken");
            selectedAnswer = "";
          }
          break;

        case R.id.quiz_btnSkip:
          resultJsonObj.put("id",qID);
          radioBtnId[questionCnt] = 0;
          questionCnt++;
          selectedAnswer = "";
          resultJsonObj.remove("status");
          resultJsonObj.put("status","skipped");
          break;

        case R.id.quiz_btnNavSwitch:
          RelativeLayout.LayoutParams params =
              (RelativeLayout.LayoutParams)view.getLayoutParams();
          if(frameNavigator.getVisibility() == View.GONE){
            relativeNavigator.startAnimation(slideLeft);
            frameNavigator.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            btnNavigator.setLayoutParams(params);
            btnNavigator.setVisibility(View.GONE);
          }else{
            relativeNavigator.startAnimation(slideRight);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
            btnNavigator.setLayoutParams(params);
          }
          break;
      }
      if(resultJsonObj.has("status")){
        resultArr.put(questionCnt-1,resultJsonObj);
        if(btnSubmit.getText().equals("Finish") && btnSkip.getText().equals("Skip and Finish")){
          db.updateUserResult(userID,resultArr.toString());
          startCompletionActivity();
        }
        loadQuestion(questionCnt);
        generateNavigator(responseArr);
      }
      Log.e("Result Array", "" + resultArr.toString());
    }catch (JSONException e){
      Log.e("Error",""+e);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("questionCnt", questionCnt);
    outState.putString("responseArr",responseArr.toString());
    outState.putString("resultArr",resultArr.toString());
    outState.putInt("radioBtnID",rg.getCheckedRadioButtonId());
    outState.putSerializable("radioBtnId", radioBtnId);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    questionCnt = savedInstanceState.getInt("questionCnt");
    try {
      responseArr = new JSONArray(savedInstanceState.getString("responseArr"));
      resultArr = new JSONArray(savedInstanceState.getString("resultArr"));
      generateNavigator(responseArr);
    }catch (JSONException e){
      Log.e("JSON Error Save Instance",e.toString());
    }
    radioBtnId = new Integer[responseArr.length()];
    radioBtnId = (Integer [])savedInstanceState.getSerializable("radioBtnId");
    loadQuestion(questionCnt);
    if(savedInstanceState.getInt("radioBtnID") != -1){
      RadioButton rBtn=(RadioButton)rg.findViewById(savedInstanceState.getInt("radioBtnID"));
      rBtn.setChecked(true);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    switch(compoundButton.getId()){
      case R.id.radio0:
        radioTextColorChange(b,radio0,"A.");
        break;

      case R.id.radio1:
        radioTextColorChange(b,radio1,"B.");
        break;

      case R.id.radio2:
        radioTextColorChange(b,radio2,"C.");
        break;

      case R.id.radio3:
        radioTextColorChange(b,radio3,"D.");
        break;
    }
  }

  private void makeJsonArrayReq() {
    showProgressDialog();
    JsonArrayRequest req = new JsonArrayRequest(Constants.urlQuizQuestion,
        new Response.Listener<JSONArray>(){
          @Override
          public void onResponse(JSONArray response) {
            Log.d(TAG, response.toString());
            responseArr = response;
            radioBtnId = new Integer[responseArr.length()];
            loadQuestion(questionCnt);
            hideProgressDialog();
            generateNavigator(responseArr);
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        VolleyLog.d(TAG, "Error: " + error.getMessage());
        hideProgressDialog();
      }
    });

    // Adding request to request queue
    AppController.getInstance().addToRequestQueue(req,
        Constants.tag_json_arry);
  }

  public void loadQuestion(Integer questionCnt){

    try{
      resultJsonObj = new JSONObject();

      JSONObject responseObj = responseArr.getJSONObject(questionCnt);
      no_of_questions = responseArr.length();
      qID = Integer.parseInt(responseObj.getString("id"));
      correctAnswer = responseObj.getString("correct");
      txtQuizQuestionNo.setText(Integer.toString(questionCnt+1));
      txtQuizQuestion.setText(responseObj.getString("question"));
      txtQuestionProgress.setText(questionCnt+1+" of "+no_of_questions);

      progressbarQuestionStatus.setProgress(100/(2/(questionCnt+1)));

      JSONArray options = responseObj.getJSONArray("options");
      rg.clearCheck();
      if(radioBtnId[questionCnt] != null && radioBtnId[questionCnt]!=0){
        rg.check(radioBtnId[questionCnt]);
        btnSkip.setVisibility(View.GONE);
      }else{
        resultJsonObj.put("status","activated");
        resultArr.put(questionCnt,resultJsonObj);
        Log.e("load",resultArr.toString());
        btnSkip.setVisibility(View.VISIBLE);
      }
      for(int i=0;i < options.length();i++){
        if(i == 0){
          radio0.setText("A. " + options.getString(i));
        }else if(i == 1){
          radio1.setText("B. " + options.getString(i));
        }else if(i == 2){
          radio2.setText("C. " + options.getString(i));
        }else{
          radio3.setText("D. " + options.getString(i));
        }
      }
      if(no_of_questions-1 == questionCnt){
        btnSkip.setText("Skip and Finish");
        btnSkip.setPadding(10,0,10,0);
        btnSubmit.setText("Finish");
      }else{
        btnSkip.setText("Skip");
        btnSkip.setPadding(0,0,0,0);
        btnSubmit.setText("Submit");
      }
    }catch (JSONException e){
      Log.d(TAG, "Response Error: " + e);
    }
  }

  private void showProgressDialog() {
    if (!pDialog.isShowing())
      pDialog.show();
  }

  private void hideProgressDialog() {
    if (pDialog.isShowing())
      pDialog.dismiss();
  }

  private void generateNavigator(JSONArray quizArray){
    tableNavigatorLayout.removeAllViews();
    Integer cnt = 1;
    Integer rowLength = quizArray.length()/3;
    Integer quizLength = quizArray.length();
    float d = getApplicationContext().getResources().getDisplayMetrics().density;
    for(int i = 1; i<= rowLength+1; i++ ){
      TableRow row = new TableRow(this);
      row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
          TableRow.LayoutParams.WRAP_CONTENT));
      for(int j = 1; j < 6; j++ ){
        ids.add(cnt);
        TextView tv = new TextView(this);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT));
        tv.setId(Integer.parseInt("" + cnt));
        tv.setPadding(10, 10, 10, 10);
        tv.setText("" + (cnt));
        tv.setTextColor(Color.parseColor("#ffffff"));
        tv.setBackgroundColor(Color.parseColor("#1991e7"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tv.setGravity(Gravity.CENTER);
        tv.setMinWidth(50);
        if(resultArr.length() != 0){
          try{
            if(!resultArr.isNull(cnt-1)) {
              JSONObject json = resultArr.getJSONObject(cnt - 1);
              if (json.getString("status").equals("taken"))
                tv.setBackgroundResource(R.drawable.taken);
              else if (json.getString("status").equals("skipped"))
                tv.setBackgroundResource(R.drawable.skiped);
            }
          }catch (JSONException e){
            Log.e("Navigator Exception",""+e);
          }
        }
        tv.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            TextView textView = (TextView) view;
            Integer no = textView.getId() - 1;
            if (!resultArr.isNull(no)) {
              questionCnt = no;
              loadQuestion(no);
            }
          }
        });

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();

        params.rightMargin = 4;
        tv.setLayoutParams(params);
        row.addView(tv);

        if(quizLength == cnt)
          break;
        cnt++;
      }
      tableNavigatorLayout.addView(row);
    }
  }

  private void startCompletionActivity(){
    Intent i = new Intent(getApplicationContext(),CompletionActivity.class);
    startActivity(i);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(broadcastReceiver);
    if(isFinishing()){
      Intent intentService = new Intent(this, TimerService.class);
      stopService(intentService);
    }
  }
}
