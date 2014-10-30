package quiz.fw.com.utils;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.TimeUnit;

import quiz.fw.com.quiz.CompletionActivity;
import quiz.fw.com.quiz.RegistrationActivity;

/**
 * Created by kaustubh on 14/10/14.
 */
public class TimerService extends Service {
  private final String TAG = "TimerService";
  public static final String BROADCAST_ACTION = "quiz.fw.com";
  Intent intentTimer;

  @Override
  public void onCreate() {
    super.onCreate();
    intentTimer = new Intent(BROADCAST_ACTION);
    Log.d("Service", "Service Created.....");
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if(intent != null){
      Integer minutes = intent.getIntExtra("minutes",0);
      new CountDownTimer(minutes*60000, 1000) { // adjust the milli seconds here

        public void onTick(long millisUntilFinished) {
          String time= String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
              TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
              TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
          intentTimer.putExtra("time",time);
          sendBroadcast(intentTimer);
        }

        public void onFinish() {
          TimerService.this.stopSelf();
          intentTimer.putExtra("time","finish");
          sendBroadcast(intentTimer);
        }
      }.start();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public synchronized void onDestroy() {
    super.onDestroy();
    Log.d("Service", "Service Destroyed.....");
  }

}
