package quiz.fw.com.quiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import quiz.fw.com.Database.DatabaseHelper;
import quiz.fw.com.Database.Quiz;
import quiz.fw.com.R;
import quiz.fw.com.controller.AppController;
import quiz.fw.com.utils.CommonFunctions;
import quiz.fw.com.utils.Constants;

public class RegistrationActivity extends Activity {

  EditText etName,etEmail,etPhone;
  Spinner spPostApplied;
  Button btnSubmit;
  DatabaseHelper db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.registration);

    db = new DatabaseHelper(this);

    etName = (EditText) findViewById(R.id.reg_edtName);
    etEmail = (EditText) findViewById(R.id.reg_edtEmail);
    etPhone = (EditText) findViewById(R.id.reg_edtPhone);

    spPostApplied = (Spinner) findViewById(R.id.reg_selectPost);
    spPostApplied.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        spPostApplied.getItemAtPosition(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    btnSubmit =(Button) findViewById(R.id.reg_btnSubmit);
    btnSubmit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        long insertedId = db.addUserDetails(new Quiz(etName.getText().toString(), etEmail.getText().toString(), etPhone.getText().toString(), spPostApplied.getSelectedItem().toString()));
        Toast.makeText(getApplicationContext(),""+insertedId,Toast.LENGTH_SHORT).show();
        if (insertedId != -1) {
          Toast.makeText(getApplicationContext(), "Data Saved Successfully", Toast.LENGTH_LONG).show();
          Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
          intent.putExtra("userID",Long.toString(insertedId));
          startActivity(intent);
          finish();
        }
      }
    });
  }
}
