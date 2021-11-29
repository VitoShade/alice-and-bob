package uni.project.a.b.domain;


import lombok.Data;

import java.time.LocalDateTime;


@Data
public class AppMessage {

   private Long id;

   private Long sessionId;

   private byte[] body;

   private LocalDateTime time;

   //the name of the user?
   private String senderUser;

   private AppHeader header;

 public AppMessage(Long sessionId, byte[] body, LocalDateTime time, String senderUser) {
  this.sessionId = sessionId;
  this.body = body;
  this.time = time;
  this.senderUser = senderUser;
 }

 public AppMessage(Long sessionId, byte[] body, LocalDateTime time, String senderUser, AppHeader header) {
  this.sessionId = sessionId;
  this.body = body;
  this.time = time;
  this.senderUser = senderUser;
  this.header = header;
 }

 public byte[] getBody() {
  return body;
 }

 public void setBody(byte[] body) {
  this.body = body;
 }

 public LocalDateTime getTime() {
  return time;
 }


 public String getSenderUser() {
  return senderUser;
 }

}
