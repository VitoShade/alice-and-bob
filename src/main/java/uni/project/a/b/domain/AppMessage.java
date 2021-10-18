package uni.project.a.b.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/*@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

 */
public class AppMessage {

  // @Id
   private Long sessionId;

   private String body;

   private LocalDateTime time;

   //the name of the user?
   private Long senderId;

 public Long getSessionId() {
  return sessionId;
 }

 public void setSessionId(Long sessionId) {
  this.sessionId = sessionId;
 }

 public String getBody() {
  return body;
 }

 public void setBody(String body) {
  this.body = body;
 }

 public LocalDateTime getTime() {
  return time;
 }

 public void setTime(LocalDateTime time) {
  this.time = time;
 }

 public Long getSenderId() {
  return senderId;
 }

 public void setSenderId(Long senderId) {
  this.senderId = senderId;
 }
}
