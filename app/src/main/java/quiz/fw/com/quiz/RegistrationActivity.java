package quiz.fw.com.quiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import quiz.fw.com.Database.DatabaseHelper;
import quiz.fw.com.Database.Quiz;
import quiz.fw.com.R;
import quiz.fw.com.controller.AppController;
import quiz.fw.com.optionsTemplate.OptionsPagerActivity;
import quiz.fw.com.utils.CommonFunctions;
import quiz.fw.com.utils.Constants;
import quiz.fw.com.utils.SpinnerAdapter;

public class RegistrationActivity extends Activity {

  EditText etName,etEmail,etPhone;
  Spinner spPostApplied;
  Button btnSubmit;
  DatabaseHelper db;
  RelativeLayout rl;
  JSONArray tags;
  String [] tagsList;
  String [] tagsId;
  String designation;

  ShowcaseView sv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.registration);

    rl = (RelativeLayout) findViewById(R.id.frameLayout1);
    Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    final int orientation = display.getOrientation();

    if(Configuration.ORIENTATION_PORTRAIT == orientation){
      rl.setPadding(300,0,300,0);
    }else{
      rl.setPadding(10,0,10,0);
    }

    db = new DatabaseHelper(this);

    etName = (EditText) findViewById(R.id.reg_edtName);
    etEmail = (EditText) findViewById(R.id.reg_edtEmail);
    etPhone = (EditText) findViewById(R.id.reg_edtPhone);

    spPostApplied = (Spinner) findViewById(R.id.reg_selectPost);
    // Spinner adapter
    getTagsJsonObject();

    btnSubmit =(Button) findViewById(R.id.reg_btnSubmit);
    btnSubmit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(checkValidation()){
          long insertedId = db.addUserDetails(new Quiz(etName.getText().toString(), etEmail.getText().toString(), etPhone.getText().toString(), designation));
          if (insertedId != -1) {
            Toast.makeText(getApplicationContext(), "Data Saved Successfully", Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
            Intent intent = new Intent(getApplicationContext(), OptionsPagerActivity.class);
            intent.putExtra("userID",Long.toString(insertedId));
            intent.putExtra("designation", designation);
            startActivity(intent);
            finish();
          }
        }
      }
    });
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
      rl.setPadding(10,0,10,0);
    }

    if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
      rl.setPadding(300,0,300,0);
    }

  }

  private boolean checkValidation(){
    boolean checkVal = true;

    if(etName.getText().length()==0)
    {
      etName.setError("Field cannot be left blank.");
      checkVal = false;
    }
    if(etPhone.getText().length()==0)
    {
      etPhone.setError("Field cannot be left blank.");
      checkVal = false;
    }else if(etPhone.getText().length() < 6 || etPhone.getText().length() > 13)
    {
      etPhone.setError("Enter Valid Number");
      checkVal = false;
    }

    if(etEmail.getText().length()==0)
    {
      etEmail.setError("Field cannot be left blank.");
      checkVal = false;
    }else if(!CommonFunctions.isValidEmail(etEmail.getText()))
    {
      etEmail.setError("Enter Valid Email ID");
      checkVal = false;
    }

    return checkVal;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    View v = getCurrentFocus();

    if (v != null &&
        (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
        v instanceof EditText &&
        !v.getClass().getName().startsWith("android.webkit."))
    {
      int scrcoords[] = new int[2];
      v.getLocationOnScreen(scrcoords);
      float x = ev.getRawX() + v.getLeft() - scrcoords[0];
      float y = ev.getRawY() + v.getTop() - scrcoords[1];

      if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
        CommonFunctions.hideKeyboard(this);
    }
    return super.dispatchTouchEvent(ev);
  }

  public void getTagsJsonObject(){
    JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, Constants.urlQuizTags, null,
        new Response.Listener<JSONObject>()
        {
          @Override
          public void onResponse(JSONObject response) {
            try {
              // display response
              tagsList = new String[response.names().length()];
              tagsId = new String[response.names().length()];
              int len= response.names().length();
              for (int i = 0; i < len ; i++) {
                tagsId[i] = response.names().getString(i);
                tagsList[i] = response.getString(response.names().getString(i));
              }
              spPostApplied.setAdapter(new SpinnerAdapter(RegistrationActivity.this,R.layout.spinner_item,tagsList));
              spPostApplied.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                  designation = tagsId[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
              });
            }catch (Exception e){
              Log.e("Tag Exception",e.toString());
            }
          }
        },
        new Response.ErrorListener()
        {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("Error.Response", error.toString());
          }
        }
    );
    // add it to the RequestQueue
    AppController.getInstance().addToRequestQueue(getRequest);
  }
}
