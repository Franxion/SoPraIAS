package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

// DTO for specific user
public class UserGetFullDTO {

  private Long id;
  private String username;
  private UserStatus status;
  private LocalDateTime creationDate;
  private LocalDate birthdayDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public LocalDate getBirthdayDate() {
    return birthdayDate;
  }

  public void setBirthdayDate(LocalDate birthdayDate) {
    this.birthdayDate = birthdayDate;
  }
}
