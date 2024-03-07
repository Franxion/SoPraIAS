package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// I campi modificabili della put dovrebbero essere: birthdayDate, Username but be wary 
// to ID read well the specifications
public class UserPutDTO {
  private String username;
  private LocalDate birthdayDate;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public LocalDate getBirthdayDate() {
    return birthdayDate;
  }

  public void setBirthdayDate(LocalDate birthdayDate) {
    this.birthdayDate = birthdayDate;
  }
}