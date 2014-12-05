package quiz.fw.com.optionsTemplate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import quiz.fw.com.Database.DatabaseHelper;
import quiz.fw.com.Database.Quiz;
import quiz.fw.com.R;
import quiz.fw.com.controller.AppController;
import quiz.fw.com.quiz.CompletionActivity;
import quiz.fw.com.quiz.RegistrationActivity;
import quiz.fw.com.utils.CommonFunctions;
import quiz.fw.com.utils.Constants;
import quiz.fw.com.utils.DepthPageTransformer;
import quiz.fw.com.utils.TimerService;

/**
 * Created by kaustubh on 11/11/14.
 */
public class OptionsPagerActivity extends Activity implements ViewPager.PageTransformer{

  TextView txtTimer,txtQuestionCnt;
  protected ViewPager viewPager;
  private ProgressBar progressbarQuestionStatus;
  private String TAG = OptionsPagerActivity.class.getSimpleName();
  private ProgressDialog pDialog;
  AlertDialog alert11;
  OptionsPagerFragmentAdapter adapter;
  private BroadcastReceiver broadcastReceiver;
  DatabaseHelper db;
  private Integer userID;
  private String designation;
  JSONArray responseArr;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.options_screen_slider);

    db = new DatabaseHelper(this);

    Intent extras = getIntent();
    if (extras != null) {
      userID = Integer.parseInt(extras.getStringExtra("userID"));
      designation = extras.getStringExtra("designation");
    }

    pDialog = new ProgressDialog(this);
    pDialog.setMessage("Loading...");
    pDialog.setCancelable(false);

    progressbarQuestionStatus = (ProgressBar) findViewById(R.id.questionProgress);

    viewPager = (ViewPager) findViewById(R.id.pager);

    txtTimer = (TextView) findViewById(R.id.quiz_txtTimer);
    txtQuestionCnt = (TextView) findViewById(R.id.quiz_txtCurrentQuestion);

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
      if(!CommonFunctions.isServiceRunning(TimerService.class, this)) {
        Intent intentService = new Intent(this, TimerService.class);
        intentService.putExtra("minutes",30);
        startService(intentService);
      }
    }
  }

  public void setViewPager(JSONArray response){
    progressbarQuestionStatus.setProgress(100/response.length()*1);
    adapter = new OptionsPagerFragmentAdapter(OptionsPagerActivity.this,response);
    viewPager.setPageTransformer(true, new DepthPageTransformer());
    viewPager.setAdapter(adapter);
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      int oldPos = viewPager.getCurrentItem();
      int position = 0;
      @Override
      public void onPageScrolled(int i, float v, int i2) {
      }

      @Override
      public void onPageSelected(int pos) {
        int no_of_questions= responseArr.length();
        txtQuestionCnt.setText((pos + 1) + " of " + no_of_questions);
        progressbarQuestionStatus.setProgress(100 / no_of_questions * (pos + 1));
        if(pos > oldPos) {
          //Moving to the right
          adapter.updateStatus(pos - 1);
          oldPos = viewPager.getCurrentItem();
        } else if(pos < oldPos) {
          //Moving to the Left
          adapter.updateStatus(pos+1);
          oldPos = viewPager.getCurrentItem();
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
        if(state == ViewPager.SCROLL_STATE_IDLE){
          adapter.notifyDataSetChanged();
        }
        if(state == 1 && viewPager.getCurrentItem() == 4){
          position = 3;
        } else if(state == 2){
          position = 0;
        }
        Log.i("changed",viewPager.getCurrentItem()+" "+state+" "+position);
        if(position == 3 && state == 0){
          showFinalPopup();
        }
      }
    });
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if(responseArr != null) {
      outState.putString("responseArr", responseArr.toString());
    }else if (alert11 != null){
      alert11.dismiss();
    }
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if(savedInstanceState.getString("responseArr") != null){
      try {
        responseArr = new JSONArray(savedInstanceState.getString("responseArr"));
        setViewPager(responseArr);
      }catch (JSONException e){
        Log.e("JSON Error Save Instance",e.toString());
      }
    }else{
      showPopup();
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

  private void makeJsonArrayReq() {
    showProgressDialog();
    JsonArrayRequest req = new JsonArrayRequest(Constants.urlQuizQuestion,
        new Response.Listener<JSONArray>(){
          @Override
          public void onResponse(JSONArray response) {
            Log.d(TAG, response.toString());
            responseArr=response;
            txtQuestionCnt.setText("1 of "+responseArr.length());
            setViewPager(responseArr);
            hideProgressDialog();
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
//            db.deleteRecord(userID);
            Intent i = new Intent(getApplicationContext(),RegistrationActivity.class);
            startActivity(i);
            finish();
          }
        });

    alert11 = builder1.create();
    alert11.show();
  }

  public void showFinalPopup(){
    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    builder1.setMessage("End of Quiz. Do you want to submit ?");
    builder1.setCancelable(false);
    builder1.setPositiveButton("Finish",
        new DialogInterface.OnClickListener(){
          public void onClick(DialogInterface dialog, int id) {
            Toast.makeText(getApplicationContext(),""+viewPager.getCurrentItem(),Toast.LENGTH_SHORT).show();
            adapter.updateStatus(viewPager.getCurrentItem());
          }
        });
    builder1.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {

          }
        });

    alert11 = builder1.create();
    alert11.show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    destroyService();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  protected void destroyService(){
    if(isFinishing()){
      Log.e("Destroy","Stop service");
      Intent intentService = new Intent(this, TimerService.class);
      stopService(intentService);
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

  public void updateUserData(JSONArray resultArr){
    db.updateUserResult(userID,resultArr.toString());
    startCompletionActivity();
  }

  public void startCompletionActivity(){
    postData();
    Intent i = new Intent(getApplicationContext(),CompletionActivity.class);
    startActivity(i);
    finish();
  }

  public void setCurrentPage(int pageNo){
    viewPager.setCurrentItem(pageNo);
  }

  @Override
  public void transformPage(View view, float v) {

  }
}
