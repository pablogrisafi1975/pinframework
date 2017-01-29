package com.pinframework;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class DateRelated {
  private LocalDateTime localDateTime;
  private LocalDate localDate;
  private ZonedDateTime zonedDateTime;
  
  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }
  
  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }
  
  public LocalDate getLocalDate() {
    return localDate;
  }
  
  public void setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
  }
  
  public ZonedDateTime getZonedDateTime() {
    return zonedDateTime;
  }
  
  public void setZonedDateTime(ZonedDateTime zonedDateTime) {
    this.zonedDateTime = zonedDateTime;
  }
  
}
