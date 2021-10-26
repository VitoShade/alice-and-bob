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

   private Long id;

   private Long sessionId;

   private String body;

   private LocalDateTime time;

   //the name of the user?
   private String senderUser;

 public AppMessage(Long sessionId, String body, LocalDateTime time, String senderUser) {
  this.sessionId = sessionId;
  this.body = body;
  this.time = time;
  this.senderUser = senderUser;
 }

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

 public String getSenderUser() {
  return senderUser;
 }

 public void setSenderUser(String senderUser) {
  this.senderUser = senderUser;
 }
}
