package quiz.fw.com.utils;

/**
 * Created by kaustubh on 9/10/14.
 */
public class Constants {

  // These tags will be used to cancel the requests
  public static String tag_json_arry = "jarray_req";
  public static String tag_json_obj = "jobj_req";

//    public static final String baseUrl = "http://staging.focalworks.in/intranet/public/";
//  public static final String baseUrl = "http://192.168.3.170/focalworks-intranet/public/";
//  public static final String baseUrl = "http://192.168.3.79/focalworks-intranet/public/";
  private static final String baseUrl = "http://192.168.3.75/focalworks-intranet/public/";

  public static final String urlQuizQuestion = baseUrl + "quiz/get_quiz_questisons/php";
  public static final String urlQuizUserSave = baseUrl + "quiz/user_save";

}
