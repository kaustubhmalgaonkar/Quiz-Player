package quiz.fw.com.quiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
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
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quiz.fw.com.Database.DatabaseHelper;
import quiz.fw.com.Database.Quiz;
import quiz.fw.com.R;
import quiz.fw.com.controller.AppController;
import quiz.fw.com.utils.CommonFunctions;
import quiz.fw.com.utils.Constants;
import quiz.fw.com.utils.TimerService;

/**
 * Created by kaustubh on 9/10/14.
 */
public class QuizActivity extends Activity implements View.OnClickListener {

  private DatabaseHelper db;
  private TextView txtQuizQuestion,txtQuestionProgress,txtNavLabel,txtTimer;
  private Button btnSubmit,btnSkip;
  private ImageButton btnNavigator;
  private ProgressBar progressbarQuestionStatus;
  private ProgressDialog pDialog;
  private String TAG = QuizActivity.class.getSimpleName();
  Integer no_of_questions;
  RadioGroup rg;
  private List<Integer> ids = new ArrayList<Integer>();

  private TableLayout tableNavigatorLayout;
  private Animation slideRight, slideLeft,fade_in, fade_out;
  private FrameLayout frameNavigator;
  private RelativeLayout relativeNavigator;
  JSONObject resultJsonObj;
  private JSONArray responseArr,resultArr,options;
  private Integer questionCnt = 0;
  private Integer userID;
  private Integer qID;
  private Integer [] radioBtnId;
  private String designation;
  AlertDialog alert11;
  private BroadcastReceiver broadcastReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.quiz);

    db = new DatabaseHelper(this);

    Intent extras = getIntent();
    if (extras != null) {
      userID = Integer.parseInt(extras.getStringExtra("userID"));
      designation = extras.getStringExtra("designation");
    }

    resultArr = new JSONArray();

    tableNavigatorLayout = (TableLayout) findViewById(R.id.tableNavigatorLayout);
    tableNavigatorLayout.removeAllViews();

    txtQuizQuestion = (TextView) findViewById(R.id.quiz_txtQuestion);
    txtQuestionProgress = (TextView) findViewById(R.id.quiz_txtCurrentQuestion);
    txtTimer = (TextView) findViewById(R.id.quiz_txtTimer);
    txtNavLabel = (TextView) findViewById(R.id.quiz_txtNavLabel);
    txtNavLabel.setOnClickListener(this);

    progressbarQuestionStatus = (ProgressBar) findViewById(R.id.questionProgress);
    frameNavigator = (FrameLayout) findViewById(R.id.frameNavigator);
    relativeNavigator = (RelativeLayout) findViewById(R.id.navigatorLayout);

    rg = (RadioGroup) findViewById(R.id.quiz_rgOpinion);

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
    broadcastReceiver = new BroadcastReceiver(){
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

    registerReceiver(broadcastReceiver, new IntentFilter(TimerService.BROADCAST_ACTION));
    if(savedInstanceState == null) {
      makeJsonArrayReq();
      if(!CommonFunctions.isServiceRunning(TimerService.class,this)) {
        Intent intentService = new Intent(this, TimerService.class);
        intentService.putExtra("minutes",30);
        startService(intentService);
      }
    }
  }

  public void radioTextColorChange(boolean b, RadioButton radioBtn){
    if(b) {
      radioBtn.setTextColor(getResources().getColor(R.color.white));
    }else {
      radioBtn.setTextColor(getResources().getColor(R.color.black));
    }
  }

  @Override
  public void onClick(View view) {
    try{
      switch (view.getId()) {
        case R.id.quiz_btnSubmit:
          Log.e("radio",""+rg.getCheckedRadioButtonId());
          if(rg.getCheckedRadioButtonId() != -1){
            resultJsonObj.put("q_id",qID);
            radioBtnId[questionCnt] = rg.getCheckedRadioButtonId();
            questionCnt++;
            JSONObject json = options.getJSONObject(rg.getCheckedRadioButtonId());
            resultJsonObj.put("option_id",json.getString("id"));

            resultJsonObj.remove("status");
            resultJsonObj.put("status","1");
          }
          break;

        case R.id.quiz_btnSkip:
          resultJsonObj.put("q_id",qID);
          radioBtnId[questionCnt] = -1;
          questionCnt++;
          resultJsonObj.remove("status");
          resultJsonObj.put("status","0");
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

      if(resultJsonObj != null && resultJsonObj.has("status")){
        if(rg.getCheckedRadioButtonId() != -1)
          resultArr.put(questionCnt-1,resultJsonObj);
        if(btnSubmit.getText().equals("Finish") && btnSkip.getText().equals("Skip and Finish")){
          db.updateUserResult(userID,resultArr.toString());
          startCompletionActivity();
        }else{
          loadQuestion(questionCnt);
          generateNavigator(responseArr);
        }
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
    if(responseArr != null && resultArr != null && options != null && radioBtnId != null) {
      outState.putString("responseArr", responseArr.toString());
      outState.putString("options", options.toString());
      outState.putString("resultArr", resultArr.toString());
      outState.putInt("radioBtnID", rg.getCheckedRadioButtonId());
      outState.putSerializable("radioBtnId", radioBtnId);
    }else if (alert11 != null){
      alert11.dismiss();
    }
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    questionCnt = savedInstanceState.getInt("questionCnt");
    if(savedInstanceState.getString("responseArr") != null){
      try {
        responseArr = new JSONArray(savedInstanceState.getString("responseArr"));
        options = new JSONArray(savedInstanceState.getString("options"));
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
    }else{
      showPopup();
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
        showPopup();
      }
    });

    // Adding request to request queue
    AppController.getInstance().addToRequestQueue(req,
        Constants.tag_json_arry);
  }

  public void showPopup(){
    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    builder1.setMessage("Failed to retrieve Questions.");
    builder1.setCancelable(false);
    builder1.setPositiveButton("Retry",
        new DialogInterface.OnClickListener(){
          public void onClick(DialogInterface dialog, int id) {
            destroyService();
            makeJsonArrayReq();
            dialog.cancel();
          }
        });
    builder1.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            db.deleteRecord(userID);
            Intent i = new Intent(getApplicationContext(),RegistrationActivity.class);
            startActivity(i);
            finish();
          }
        });

    alert11 = builder1.create();
    alert11.show();
  }

  public void loadQuestion(Integer questionCnt){

    try{
      resultJsonObj = new JSONObject();

      JSONObject responseObj = responseArr.getJSONObject(questionCnt);
      no_of_questions = responseArr.length();
      qID = Integer.parseInt(responseObj.getString("id"));
      txtQuizQuestion.setText(responseObj.getString("title"));
      txtQuestionProgress.setText(questionCnt+1+" of "+no_of_questions);

      progressbarQuestionStatus.setProgress(100/no_of_questions*(questionCnt+1));

      options = responseObj.getJSONArray("options");
      rg.clearCheck();
      rg.removeAllViews();
      for(int i=0;i < options.length();i++){
        JSONObject json = options.getJSONObject(i);

        RadioGroup.LayoutParams params_soiled = new RadioGroup.LayoutParams(getBaseContext(), null);
        params_soiled.setMargins(0,5,0,5);

        final RadioButton rdBtn = new RadioButton(this);
        rdBtn.setId(i);
        rdBtn.setText(String.valueOf(Character.toChars(65 + i)) + ". " + Html.fromHtml(json.getString("option")));
        rdBtn.setTextColor(getResources().getColor(R.color.black));
        rdBtn.setButtonDrawable(new StateListDrawable());
        rdBtn.setBackgroundResource(R.drawable.radio);
        rdBtn.setPadding(10, 10, 10, 15);
        rdBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        rdBtn.setLayoutParams(params_soiled);
        rdBtn.setWidth(500);
        rdBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            radioTextColorChange(b,rdBtn);
          }
        });
        if(radioBtnId[questionCnt] != null && radioBtnId[questionCnt] == i)
          rdBtn.setChecked(true);

        rg.addView(rdBtn);
      }

      if(radioBtnId[questionCnt] != null){
        btnSkip.setVisibility(View.GONE);
      }else{
        resultJsonObj.put("status","2");
        resultArr.put(questionCnt,resultJsonObj);
        btnSkip.setVisibility(View.VISIBLE);
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
    for(int i = 1; i<= rowLength; i++ ){
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
        tv.setMinHeight(50);
        if(resultArr.length() != 0){
          try{
            if(!resultArr.isNull(cnt-1)) {
              JSONObject json = resultArr.getJSONObject(cnt - 1);
              if (json.getString("status").equals("1"))
                tv.setBackgroundResource(R.drawable.taken);
              else if (json.getString("status").equals("0"))
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
        params.bottomMargin = 4;
        tv.setLayoutParams(params);
        row.addView(tv);
        if(quizLength == cnt)
          break;
        cnt++;
      }
      tableNavigatorLayout.addView(row);
    }
  }

  private void postData(){

    if(CommonFunctions.checkNetworkConnection(this)){
      StringRequest postRequest = new StringRequest(Request.Method.POST,Constants.urlQuizSubmit, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
          db.deleteRecord(userID);
          Log.e("Response",response);
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.e("Error Response",error.toString());
        }
      }){
        @Override
        protected Map<String,String> getParams(){
          Quiz userData = db.getUserDetails(userID);
          Map<String, String> jsonParams = new HashMap<String, String>();
          jsonParams.put("name", userData.getName());
          jsonParams.put("phone", userData.getPhone());
          jsonParams.put("email", userData.getEmail());
          jsonParams.put("post_applied", userData.getPost_applied());
          jsonParams.put("result", userData.getResult());

          return jsonParams;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          Map<String,String> params = new HashMap<String, String>();
          params.put("Content-Type","application/x-www-form-urlencoded");
          return params;
        }
      };
      AppController.getInstance().addToRequestQueue(postRequest);
    }
  }

  private void startCompletionActivity(){
    postData();
    Intent i = new Intent(getApplicationContext(),CompletionActivity.class);
    startActivity(i);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    destroyService();
  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterReceiver(broadcastReceiver);
  }

  protected void destroyService(){
    if(isFinishing()){
      Log.e("Destroy","Stop service");
      Intent intentService = new Intent(this, TimerService.class);
      stopService(intentService);
    }
  }
}
