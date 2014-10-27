package quiz.fw.com.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quiz.fw.com.controller.AppController;

/**
 * Created by kaustubh on 13/10/14.
 */
public class CommonFunctions {

  //check wifi and internet availability
  public static boolean checkNetworkConnection(Context context)
  {
    final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    final android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if(wifi.isAvailable())
      return true;
    else if(mobile.isAvailable())
      return true;
    else
      return false;
  }

  //time counter
  public static void countdownDecreaseTimer(Integer minutes,final TextView txtTimer){
    new CountDownTimer(minutes*60000, 1000) { // adjust the milli seconds here

      public void onTick(long millisUntilFinished) {
        txtTimer.setText("" + String.format("%02d:%02d:%02d",TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
      }

      public void onFinish() {
        txtTimer.setText("done!");
      }
    }.start();
  }

  //check service is running or not
  public static boolean isServiceRunning(Class<?> serviceClass,Context context) {
    ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        Log.i("Service already","running");
        return true;
      }
    }
    Log.i("Service not", "running");
    return false;
  }

  private void postData(final Context context,String url,Map<String, String> jsonParams){

    JsonObjectRequest myRequest = new JsonObjectRequest(
        Request.Method.POST,
        url,
        new JSONObject(jsonParams),

        new Response.Listener<JSONObject>(){
          @Override
          public void onResponse(JSONObject response) {
            VolleyLog.d("Registration response", response.toString());
            Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            VolleyLog.e("Registration Error",error.getMessage());
            Toast.makeText(context,""+error.getMessage(),Toast.LENGTH_SHORT).show();
          }
        }) {

      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
      }
    };
    AppController.getInstance().addToRequestQueue(myRequest, Constants.tag_json_obj);
  }

  // validating email id
  public static boolean isValidEmail(String email) {
    String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }
}
