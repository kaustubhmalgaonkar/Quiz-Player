package quiz.fw.com.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by kaustubh on 30/9/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

  // All Static variables
  // Database Version
  private static final int DATABASE_VERSION = 1;

  // Database Name
  private static final String DATABASE_NAME = "QUIZ";

  // table name
  private static final String TABLE_USER_DETAIL = "userDetails";

  // UserDetails_Data Table Columns names
  private static final String KEY_UID = "uid";
  private static final String KEY_name = "name";
  private static final String KEY_email = "email";
  private static final String KEY_mobile = "mobile";
  private static final String KEY_post = "post_applied";
  private static final String KEY_RESULT = "result";

  public DatabaseHelper(Context context){
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_USER_DETAIL = "CREATE TABLE " + TABLE_USER_DETAIL + "("
        + KEY_UID + " INTEGER PRIMARY KEY,"+ KEY_name + " TEXT,"+KEY_email+ " TEXT,"+KEY_mobile+ " TEXT,"+KEY_post+ " TEXT,"+KEY_RESULT+ " TEXT)";
    db.execSQL(CREATE_USER_DETAIL);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    // Drop older table if existed
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAIL);
    onCreate(db);
  }

  /*Add user details*/
  public long addUserDetails(Quiz quiz){
    SQLiteDatabase db = this.getReadableDatabase();

    ContentValues values = new ContentValues();
    values.put(KEY_name, quiz.getName());
    values.put(KEY_email, quiz.getEmail());
    values.put(KEY_mobile, quiz.getPhone());
    values.put(KEY_post, quiz.getPost_applied());
    long lastId = db.insert(TABLE_USER_DETAIL,null,values);

    Log.i("Database", "Inserted User data");

    return lastId;
  }

  /*Update users result*/
  public boolean updateUserResult(Integer uid,String result){
    SQLiteDatabase db = this.getReadableDatabase();

    ContentValues values = new ContentValues();
    values.put(KEY_RESULT, result);
    boolean updatedId = db.update(TABLE_USER_DETAIL, values, KEY_UID+"="+uid, null)>0;
    Log.i("Database", "Updated Result");

    return updatedId;
  }
}
