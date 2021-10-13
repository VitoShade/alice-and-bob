package uni.project.a.b.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// Main idea is that the session should not be saved in the db, is currently possible???
// First try is that we saved it and use jpa for simplicity

/*@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

 */
public class AppSession {

   /* @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    */
    private Long id;

    private Long aliceId;

    private Long bobId;

    private String sessionKey;

    private String oneTimeKey;

    private LocalDateTime startedTime;

    // lists of messages???
    //@OneToMany
    private List<AppMessage> messages;

 public AppSession(Long id, Long aliceId, Long bobId) {
  this.id = id;
  this.aliceId = aliceId;
  this.bobId = bobId;
 }

 public Long getId() {
  return this.id;
 }

 public void setId(Long id) {
  this.id = id;
 }

 public Long getAliceId() {
  return aliceId;
 }

 public void setAliceId(Long aliceId) {
  this.aliceId = aliceId;
 }

 public Long getBobId() {
  return bobId;
 }

 public void setBobId(Long bobId) {
  this.bobId = bobId;
 }

 public String getSessionKey() {
  return sessionKey;
 }

 public void setSessionKey(String sessionKey) {
  this.sessionKey = sessionKey;
 }

 public String getOneTimeKey() {
  return oneTimeKey;
 }

 public void setOneTimeKey(String oneTimeKey) {
  this.oneTimeKey = oneTimeKey;
 }

 public LocalDateTime getStartedTime() {
  return startedTime;
 }

 public void setStartedTime(LocalDateTime startedTime) {
  this.startedTime = startedTime;
 }

 public List<AppMessage> getMessages() {
  return messages;
 }

 public void setMessages(AppMessage messages) {
  this.messages.add(messages);
 }
}
