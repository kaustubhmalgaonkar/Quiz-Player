package quiz.fw.com.optionsTemplate;

import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import quiz.fw.com.R;

/**
 * Created by kaustubh on 12/11/14.
 */
public class OptionsPagerFragmentAdapter extends PagerAdapter{

  private String TAG = OptionsPagerActivity.class.getSimpleName();
  private TableLayout tableNavigatorLayout;
  private List<Integer> ids = new ArrayList<Integer>();
  OptionsPagerActivity activity;
  LayoutInflater inflater;
  private TextView txtQuizQuestion;
  private Button btnSubmit;
  private JSONArray responseArr,resultArr,options;
  JSONObject resultJsonObj;
  Integer no_of_questions;
  RadioGroup rg;
  public int [] radioBtnId,navStatus,questionId;

  public OptionsPagerFragmentAdapter(OptionsPagerActivity activity, JSONArray responseArr) {
    this.activity = activity;
    this.responseArr = responseArr;
    resultArr = new JSONArray();
    radioBtnId = new int[responseArr.length()];
    navStatus = new int[responseArr.length()];
    questionId = new int[responseArr.length()];
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == ((RelativeLayout)object);
  }

  @Override
  public int getCount() {
    return responseArr.length();
  }

  @Override
  public Object instantiateItem(final ViewGroup container, final int position) {

    inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
    final View itemView = inflater.inflate(R.layout.options_fragment, container,false);

    txtQuizQuestion = (TextView) itemView.findViewById(R.id.quiz_txtQuestion);
    txtQuizQuestion.setText("title");

    tableNavigatorLayout = (TableLayout) itemView.findViewById(R.id.tableNavigatorLayout);
    tableNavigatorLayout.removeAllViews();

    btnSubmit = (Button) itemView.findViewById(R.id.quiz_btnSubmit);
    btnSubmit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        updateStatus(position);
        activity.setCurrentPage(position + 1);
      }
    });

    rg = (RadioGroup) itemView.findViewById(R.id.quiz_rgOpinion);

    loadQuestion(position);
    generateNavigator(responseArr);

    ((ViewPager) container).addView(itemView);
    return itemView;
  }

  @Override
  public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  @Override
  public Parcelable saveState() {
    Bundle state = null;

    if(radioBtnId.length != 0)
    {
      state = new Bundle();
      state.putIntArray("radioBtnId", radioBtnId);
      state.putIntArray("navStatus", navStatus);
      state.putIntArray("questionId", questionId);
    }
    return state;
  }

  @Override
  public void restoreState(Parcelable state, ClassLoader loader) {
    super.restoreState(state, loader);
    Bundle bundle = (Bundle)state;
    radioBtnId = bundle.getIntArray("radioBtnId");
    navStatus = bundle.getIntArray("navStatus");
    questionId = bundle.getIntArray("questionId");
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    ((ViewPager) container).removeView((RelativeLayout) object);
  }

  public void loadQuestion(final Integer questionCnt){
    try{

      JSONObject responseObj = responseArr.getJSONObject(questionCnt);
      no_of_questions = responseArr.length();
      questionId[questionCnt] = Integer.parseInt(responseObj.getString("id"));
      txtQuizQuestion.setText(responseObj.getString("title"));

      options = responseObj.getJSONArray("options");
      rg.clearCheck();
      rg.removeAllViews();
      for(int i=0;i < options.length();i++){
        JSONObject json = options.getJSONObject(i);

        RadioGroup.LayoutParams params_soiled = new RadioGroup.LayoutParams(activity.getApplicationContext(), null);
        params_soiled.setMargins(0, 5, 0, 5);
        if(radioBtnId[questionCnt] == 0) {
          radioBtnId[questionCnt] = -1;
          navStatus[questionCnt] = 3;
        }

        final RadioButton rdBtn = new RadioButton(activity);
        rdBtn.setId(json.getInt("id"));
        rdBtn.setText(String.valueOf(Character.toChars(65 + i)) + ". " + Html.fromHtml(json.getString("option")));
        rdBtn.setTextColor(activity.getResources().getColor(R.color.black));
        rdBtn.setButtonDrawable(new StateListDrawable());
        rdBtn.setBackgroundResource(R.drawable.radio);
        rdBtn.setPadding(10, 10, 10, 15);
        rdBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        rdBtn.setLayoutParams(params_soiled);
        rdBtn.setWidth(500);
        final int cnt = questionCnt;
        rdBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            radioTextColorChange(b,rdBtn);
            if( b == true)
              radioBtnId[questionCnt] = rdBtn.getId();
          }
        });

        if(radioBtnId[questionCnt] == rdBtn.getId()) {
          rdBtn.setChecked(true);
        }

        rg.addView(rdBtn);
      }

      if(no_of_questions-1 == questionCnt){
        btnSubmit.setText("Finish");
        btnSubmit.setVisibility(View.VISIBLE);
      }else{
        btnSubmit.setVisibility(View.INVISIBLE);
      }
    }catch (JSONException e){
      Log.d(TAG, "Response Error: " + e);
    }
  }

  private void generateNavigator(JSONArray quizArray){
    tableNavigatorLayout.removeAllViews();
    Integer cnt = 1;
    Integer rowLength = quizArray.length()/3;
    Integer quizLength = quizArray.length();
    float d = activity.getApplicationContext().getResources().getDisplayMetrics().density;
    for(int i = 1; i<= rowLength; i++ ){
      TableRow row = new TableRow(activity);
      row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
          TableRow.LayoutParams.WRAP_CONTENT));
      for(int j = 1; j < 6; j++ ){
        ids.add(cnt);
        TextView tv = new TextView(activity);
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

        if(navStatus[cnt-1] != 0) {
          if (navStatus[cnt-1] == 1)
            tv.setBackgroundResource(R.drawable.taken);
          else if (navStatus[cnt-1] == 2)
            tv.setBackgroundResource(R.drawable.skiped);
        }

        tv.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            TextView textView = (TextView) view;
            Integer no = textView.getId() - 1;
            if(navStatus[no] != 0)
              activity.setCurrentPage(no);
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

  public void createJsonData(){

    for (int i=0 ; i < radioBtnId.length ; i++) {
      try {
        resultJsonObj = new JSONObject();
        resultJsonObj.put("q_id", questionId[i]);
        resultJsonObj.put("option_id", radioBtnId[i]);
        resultJsonObj.put("status", navStatus[i]);

        resultArr.put(i,resultJsonObj);
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    activity.updateUserData(resultArr);
  }

  public void updateStatus(int position){
    if(radioBtnId[position] != -1){
      navStatus[position] = 1;
    }else{
      navStatus[position] = 2;
    }
    if(btnSubmit.getText().equals("Submit")){
      createJsonData();
    }
  }

  public void radioTextColorChange(boolean b, RadioButton radioBtn){
    if(b) {
      radioBtn.setTextColor(activity.getResources().getColor(R.color.white));
    }else {
      radioBtn.setTextColor(activity.getResources().getColor(R.color.black));
    }
  }
}
