package quiz.fw.com.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import quiz.fw.com.R;

/**
 * Created by kaustubh on 6/11/14.
 */
public class SpinnerAdapter extends ArrayAdapter<String> {
  Activity ctx;
  String [] tags;

  public SpinnerAdapter(Activity ctx, int txtViewResourceId, String[] tags)
  {
    super(ctx, txtViewResourceId, tags);
    this.ctx = ctx;
    this.tags = tags;
  }

  @Override
  public View getDropDownView(int position, View cnvtView, ViewGroup prnt)
  {
    return getCustomView(position, cnvtView, prnt);
  }

  @Override
  public View getView(int pos, View cnvtView, ViewGroup prnt)
  {
    return getCustomView(pos, cnvtView, prnt);
  }

  public View getCustomView(int position, View convertView, ViewGroup parent)
  {
    LayoutInflater inflater = ctx.getLayoutInflater();
    View mySpinner = inflater.inflate(R.layout.spinner_item, parent, false);
    TextView main_text = (TextView) mySpinner .findViewById(R.id.spText);
    main_text.setText(tags[position]);

    return mySpinner;
  }
}
