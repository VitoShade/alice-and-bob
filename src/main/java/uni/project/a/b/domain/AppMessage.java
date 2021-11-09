package uni.project.a.b.domain;


import lombok.Data;
import org.springframework.security.web.header.Header;

import java.time.LocalDateTime;
import java.util.Optional;

/*@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

 */
@Data
public class AppMessage {

   private Long id;

   private Long sessionId;

   private String body;

   private LocalDateTime time;

   //the name of the user?
   private String senderUser;

   private Header header;

 public AppMessage(Long sessionId, String body, LocalDateTime time, String senderUser) {
  this.sessionId = sessionId;
  this.body = body;
  this.time = time;
  this.senderUser = senderUser;
 }

 public AppMessage(Long sessionId, String body, LocalDateTime time, String senderUser, Header header) {
  this.sessionId = sessionId;
  this.body = body;
  this.time = time;
  this.senderUser = senderUser;
  this.header = header;
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
