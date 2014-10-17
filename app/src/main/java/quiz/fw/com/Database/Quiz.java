package quiz.fw.com.Database;

/**
 * Created by kaustubh on 30/9/14.
 */
public class Quiz {

  private Integer uId;
  private String name;
  private String email;
  private String phone;
  private String post_applied;
  private String result;

  public Quiz(){}

  public Quiz(Integer uId, String result){
    this.uId = uId;
    this.result = result;
  }

  public Quiz(String name, String email, String phone, String post_applied){
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.post_applied = post_applied;
  }

  public Integer getUId() {
    return uId;
  }

  public void setUId(Integer uId) {
    this.uId = uId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPost_applied() {
    return post_applied;
  }

  public void setPost_applied(String post_applied) {
    this.post_applied = post_applied;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
