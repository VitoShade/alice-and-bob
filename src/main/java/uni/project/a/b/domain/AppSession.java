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
@Data
public class AppSession {

   /* @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    */
    private Long id;

    private String aliceUser;


 private String bobUser;

    private String sessionKey;

    private String oneTimeKey;

    private LocalDateTime startedTime;

    // lists of messages???
    //@OneToMany
    private List<AppMessage> messages;


 public AppSession(Long id, String aliceUser, String bobUser) {
  this.id = id;
  this.aliceUser = aliceUser;
  this.bobUser = bobUser;

 }




 public List<AppMessage> getMessages() {
  return messages;
 }

 public void addMessage(AppMessage messages) {
  this.messages.add(messages);
 }

}
